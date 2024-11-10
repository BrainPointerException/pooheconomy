package de.poohscord.pooheconomy.economy;

import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EconomyManager extends EconomyQuery { ;

    Mono<Boolean> createAccount(Player player);

    Mono<Boolean> setBalance(String playerName, Currency currency, int amount);

    Mono<Boolean> setBalance(UUID playerUuid, Currency currency, int amount);

    Mono<Boolean> setBalance(Player player, Currency currency, int amount);

    Mono<Boolean> addBalance(String playerName, Currency currency, int amount);

    Mono<Boolean> addBalance(UUID playerUuid, Currency currency, int amount);

    Mono<Boolean> addBalance(Player player, Currency currency, int amount);

    Mono<Boolean> removeBalance(String playerName, Currency currency, int amount);

    Mono<Boolean> removeBalance(UUID playerUuid, Currency currency, int amount);

    Mono<Boolean> removeBalance(Player player, Currency currency, int amount);

    Mono<Boolean> transferBalance(String fromPlayerName, String toPlayerName, int amount);

    Mono<Boolean> transferBalance(UUID fromPlayerUuid, UUID toPlayerUuid, int amount);

    Mono<Boolean> transferBalance(Player fromPlayer, Player toPlayer, int amount);

    Mono<Boolean> exchangeCurrency(Player player, int exchangeRate, int amount);

}
