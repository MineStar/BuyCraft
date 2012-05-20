package de.minestar.buycraft.units;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersistentAlias extends Alias {
    private final int ID;

    private PersistentAlias(int ID, String playerName, String aliasName) {
        super(playerName, aliasName);
        this.ID = ID;
    }

    public PersistentAlias(ResultSet result) throws SQLException {
        this(result.getInt("ID"), result.getString("playerName"), result.getString("aliasName"));
    }

    /**
     * @return the iD
     */
    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "PersistentAlias={ " + this.ID + " ; " + this.getPlayerName() + " => " + this.getAliasName() + " }";
    }
}
