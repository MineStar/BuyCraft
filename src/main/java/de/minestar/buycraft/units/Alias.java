package de.minestar.buycraft.units;

public class Alias {
    private final String playerName, aliasName;

    public Alias(String playerName, String aliasName) {
        this.playerName = playerName;
        this.aliasName = aliasName;
    }

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return the aliasName
     */
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String toString() {
        return "Alias={ " + this.playerName + " => " + this.aliasName + " }";
    }
}
