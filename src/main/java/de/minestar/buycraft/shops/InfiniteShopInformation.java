package de.minestar.buycraft.shops;

import org.bukkit.Material;

public class InfiniteShopInformation {
    private final Material material;
    private final int itemID;
    private final short subID;
    private final int elementCount;
    private final int elementSize, totalCount;
    private final InfiniteShop shop;

    public InfiniteShopInformation(InfiniteShop shop, int itemID, int elementCount, int elementSize) {
        this(shop, itemID, (short) 0, elementCount, elementSize);
    }

    public InfiniteShopInformation(InfiniteShop shop, int itemID, short subID, int elementCount, int elementSize) {
        this.shop = shop;
        this.material = Material.matchMaterial(Integer.toString(itemID));
        this.itemID = itemID;
        this.subID = subID;
        if (elementCount < 1) {
            elementCount = 1;
        }
        this.elementCount = elementCount;
        if (elementSize < 1) {
            elementSize = 1;
        }
        this.elementSize = elementSize;
        this.totalCount = this.elementCount * this.elementSize;
    }

    public Material getMaterial() {
        return material;
    }

    public int getItemID() {
        return itemID;
    }

    public short getSubID() {
        return subID;
    }

    public int getElementCount() {
        return elementCount;
    }

    public int getElementSize() {
        return elementSize;
    }

    public int getTotalCount() {
        return totalCount;
    }
}
