package de.poohscord.pooheconomy.economy.impl.message;

import de.poohscord.pooheconomy.economy.Currency;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;

public interface MessageConfig {

    Component getPlayerEconomyInitSuccessMessage();

    Component getPlayerEconomyInitFailedMessage();

    Component getCommandSenderMustBePlayerMessage();

    Component getOwnBalanceMessage(Map<Currency, Integer> currencies);

    Component getBalanceMessage(Player sender, String targetName, Map<Currency, Integer> currencies);

    Component getBalanceChangeMessage(Player player, String targetName, Currency currency, int amount);

    Component getPlayerNotFoundMessage();

    Component getBalanceCommandUsageMessage();

    Component getPayCommandUsageMessage();

    Component getBalanceNotEnoughMessage(Currency currency);

    Component getPaymentTransferMessage(Player sender, String targetName, int amount);

    Component getPaymentReceiveMessage(Player receiver, String senderName, int amount);

    Component getExchangeCommandUsageMessage();

    Component getExchangeSuccessMessage(int fromAmount, int toAmount);

}
