package de.poohscord.pooheconomy.economy.impl.listener;

import de.poohscord.pooheconomy.economy.EconomyManager;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyListener implements Listener {

    private final EconomyManager economyManager;
    private final MessageConfig config;

    public EconomyListener(EconomyManager economyManager, MessageConfig config) {
        this.economyManager = economyManager;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.economyManager.hasAccount(player)
                .subscribe(hasAccount -> {
                    if (!hasAccount) {
                        this.economyManager.createAccount(player)
                                .subscribe(success -> {
                                    if (success) {
                                        player.sendMessage(config.getPlayerEconomyInitSuccessMessage());
                                    } else {
                                        player.sendMessage(config.getPlayerEconomyInitFailedMessage());
                                    }
                                });
                    }
                });
    }

}
