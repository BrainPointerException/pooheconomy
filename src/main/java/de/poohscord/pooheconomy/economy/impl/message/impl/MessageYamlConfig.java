package de.poohscord.pooheconomy.economy.impl.message.impl;

import de.poohscord.pooheconomy.economy.Currency;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MessageYamlConfig implements MessageConfig {

    private final YamlConfiguration config;
    private final Component prefix;

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    public MessageYamlConfig(JavaPlugin plugin) {
        final File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.prefix = SERIALIZER.deserialize(this.config.getString("prefix"));
    }

    @Override
    public Component getPlayerEconomyInitSuccessMessage() {
        return getMessage("playerEconomyInitSuccessMessage");
    }

    @Override
    public Component getPlayerEconomyInitFailedMessage() {
        return getMessage("playerEconomyInitFailMessage");
    }

    @Override
    public Component getCommandSenderMustBePlayerMessage() {
        return getMessage("commandSenderMustBePlayerMessage");
    }

    @Override
    public Component getOwnBalanceMessage(Map<Currency, Integer> currencies) {
        return getStringListMessage("ownBalanceMessage", Map.of(
                "%honigtropfen%", String.valueOf(currencies.get(Currency.HONIGTROPFEN)),
                "%honigkristalle%", String.valueOf(currencies.get(Currency.HONIGKRISTALLE))
        ));
    }

    @Override
    public Component getBalanceMessage(Player sender, String targetName, Map<Currency, Integer> currencies) {
        return getStringListMessage("balanceMessage", Map.of(
                "%player_name%", targetName,
                "%honigtropfen%", String.valueOf(currencies.get(Currency.HONIGTROPFEN)),
                "%honigkristalle%", String.valueOf(currencies.get(Currency.HONIGKRISTALLE))
        ));
    }

    @Override
    public Component getBalanceChangeMessage(Player player, String targetName, Currency currency, int amount) {
        return SERIALIZER.deserialize(SERIALIZER.serialize(getMessage("balanceChangeMessage"))
                .replace("%currency%", currency.name())
                .replace("%amount%", String.valueOf(amount))
                .replace("%player_name%", targetName));
    }

    @Override
    public Component getPlayerNotFoundMessage() {
        return getMessage("playerNotFoundMessage");
    }

    @Override
    public Component getBalanceCommandUsageMessage() {
        return getMessage("balanceCommandUsageMessage");
    }

    private Component getStringListMessage(String message, Map<String, String> placeholders) {
        List<String> messageList = this.config.getStringList("message." + message);
        Component component = Component.text("").appendNewline();
        for (String s : messageList) {
            boolean contains = false;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                if (s.contains(entry.getKey())) {
                    component = component.append(SERIALIZER.deserialize(s.replace(entry.getKey(), entry.getValue()))).appendNewline();
                    contains = true;
                }
            }
            if (!contains) {
                component = component.append(SERIALIZER.deserialize(s)).appendNewline();
            }
        }
        return component;
    }

    private Component getMessage(String message) {
        return prefix.append(SERIALIZER.deserialize(this.config.getString("message." + message)));
    }
}
