package de.minestar.buycraft.units;

import java.util.ArrayList;

import de.minestar.buycraft.manager.DatabaseManager;
import de.minestar.buycraft.shops.UserShop;

public class BuyCraftInventory {

    private final ArrayList<BuyCraftStack> items;

    public BuyCraftInventory() {
        this.items = new ArrayList<BuyCraftStack>();
    }

    public int countGeneralStackSize() {
        int count = 0;
        for (BuyCraftStack stack : this.items) {
            count += stack.getAmount();
        }
        return count;
    }

    public int countItemStack(int TypeID, short SubID) {
        int count = 0;
        for (BuyCraftStack stack : this.items) {
            if (stack.equals(TypeID, SubID)) {
                count += stack.getAmount();
            }
        }
        return count;
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

    public boolean addItem(UserShop shop, int TypeID, short SubID, int Amount) {
        BuyCraftStack stack = DatabaseManager.getInstance().createItemStack(shop, TypeID, SubID, Amount);
        if (stack == null)
            return false;
        this.addItem(stack);
        return true;
    }

    public boolean removeItem(UserShop shop, int TypeID, short SubID, int Amount) {
        // make a copy of all current itemstacks
        ArrayList<NonPersistenBuyCraftStack> tempStacks = new ArrayList<NonPersistenBuyCraftStack>();
        for (BuyCraftStack stack : this.items) {
            tempStacks.add(new NonPersistenBuyCraftStack(stack.getTypeID(), stack.getSubID(), stack.getAmount()));
        }

        // clear inventory from DB and internally
        DatabaseManager.getInstance().removeInventory(shop);
        this.clearItems();

        // reduce the amount of the item
        int reduceAmount = Amount;
        for (NonPersistenBuyCraftStack stack : tempStacks) {
            // wrong type => continue
            if (!stack.equals(TypeID, SubID)) {
                continue;
            }

            // reduce it
            if (stack.getAmount() >= reduceAmount) {
                stack.setAmount(stack.getAmount() - reduceAmount);
                reduceAmount = 0;
            } else {
                // so we need to split it
                reduceAmount -= stack.getAmount();
                stack.setAmount(0);
            }

            // completely reduced?
            if (reduceAmount < 1)
                break;
        }

        // finally write the new items into the database
        for (NonPersistenBuyCraftStack stack : tempStacks) {
            // Amount < 1 => continue
            if (stack.getAmount() < 1) {
                continue;
            }

            // Add the item. If this fails, there is an internal error
            if (!this.addItem(shop, stack.getTypeID(), stack.getSubID(), stack.getAmount())) {
                return false;
            }
        }

        return true;
    }
}
