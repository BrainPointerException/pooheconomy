package de.poohscord.pooheconomy.economy.impl.data;

import de.poohscord.pooheconomy.economy.EconomyManager;

public interface DatabaseDriver extends EconomyManager {

    void connect();

    void disconnect();

}
