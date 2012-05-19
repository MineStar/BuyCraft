package de.minestar.buycraft.units;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.inventory.ItemStack;

public class BuyCraftStack {
    private final int StackID;
    private final int ShopID;
    private final int TypeID;
    private final short SubID;
    private int Amount = 1;

    /**
     * CONSTRUCTORS
     * 
     * @throws SQLException
     */

    public BuyCraftStack(ResultSet result) throws SQLException {
        this(result.getInt("ID"), result.getInt("ShopID"), result.getInt("TypeID"), result.getShort("SubID"), result.getInt("Amount"));
    }

    private BuyCraftStack(int StackID, int ShopID, int TypeID, short SubID, int Amount) {
        this.StackID = StackID;
        this.ShopID = ShopID;
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

    /**
     * @return the StackID
     */
    public int getStackID() {
        return this.StackID;
    }

    /**
     * @return the shopID
     */
    public int getShopID() {
        return ShopID;
    }

    @Override
    public String toString() {
        return "BuyCraftStack={ " + this.StackID + " = " + this.TypeID + " : " + this.SubID + " * " + this.Amount + " ; }";
    }
}
