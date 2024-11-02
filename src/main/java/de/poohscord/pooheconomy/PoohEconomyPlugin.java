package de.poohscord.pooheconomy;

import de.poohscord.pooheconomy.economy.impl.command.BalanceCommand;
import de.poohscord.pooheconomy.economy.impl.data.DatabaseDriver;
import de.poohscord.pooheconomy.economy.impl.data.impl.MongoDriverImpl;
import de.poohscord.pooheconomy.economy.impl.listener.EconomyListener;
import de.poohscord.pooheconomy.economy.impl.message.MessageConfig;
import de.poohscord.pooheconomy.economy.impl.message.impl.MessageYamlConfig;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PoohEconomyPlugin extends JavaPlugin {

    private DatabaseDriver databaseDriver;

    @Override
    public void onEnable() {
        this.databaseDriver = new MongoDriverImpl(this);
        this.databaseDriver.connect();

        MessageConfig messageConfig = new MessageYamlConfig(this);

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new EconomyListener(this.databaseDriver, messageConfig), this);

        getCommand("balance").setExecutor(new BalanceCommand(this.databaseDriver, messageConfig));
    }

    @Override
    public void onDisable() {
        this.databaseDriver.disconnect();
    }
}
