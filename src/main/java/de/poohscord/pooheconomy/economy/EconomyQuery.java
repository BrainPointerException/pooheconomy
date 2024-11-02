package de.poohscord.pooheconomy.economy;

import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EconomyQuery {

    Mono<Integer> getBalance(UUID playerUuid, Currency currency);

    Mono<Integer> getBalance(Player player, Currency currency);

    Mono<Integer> getBalance(String playerName, Currency currency);

    Mono<Boolean> hasSufficientBalance(UUID playerUuid, Currency currency, int amount);

    Mono<Boolean> hasSufficientBalance(Player player, Currency currency, int amount);

    Mono<Boolean> hasSufficientBalance(String playerName, Currency currency, int amount);

    Mono<Boolean> hasAccount(UUID playerUuid);

    Mono<Boolean> hasAccount(Player player);

    Mono<Boolean> hasAccount(String playerName);

}
