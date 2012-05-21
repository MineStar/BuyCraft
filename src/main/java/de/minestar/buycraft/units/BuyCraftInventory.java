package de.minestar.buycraft.units;

import java.util.ArrayList;

import de.minestar.buycraft.manager.DatabaseManager;
import de.minestar.buycraft.shops.UserShop;

public class BuyCraftInventory {

    private final ArrayList<PersistentBuyCraftStack> items;

    public BuyCraftInventory() {
        this.items = new ArrayList<PersistentBuyCraftStack>();
    }

    public int countGeneralStackSize() {
        int count = 0;
        for (PersistentBuyCraftStack stack : this.items) {
            count += stack.getAmount();
        }
        return count;
    }

    public int countItemStack(int TypeID, short SubID) {
        int count = 0;
        for (PersistentBuyCraftStack stack : this.items) {
            if (stack.equals(TypeID, SubID)) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    public ArrayList<PersistentBuyCraftStack> setItems(ArrayList<PersistentBuyCraftStack> items) {
        this.clearItems();
        this.items.addAll(items);
        return this.items;
    }

    public ArrayList<PersistentBuyCraftStack> addItems(ArrayList<PersistentBuyCraftStack> items) {
        this.items.addAll(items);
        return this.getItems();
    }

    public ArrayList<PersistentBuyCraftStack> getItems() {
        return this.items;
    }

    public ArrayList<PersistentBuyCraftStack> clearItems() {
        this.items.clear();
        return this.getItems();
    }

    public int getSize() {
        return this.items.size();
    }

    public PersistentBuyCraftStack getItem(int index) {
        try {
            return this.items.get(index);
        } catch (Exception e) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public ArrayList<PersistentBuyCraftStack> addItem(PersistentBuyCraftStack item) {
        this.items.add(item);
        return this.getItems();
    }

    @Override
    public String toString() {
        String txt = "BuyCraftInventory={ ";
        for (PersistentBuyCraftStack stack : this.items) {
            txt += "\r\n" + stack.toString() + " ; ";
        }
        txt += "}";
        return txt;
    }

    public boolean addItem(UserShop shop, int TypeID, short SubID, int Amount) {
        PersistentBuyCraftStack stack = DatabaseManager.getInstance().createItemStack(shop, TypeID, SubID, Amount);
        if (stack == null)
            return false;
        this.addItem(stack);
        return true;
    }

    public boolean removeItem(UserShop shop, int TypeID, short SubID, int Amount) {
        // make a copy of all current itemstacks
        ArrayList<BuyCraftStack> tempStacks = new ArrayList<BuyCraftStack>();
        for (PersistentBuyCraftStack stack : this.items) {
            tempStacks.add(new BuyCraftStack(stack.getTypeID(), stack.getSubID(), stack.getAmount()));
        }

        // clear inventory from DB and internally
        DatabaseManager.getInstance().removeInventory(shop);
        this.clearItems();

        // reduce the amount of the item
        int reduceAmount = Amount;
        for (BuyCraftStack stack : tempStacks) {
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
        for (BuyCraftStack stack : tempStacks) {
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
