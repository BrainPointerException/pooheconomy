package de.poohscord.pooheconomy.economy.impl.command;

import de.poohscord.pooheconomy.economy.EconomyManager;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import de.poohscord.pooheconomy.economy.impl.settings.SettingsConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExchangeCommand implements CommandExecutor {

    private final EconomyManager em;
    private final MessageConfig config;
    private final SettingsConfig settingsConfig;

    public ExchangeCommand(EconomyManager em, MessageConfig config, SettingsConfig settingsConfig) {
        this.em = em;
        this.config = config;
        this.settingsConfig = settingsConfig;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.config.getCommandSenderMustBePlayerMessage());
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(this.config.getExchangeCommandUsageMessage());
            return false;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(this.config.getExchangeCommandUsageMessage());
            return false;
        }

        if (amount <= 0) {
            player.sendMessage(this.config.getExchangeCommandUsageMessage());
            return false;
        }

        int toAmount = this.settingsConfig.getExchangeRate() * amount;

        em.exchangeCurrency(player, settingsConfig.getExchangeRate(), amount).subscribe(success -> {
            if (success) {
                player.sendMessage(this.config.getExchangeSuccessMessage(amount, toAmount));
            } else {
                player.sendMessage(this.config.getExchangeCommandUsageMessage());
            }
        });

        return true;
    }
}
