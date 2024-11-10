package de.poohscord.pooheconomy.economy.impl.settings.impl;

import de.poohscord.pooheconomy.economy.impl.settings.SettingsConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SettingsYamlConfig implements SettingsConfig {

    private final YamlConfiguration config;

    public SettingsYamlConfig(JavaPlugin plugin) {
        final File file = new File(plugin.getDataFolder(), "settings.yml");
        if (!file.exists()) {
            plugin.saveResource("settings.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public int getExchangeRate() {
        return this.config.getInt("settings.exchangeRate");
    }
}
