package com.bukkit.gemo.BuyCraft;

public class BCMinimalItem {
    private int ID, SubID;

    public BCMinimalItem(int ID, int SubID) {
        this.ID = ID;
        this.SubID = SubID;
    }

    public int getID() {
        return ID;
    }

    public int getSubID() {
        return SubID;
    }
}
