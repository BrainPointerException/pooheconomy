package de.poohscord.pooheconomy.economy.impl.data.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.reactivestreams.client.*;
import de.poohscord.pooheconomy.economy.Currency;
import de.poohscord.pooheconomy.economy.impl.data.DatabaseDriver;
import org.bson.Document;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.UUID;

public class MongoDriverImpl implements DatabaseDriver {

    private final YamlConfiguration config;
    private MongoClient client;
    private MongoCollection<Document> collection;

    public MongoDriverImpl(JavaPlugin plugin) {
        final File file = new File(plugin.getDataFolder(), "connection.yml");
        if (!file.exists()) {
            plugin.saveResource("connection.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void connect() {
        String uri = this.config.getString("db.mongo.uri");

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        ConnectionString connectionString = new ConnectionString(uri);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(serverApi)
                .build();

        this.client = MongoClients.create(settings);
        MongoDatabase database = this.client.getDatabase(connectionString.getDatabase());
        this.collection = database.getCollection(this.config.getString("db.mongo.collection"));

        initializeIndexes();
    }

    private void initializeIndexes() {
        IndexOptions indexOptions = new IndexOptions().unique(true);
        Publisher<String> publisher = this.collection.createIndex(Indexes.ascending("playerUuid"), indexOptions);
        Mono.from(publisher).block();
    }

    @Override
    public void disconnect() {
        this.collection = null;
        this.client.close();
    }

    @Override
    public Mono<Boolean> createAccount(Player player) {
        return Mono.from(this.collection.insertOne(new Document("playerUuid", player.getUniqueId().toString())
                .append("playerName", player.getName())
                .append(Currency.HONIGTROPFEN.name(), 0)
                .append(Currency.HONIGKRISTALLE.name(), 0)))
                .map(success -> true);
    }

    @Override
    public Mono<Boolean> setBalance(String playerName, Currency currency, int amount) {
        return getUuidFromPlayerName(playerName)
                .flatMap(uuid -> setBalance(uuid, currency, amount));
    }

    @Override
    public Mono<Boolean> setBalance(UUID playerUuid, Currency currency, int amount) {
        if (amount < 0) {
            return Mono.just(false);
        }
        return hasAccount(playerUuid)
                .flatMap(hasAccount -> {
                    if (!hasAccount) return Mono.just(false);
                    return updateBalance(playerUuid.toString(), currency, amount);
                });
    }

    @Override
    public Mono<Boolean> setBalance(Player player, Currency currency, int amount) {
        return setBalance(player.getUniqueId(), currency, amount);
    }

    private Mono<Boolean> updateBalance(String uuid, Currency currency, int amount) {
        return Mono.from(this.collection.updateOne(new Document("playerUuid", uuid), new Document("$set", new Document(currency.name(), amount))))
                .map(updateResult -> updateResult.getModifiedCount() > 0);
    }

    @Override
    public Mono<Boolean> addBalance(String playerName, Currency currency, int amount) {
        return getUuidFromPlayerName(playerName)
                .flatMap(uuid -> addBalance(UUID.fromString(uuid), currency, amount));
    }

    @Override
    public Mono<Boolean> addBalance(UUID playerUuid, Currency currency, int amount) {
        return getBalance(playerUuid, currency)
                .flatMap(balance -> setBalance(playerUuid, currency, balance + amount));
    }

    @Override
    public Mono<Boolean> addBalance(Player player, Currency currency, int amount) {
        return addBalance(player.getUniqueId(), currency, amount);
    }

    @Override
    public Mono<Boolean> removeBalance(String playerName, Currency currency, int amount) {
        return getUuidFromPlayerName(playerName)
                .flatMap(uuid -> removeBalance(UUID.fromString(uuid), currency, amount));
    }

    @Override
    public Mono<Boolean> removeBalance(UUID playerUuid, Currency currency, int amount) {
        return getBalance(playerUuid, currency)
                .flatMap(balance -> setBalance(playerUuid, currency, balance - amount));
    }

    @Override
    public Mono<Boolean> removeBalance(Player player, Currency currency, int amount) {
        return removeBalance(player.getUniqueId(), currency, amount);
    }

    @Override
    public Mono<Boolean> transferBalance(String fromPlayerName, String toPlayerName, int amount) {
        return Mono.zip(getUuidFromPlayerName(fromPlayerName), getUuidFromPlayerName(toPlayerName))
                .flatMap(tuple -> transferBalance(UUID.fromString(tuple.getT1()), UUID.fromString(tuple.getT2()), amount));
    }

    @Override
    public Mono<Boolean> transferBalance(UUID fromPlayerUuid, UUID toPlayerUuid, int amount) {
        return Mono.zip(getBalance(fromPlayerUuid, Currency.HONIGTROPFEN), getBalance(toPlayerUuid, Currency.HONIGTROPFEN))
                .flatMap(tuple -> {
                    int fromBalance = tuple.getT1();
                    int toBalance = tuple.getT2();
                    if (fromBalance < amount) return Mono.just(false);
                    return Mono.zip(setBalance(fromPlayerUuid, Currency.HONIGTROPFEN, fromBalance - amount),
                            setBalance(toPlayerUuid, Currency.HONIGTROPFEN, toBalance + amount))
                            .map(success -> success.getT1() && success.getT2());
                });
    }

    @Override
    public Mono<Boolean> transferBalance(Player fromPlayer, Player toPlayer, int amount) {
        return transferBalance(fromPlayer.getUniqueId(), toPlayer.getUniqueId(), amount);
    }

    @Override
    public Mono<Integer> getBalance(UUID playerUuid, Currency currency) {
        return Mono.from(this.collection.find(new Document("playerUuid", playerUuid.toString())))
                .map(document -> document.getInteger(currency.name()));
    }

    @Override
    public Mono<Integer> getBalance(Player player, Currency currency) {
        return getBalance(player.getUniqueId(), currency);
    }

    @Override
    public Mono<Integer> getBalance(String playerName, Currency currency) {
        return getUuidFromPlayerName(playerName)
                .flatMap(uuid -> getBalance(UUID.fromString(uuid), currency));
    }

    @Override
    public Mono<Boolean> hasSufficientBalance(UUID playerUuid, Currency currency, int amount) {
        return getBalance(playerUuid, currency)
                .map(balance -> balance >= amount);
    }

    @Override
    public Mono<Boolean> hasSufficientBalance(Player player, Currency currency, int amount) {
        return hasSufficientBalance(player.getUniqueId(), currency, amount);
    }

    @Override
    public Mono<Boolean> hasSufficientBalance(String playerName, Currency currency, int amount) {
        return getUuidFromPlayerName(playerName)
                .flatMap(uuid -> hasSufficientBalance(UUID.fromString(uuid), currency, amount));
    }

    @Override
    public Mono<Boolean> hasAccount(UUID playerUuid) {
        return hasEntry(this.collection.find(new Document("playerUuid", playerUuid.toString())));
    }

    @Override
    public Mono<Boolean> hasAccount(Player player) {
        return hasAccount(player.getUniqueId());
    }

    @Override
    public Mono<Boolean> hasAccount(String playerName) {
        return getUuidFromPlayerName(playerName)
                .flatMap(this::hasAccount)
                .defaultIfEmpty(false);
    }

    private Mono<Boolean> hasEntry(FindPublisher<Document> findPublisher) {
        return Flux.from(findPublisher)
                .count()
                .map(count -> count > 0);
    }

    public Mono<String> getUuidFromPlayerName(String playerName) {
        return Flux.from(this.collection.find(new Document("playerName", new Document("$regex", playerName).append("$options", "i"))))
                .map(document -> document.getString("playerUuid"))
                .next();
    }
}
