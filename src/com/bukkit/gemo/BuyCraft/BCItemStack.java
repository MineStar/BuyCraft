package com.bukkit.gemo.BuyCraft;

import java.io.Serializable;

import org.bukkit.inventory.ItemStack;

public class BCItemStack implements Serializable {
    private static final long serialVersionUID = 4097550354323969698L;
    private int Id = 0;
    private short SubId = 0;
    private int Amount = 1;

    public BCItemStack(int Id) {
        setId(Id);
        setSubId((byte) 0);
        setAmount(1);
    }

    public BCItemStack(int Id, byte SubId) {
        setId(Id);
        setSubId(SubId);
        setAmount(1);
    }

    public BCItemStack(int Id, int Amount) {
        setId(Id);
        setSubId((byte) 0);
        setAmount(Amount);
    }

    public BCItemStack(int Id, short SubId, int Amount) {
        setId(Id);
        setSubId(SubId);
        setAmount(Amount);
    }

    public BCItemStack(ItemStack item) {
        setId(item.getTypeId());
        setSubId(item.getData().getData());
        setAmount(item.getAmount());
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(getId());
        item.setAmount(getAmount());
        item.setDurability(getSubId());
        return item;
    }

    /**
     * @return the id
     */
    public int getId() {
        return Id;
    }
    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        Id = id;
    }
    /**
     * @return the amount
     */
    public int getAmount() {
        return Amount;
    }
    /**
     * @param amount
     *            the amount to set
     */
    public void setAmount(int amount) {
        Amount = amount;
    }

    /**
     * @return the subId
     */
    public short getSubId() {
        return SubId;
    }

    /**
     * @param subId
     *            the subId to set
     */
    public void setSubId(short subId) {
        SubId = subId;
    }
}
