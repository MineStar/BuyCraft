package de.minestar.buycraft.units;

import java.sql.SQLException;

import org.bukkit.inventory.ItemStack;

public class NonPersistenBuyCraftStack {
    private final int TypeID;
    private final short SubID;
    private int Amount = 1;

    /**
     * CONSTRUCTORS
     * 
     * @throws SQLException
     */

    public NonPersistenBuyCraftStack(int TypeID, short SubID, int Amount) {
        this.TypeID = TypeID;
        this.SubID = SubID;
        this.Amount = Amount;
    }

    /**
     * get the BuyCraftStack as an ItemStack
     * 
     * @return the ItemStack
     */
    public ItemStack getItem() {
        ItemStack item = new ItemStack(this.getTypeID());
        item.setAmount(this.getAmount());
        item.setDurability(this.getSubID());
        return item;
    }

    /**
     * @return the TypeID
     */
    public int getTypeID() {
        return this.TypeID;
    }

    /**
     * @return the Amount
     */
    public int getAmount() {
        return this.Amount;
    }

    /**
     * @param amount
     *            the amount to set
     */
    public void setAmount(int amount) {
        this.Amount = amount;
    }

    /**
     * @return the SubID
     */
    public short getSubID() {
        return this.SubID;
    }

    @Override
    public String toString() {
        return "BuyCraftStack={ " + this.TypeID + " : " + this.SubID + " * " + this.Amount + " ; }";
    }

    public boolean equals(int TypeID, short SubID) {
        return this.TypeID == TypeID && this.SubID == SubID;
    }
}
