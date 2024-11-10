package de.poohscord.pooheconomy.economy.impl.command;

import de.poohscord.pooheconomy.economy.Currency;
import de.poohscord.pooheconomy.economy.EconomyManager;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PayCommand implements CommandExecutor, TabExecutor {

    private final EconomyManager em;
    private final MessageConfig config;

    public PayCommand(EconomyManager em, MessageConfig config) {
        this.em = em;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.config.getCommandSenderMustBePlayerMessage());
            return false;
        }

        if (args.length != 2) {
            player.sendMessage(this.config.getPayCommandUsageMessage());
            return false;
        }

        final Player target = player.getServer().getPlayer(args[0]);

        if (target != null && target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(this.config.getPayCommandUsageMessage());
            return false;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(this.config.getPayCommandUsageMessage());
            return false;
        }

        em.hasSufficientBalance(player, Currency.HONIGTROPFEN, amount).subscribe(balance -> {
            if (!balance) {
                player.sendMessage(this.config.getBalanceNotEnoughMessage(Currency.HONIGTROPFEN));
                return;
            }
            if (target != null) {
                em.transferBalance(player, target, amount).subscribe(success -> {
                    if (success) {
                        player.sendMessage(this.config.getPaymentTransferMessage(player, target.getName(), amount));
                        target.sendMessage(this.config.getPaymentReceiveMessage(target, player.getName(), amount));
                    } else {
                        player.sendMessage(this.config.getPlayerNotFoundMessage());
                    }
                });
            } else {
                em.transferBalance(player.getName(), args[0], amount).subscribe(success -> {
                    if (success) {
                        player.sendMessage(this.config.getPaymentTransferMessage(player, args[0], amount));
                    } else {
                        player.sendMessage(this.config.getPlayerNotFoundMessage());
                    }
                });
            }
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> !player.getUniqueId().equals(((Player) sender).getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
