package de.poohscord.pooheconomy.economy.impl.command;

import de.poohscord.pooheconomy.economy.Currency;
import de.poohscord.pooheconomy.economy.EconomyManager;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BalanceCommand implements CommandExecutor, TabExecutor {

    private final EconomyManager em;
    private final MessageConfig config;
    private static final String PERMISSION = "poohscord.pooheconomy.admin";

    public BalanceCommand(EconomyManager em, MessageConfig config) {
        this.em = em;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.config.getCommandSenderMustBePlayerMessage());
            return false;
        }

        switch (args.length) {
            case 0 -> Mono.zip(
                    this.em.getBalance(player, Currency.HONIGTROPFEN),
                    this.em.getBalance(player, Currency.HONIGKRISTALLE)
            ).subscribe(tuple -> player.sendMessage(this.config.getOwnBalanceMessage(Map.of(
                    Currency.HONIGTROPFEN, tuple.getT1(),
                    Currency.HONIGKRISTALLE, tuple.getT2()
            ))));
            case 1 -> {
                final Player target = player.getServer().getPlayer(args[0]);
                if (target != null) {
                    Mono.zip(
                            this.em.getBalance(target, Currency.HONIGTROPFEN).defaultIfEmpty(-1),
                            this.em.getBalance(target, Currency.HONIGKRISTALLE).defaultIfEmpty(-1)
                    ).subscribe(tuple -> {
                        if (tuple.getT1() == -1 || tuple.getT2() == -1) {
                            player.sendMessage(this.config.getPlayerNotFoundMessage());
                            return;
                        }
                        player.sendMessage(this.config.getBalanceMessage(player, target.getName(), Map.of(
                                Currency.HONIGTROPFEN, tuple.getT1(),
                                Currency.HONIGKRISTALLE, tuple.getT2()
                        )));
                    });
                } else {
                    Mono.zip(
                            this.em.getBalance(args[0], Currency.HONIGTROPFEN).defaultIfEmpty(-1),
                            this.em.getBalance(args[0], Currency.HONIGKRISTALLE).defaultIfEmpty(-1)
                    ).subscribe(tuple -> {
                        if (tuple.getT1() == -1 || tuple.getT2() == -1) {
                            player.sendMessage(this.config.getPlayerNotFoundMessage());
                            return;
                        }
                        player.sendMessage(this.config.getBalanceMessage(player, args[0], Map.of(
                                Currency.HONIGTROPFEN, tuple.getT1(),
                                Currency.HONIGKRISTALLE, tuple.getT2()
                        )));
                    });
                }
            }
            case 4 -> {
                if (!player.hasPermission(PERMISSION)) {
                    return false;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(this.config.getBalanceCommandUsageMessage());
                    return false;
                }
                if (amount < 0) {
                    player.sendMessage(this.config.getBalanceCommandUsageMessage());
                    return false;
                }
                Player target = player.getServer().getPlayer(args[2]);
                Currency currency;
                try {
                    currency = Currency.valueOf(args[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(this.config.getBalanceCommandUsageMessage());
                    return false;
                }
                final int finalAmount = amount;
                final Currency finalCurrency = currency;
                switch (args[0]) {
                    case "set" -> {
                        if (target != null) {
                            this.em.setBalance(target, currency, amount).subscribe(success -> {
                                if (success) {
                                    player.sendMessage(this.config.getBalanceChangeMessage(player, target.getName(), finalCurrency, finalAmount));
                                } else {
                                    player.sendMessage(this.config.getPlayerNotFoundMessage());
                                }
                            });
                        } else {
                            this.em.setBalance(args[2], currency, amount).subscribe(success -> {
                                if (success) {
                                    player.sendMessage(this.config.getBalanceChangeMessage(player, args[2], finalCurrency, finalAmount));
                                } else {
                                    player.sendMessage(this.config.getPlayerNotFoundMessage());
                                }
                            });
                        }
                        return true;
                    }
                    case "add" -> {
                        if (target != null) {
                            this.em.addBalance(target, currency, amount).subscribe(success -> {
                                if (success) {
                                    player.sendMessage(this.config.getBalanceChangeMessage(player, target.getName(), finalCurrency, finalAmount));
                                } else {
                                    player.sendMessage(this.config.getPlayerNotFoundMessage());
                                }
                            });
                        } else {
                            this.em.addBalance(args[2], currency, amount).subscribe(success -> {
                                if (success) {
                                    player.sendMessage(this.config.getBalanceChangeMessage(player, args[2], finalCurrency, finalAmount));
                                } else {
                                    player.sendMessage(this.config.getPlayerNotFoundMessage());
                                }
                            });
                        }
                        return true;
                    }
                    case "remove" -> {
                        if (target != null) {
                            this.em.removeBalance(target, currency, amount).subscribe(success -> {
                                if (success) {
                                    player.sendMessage(this.config.getBalanceChangeMessage(player, target.getName(), finalCurrency, finalAmount));
                                } else {
                                    player.sendMessage(this.config.getPlayerNotFoundMessage());
                                }
                            });
                        } else {
                            this.em.removeBalance(args[2], currency, amount).subscribe(success -> {
                                if (success) {
                                    player.sendMessage(this.config.getBalanceChangeMessage(player, args[2], finalCurrency, finalAmount));
                                } else {
                                    player.sendMessage(this.config.getPlayerNotFoundMessage());
                                }
                            });
                        }
                        return true;
                    }
                    default -> {
                        player.sendMessage(this.config.getBalanceCommandUsageMessage());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                List<String> completions = new ArrayList<>();
                if (sender.hasPermission(PERMISSION)) {
                    completions.add("set");
                    completions.add("add");
                    completions.add("remove");
                }
                for (Player player : sender.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                return completions;
            }
            case 2 -> {
                if (!sender.hasPermission(PERMISSION)) {
                    return List.of();
                }
                List<String> completions = new ArrayList<>();
                for (Currency currency : Currency.values()) {
                    completions.add(currency.name());
                }
                return completions;
            }
            case 3 -> {
                if (!sender.hasPermission(PERMISSION)) {
                    return List.of();
                }
                List<String> completions = new ArrayList<>();
                for (Player player : sender.getServer().getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                return completions;
            }
        }
        return List.of();
    }
}
