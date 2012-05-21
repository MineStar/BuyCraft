package de.minestar.buycraft.units;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.inventory.ItemStack;

public class PersistentBuyCraftStack extends BuyCraftStack {
    private final int StackID;
    private final int ShopID;

    /**
     * CONSTRUCTORS
     * 
     * @throws SQLException
     */

    public PersistentBuyCraftStack(ResultSet result) throws SQLException {
        this(result.getInt("ID"), result.getInt("ShopID"), result.getInt("TypeID"), result.getShort("SubID"), result.getInt("Amount"));
    }

    private PersistentBuyCraftStack(int StackID, int ShopID, int TypeID, short SubID, int Amount) {
        super(TypeID, SubID, Amount);
        this.StackID = StackID;
        this.ShopID = ShopID;
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
        return "PersistentBuyCraftStack={ " + this.StackID + " = " + this.getTypeID() + " : " + this.getSubID() + " * " + this.getAmount() + " ; }";
    }
}