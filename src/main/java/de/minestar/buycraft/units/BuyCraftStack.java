package de.minestar.buycraft.units;

import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BuyCraftStack implements Comparable<BuyCraftStack> {
    private final int TypeID;
    private final short SubID;
    private int Amount = 1;

    /**
     * CONSTRUCTORS
     * 
     * @throws SQLException
     */

    public BuyCraftStack(int TypeID, short SubID, int Amount) {
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
        Material mat = Material.matchMaterial(Integer.toString(getTypeID()));
        ItemStack item = new ItemStack(mat);
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

    @Override
    public int compareTo(BuyCraftStack other) {
        return this.getTypeID() - other.getTypeID();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof BuyCraftStack) {
            return this.equals((BuyCraftStack) obj);
        }

        return false;
    }

    /**
     * Check if another BuyCraftStack equals this BuyCraftStack
     * 
     * @param other
     * @return <b>true</b> if the stacks are equal, otherwise <b>false</b>
     */
    public boolean equals(BuyCraftStack other) {
        return (this.getTypeID() == other.getTypeID() && this.getSubID() == other.getSubID() && this.getAmount() == other.getAmount());
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(this.getTypeID()).hashCode() + Short.valueOf(this.getSubID()).hashCode();
    }
}
