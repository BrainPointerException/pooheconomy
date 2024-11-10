package de.poohscord.pooheconomy;

import de.poohscord.pooheconomy.economy.EconomyManager;
import de.poohscord.pooheconomy.economy.impl.command.BalanceCommand;
import de.poohscord.pooheconomy.economy.impl.command.ExchangeCommand;
import de.poohscord.pooheconomy.economy.impl.command.PayCommand;
import de.poohscord.pooheconomy.economy.impl.data.DatabaseDriver;
import de.poohscord.pooheconomy.economy.impl.data.impl.MongoDriverImpl;
import de.poohscord.pooheconomy.economy.impl.listener.EconomyListener;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import de.poohscord.pooheconomy.economy.impl.message.impl.MessageYamlConfig;
import de.poohscord.pooheconomy.economy.impl.settings.SettingsConfig;
import de.poohscord.pooheconomy.economy.impl.settings.impl.SettingsYamlConfig;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PoohEconomyPlugin extends JavaPlugin {

    private static DatabaseDriver databaseDriver;

    @Override
    public void onEnable() {
        databaseDriver = new MongoDriverImpl(this);
        databaseDriver.connect();

        MessageConfig messageConfig = new MessageYamlConfig(this);
        SettingsConfig settingsConfig = new SettingsYamlConfig(this);

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new EconomyListener(databaseDriver, messageConfig), this);

        getCommand("balance").setExecutor(new BalanceCommand(databaseDriver, messageConfig));
        getCommand("pay").setExecutor(new PayCommand(databaseDriver, messageConfig));
        getCommand("exchange").setExecutor(new ExchangeCommand(databaseDriver, messageConfig, settingsConfig));
    }

    @Override
    public void onDisable() {
        databaseDriver.disconnect();
    }

    public static EconomyManager getEconomyManager() {
        return databaseDriver;
    }
}
