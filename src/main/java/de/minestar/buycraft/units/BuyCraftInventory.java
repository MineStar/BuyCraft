package de.minestar.buycraft.units;

import java.util.ArrayList;

public class BuyCraftInventory {
    private final ArrayList<BuyCraftStack> items;

    public BuyCraftInventory() {
        this.items = new ArrayList<BuyCraftStack>();
    }

    public ArrayList<BuyCraftStack> setItems(ArrayList<BuyCraftStack> items) {
        this.clearItems();
        this.items.addAll(items);
        return this.items;
    }

    public ArrayList<BuyCraftStack> addItems(ArrayList<BuyCraftStack> items) {
        this.items.addAll(items);
        return this.getItems();
    }

    public ArrayList<BuyCraftStack> getItems() {
        return this.items;
    }

    public ArrayList<BuyCraftStack> clearItems() {
        this.items.clear();
        return this.getItems();
    }

    public int getSize() {
        return this.items.size();
    }

    public BuyCraftStack getItem(int index) {
        try {
            return this.items.get(index);
        } catch (Exception e) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public ArrayList<BuyCraftStack> addItem(BuyCraftStack item) {
        this.items.add(item);
        return this.getItems();
    }

    @Override
    public String toString() {
        String txt = "BuyCraftInventory={ ";
        for (BuyCraftStack stack : this.items) {
            txt += "\r\n" + stack.toString() + " ; ";
        }
        txt += "}";
        return txt;
    }
}
