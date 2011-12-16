package com.bukkit.gemo.BuyCraft;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import net.minecraft.server.Packet130UpdateSign;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.BuyCraft.statics.Potions;
import com.bukkit.gemo.utils.UtilPermissions;

public class BCUserShop extends BCShop implements Serializable {
    private static final long serialVersionUID = 5197016944101717903L;
    private boolean isActive = false;
    private ArrayList<BCItemStack> shopInventory = null;
    private long creationTime, lastUsedTime = 0;
    private boolean shopFinished = true;

    // /////////////////////////////////
    //
    // CONSTRUCTORS
    //
    // /////////////////////////////////
    public BCUserShop() {
        super();
        shopInventory = new ArrayList<BCItemStack>();
        creationTime = System.currentTimeMillis();
        lastUsedTime = System.currentTimeMillis();
    }

    public BCUserShop(String worldName, int x, int y, int z) {
        super(worldName, x, y, z);
        shopInventory = new ArrayList<BCItemStack>();
        creationTime = System.currentTimeMillis();
        lastUsedTime = System.currentTimeMillis();
        BCCore.getPageUpdater().addMarket(this);
    }

    public String getHTML_Area(final int uniqueUserID, int posX, int posZ, int texSize) {
        StringBuilder builder = new StringBuilder();
        String uniqueName = this.getShopOwner() + "_" + uniqueUserID;
        builder.append("<area href=\"#\" onmouseover=\"toggleShops('.");
        builder.append(uniqueName);
        builder.append("')\" onmouseout=\"toggleShops('.");
        builder.append(uniqueName);
        builder.append("')\" shape=\"poly\" coords=\"");
        builder.append(posX * texSize + "," + posZ * texSize + " ,");
        builder.append((posX + 1) * texSize + "," + posZ * texSize + " ,");
        builder.append((posX + 1) * texSize + "," + (posZ + 1) * texSize + " ,");
        builder.append(posX * texSize + "," + (posZ + 1) * texSize);
        builder.append("\" title=\"" + BCCore.getItemName(this.getItemID()) + "\" name=\"" + uniqueName + "\"");
        builder.append(">");
        return builder.toString();
    }

    public String getHTML_ShopDetails(final int uniqueUserID, final int javascriptID) {
        StringBuilder builder = new StringBuilder();
        int itemID = this.getItemID();
        String matName = BCCore.getItemName(itemID);
        if (itemID == Material.POTION.getId())
            matName = Potions.getName(this.getSubID());

        short SubID = this.getSubID();
        if (SubID != 0 && itemID != Material.POTION.getId())
            matName += ":" + SubID;

        String uniqueName = this.getShopOwner() + "_" + uniqueUserID;
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t<!-- SHOPDETAILS ( ID : " + javascriptID + " ) -->");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t<div onClick=\"javascript:toggleMarker(" + javascriptID + ");\" onMouseOver=\"javascript:showMarker(" + javascriptID + ");\" onMouseOut=\"javascript:hideMarker(" + javascriptID + ");\" class=\"usershop " + BCCore.getItemName(this.getItemID()) + " " + this.getShopOwner() + " " + uniqueName + "\">");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t<div class=\"inner\">");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t\t<div class=\"user\">" + this.getShopOwner() + "</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t\t<div class=\"material\">" + matName + "</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t\t<div class=\"buyprice\">Verkaufskurs: " + this.getBuyRatio()[0] + ":" + this.getBuyRatio()[1] + "</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t\t<div class=\"sellprice\">Ankaufkurs: " + this.getSellRatio()[0] + ":" + this.getSellRatio()[1] + "</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t\t<div class=\"available\">Verf&uuml;gbar: " + this.countItemInShopInventory(this.getItemID(), this.getSubID()) + "</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t\t</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t</div>");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t<!-- END SHOPDETAILS ( ID : " + javascriptID + " ) -->");
        builder.append(System.getProperty("line.separator"));
        return builder.toString();
    }

    public String getHTML_Marker(int javascriptID, int mapX, int mapZ) {
        StringBuilder builder = new StringBuilder();

        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t// PRINT MARKER ( ID : " + javascriptID + " ) & HIDE IT");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tvar icon" + javascriptID + " = new OpenLayers.Icon('marker.png', size, offset);");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tvar pos" + javascriptID + " = new OpenLayers.Pixel(pixBottomLeft.x + 32*" + mapX + ", pixBottomLeft.y - 32*" + mapZ + ");");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tvar lonPos" + javascriptID + " = map.getLonLatFromPixel(pos" + javascriptID + ");");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tvar marker" + javascriptID + " = new OpenLayers.Marker(lonPos" + javascriptID + ", icon" + javascriptID + ");");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tmarkers.addMarker(marker" + javascriptID + ");");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tvisListForce.push(false);");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\thideMarker(" + javascriptID + ");");
        builder.append(System.getProperty("line.separator"));

        return builder.toString();
    }

    public String getHTML_PopUp(int javascriptID) {
        StringBuilder builder = new StringBuilder();

        int itemID = this.getItemID();
        String matName = BCCore.getItemName(itemID);
        if (itemID == Material.POTION.getId())
            matName = Potions.getName(this.getSubID());

        short SubID = this.getSubID();
        if (SubID != 0 && itemID != Material.POTION.getId())
            matName += ":" + SubID;

        String buyRatio = this.getBuyRatio()[0] + ":" + this.getBuyRatio()[1];
        String sellRatio = this.getSellRatio()[0] + ":" + this.getSellRatio()[1];
        int itemCount = this.countItemInShopInventory(this.getItemID(), this.getSubID());

        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\t// ADD POPUP ( ID : " + javascriptID + " ) & HIDE IT");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tvar popup" + javascriptID + " = new OpenLayers.Popup(\"popUp" + javascriptID + "\", lonPos" + javascriptID + ", new OpenLayers.Size(180,95),\"<center><b>Item:</b> " + matName + "<br><b>Verkaufskurs:</b> " + buyRatio + "<br><b>Ankaufkurs:</b> " + sellRatio + "<br><b>Verf&uuml;gbar:</b> " + itemCount + "</center>\", false);");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tmap.addPopup(popup" + javascriptID + ");");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tpopup" + javascriptID + ".setOpacity(0.75);");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tpopup" + javascriptID + ".hide();");
        builder.append(System.getProperty("line.separator"));
        builder.append("\t\t\t\tpopUpList.push(popup" + javascriptID + ");");
        builder.append(System.getProperty("line.separator"));

        matName = null;
        buyRatio = null;
        sellRatio = null;

        return builder.toString();
    }

    // /////////////////////////////////
    //
    // METHODS FOR GETTING SHOP-PROPERTIES
    //
    // /////////////////////////////////

    /**
     * getSign()
     * 
     * @return <b>The sign</b>, if the sign was found.<br/>
     *         otherwise <b>null</b>
     */
    public Sign getSign() {
        Block block = this.getBlock();
        if (block.getTypeId() != Material.WALL_SIGN.getId())
            return null;

        return ((Sign) block.getState());
    }

    /**
     * getLines()
     * 
     * @return <b>All lines</b>, if the sign was found.<br/>
     *         otherwise <b>null</b>
     */
    public String[] getLines() {
        Sign sign = getSign();
        if (sign == null)
            return null;

        return sign.getLines();
    }

    /**
     * getLine(int linenumber)
     * 
     * @param linenumber
     * @return <b>Linecontent</b>, if the sign was found.<br/>
     *         otherwise <b>null</b>
     */
    public String getLine(int linenumber) {
        Sign sign = getSign();
        if (sign == null)
            return null;

        if (linenumber < 0 || linenumber > 3)
            return null;
        return sign.getLines()[linenumber];
    }

    public String getShopOwner() {
        String line = getLine(0);
        if (line == null)
            return null;

        line = getSpecialTextOnLine(line, "$", "$");
        return line;
    }

    public int getItemID() {
        String line = getLine(1);
        if (line == null)
            return 0;

        line = getSpecialTextOnLine(line, "{", "}");
        String[] split = line.split(":");
        try {
            return BCCore.getItemId(split[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public short getSubID() {
        String line = getLine(1);
        if (line == null)
            return 0;

        line = getSpecialTextOnLine(line, "{", "}");
        String[] split = line.split(":");
        try {
            return Short.valueOf(split[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer[] getBuyRatio() {
        String line = getLine(2);
        if (line == null)
            return null;

        return BCShop.getRatios(line);
    }

    public Integer[] getSellRatio() {
        String line = getLine(3);
        if (line == null)
            return null;

        return BCShop.getRatios(line);
    }

    // /////////////////////////////////
    //
    // HANDLE LEFTCLICK
    //
    // /////////////////////////////////
    public void handleLeftClick(Player player, Sign sign, Chest chest) {

        if (!isShopFinished()) {
            String playerName = BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$");
            if (BCCore.isShopOwner(player.getName(), playerName)) {
                // GET ITEM IN HAND
                ItemStack item = player.getItemInHand();
                if (item == null) {
                    BCChatUtils.printInfo(player, ChatColor.RED, "Bitte ein Item in die Hand nehmen um den Shop fertig zu stellen.");
                    return;
                }

                // IS ITEM ALLOWED?
                int ItemID = item.getTypeId();
                short SubData = item.getDurability();
                if (!BCCore.isItemAllowed(ItemID)) {
                    BCChatUtils.printInfo(player, ChatColor.RED, "Dieses Item ist im Moment nicht erlaubt.");
                    return;
                }

                // GET ITEMNAME OR ID
                String name = BCCore.getItemName(ItemID);
                if (SubData > 0)
                    name += ":" + SubData;

                if (name.length() > 13) {
                    name = ItemID + ":" + SubData;
                }

                // UPDATE SIGN
                Sign worldSign = (Sign) (sign.getWorld().getBlockAt(sign.getBlock().getLocation()).getState());
                if (worldSign == null) {
                    BCChatUtils.printInfo(player, ChatColor.GREEN, "Fehler beim Update des Schildes. Bitte wende dich an einen Admin.");
                    return;
                }
                worldSign.setLine(1, "{" + name + "}");
                worldSign.update();                             

                // SEND UPDATE => NEED HELP OF ORIGINAL MC-SERVERSOFTWARE
                CraftPlayer cPlayer = (CraftPlayer) player;
                Packet130UpdateSign signPacket = null;
                signPacket = new Packet130UpdateSign(sign.getX(), sign.getY(), sign.getZ(), sign.getLines());
                cPlayer.getHandle().netServerHandler.sendPacket(signPacket);

                // SAVE SHOP
                setShopFinished(true);
                sign = worldSign;

                // PRINT SUCCESS
                saveShop();
                BCChatUtils.printInfo(player, ChatColor.GREEN, "Das Item wurde erfolgreich gesetzt.");
                return;
            } else {
                // NOT THE SHOPOWNER
                BCChatUtils.printInfo(player, ChatColor.RED, "Dieser Shop ist noch nicht fertiggestellt.");
                return;
            }
        }

        String[] itemSplit = BCShop.getSplit(BCShop.getSpecialTextOnLine(sign.getLine(1), "{", "}"));
        Integer[] buyRatios = BCShop.getRatios(sign.getLine(2));
        Integer[] sellRatios = buyRatios;
        if (sign.getLine(3).length() > 0)
            sellRatios = BCShop.getRatios(sign.getLine(3));

        int sellItemId = 0;
        short sellItemData = 0;

        try {
            sellItemId = BCCore.getItemId(itemSplit[0]);
            sellItemData = Short.valueOf(itemSplit[1]);
        } catch (Exception e) {
            System.out.println("LOCATION: " + sign.getBlock().getLocation().toString());
            e.printStackTrace();
            return;
        }

        if (!BCCore.isAllowedItem(itemSplit[0]))
            return;

        if (!isActive()) {
            String playerName = BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$");
            BCChatUtils.printError(player, "Dieser Shop ist momentan nicht aktiviert!");
            if (!BCCore.isShopOwner(player.getName(), playerName))
                BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte gib dem Shop-Besitzer bescheid.");
            else
                BCChatUtils.printInfo(player, ChatColor.GRAY, "Rechtsklick auf das Schild um ihn zu aktiveren.");
            return;
        }

        String itemName = Material.getMaterial(sellItemId).name();
        if (sellItemId == Material.POTION.getId())
            itemName = Potions.getName(sellItemData);

        if (buyRatios[0] > 0 && buyRatios[1] > 0)
            BCChatUtils.printInfo(player, ChatColor.GOLD, "KAUFEN: " + buyRatios[0] + " '" + itemName + "' für " + buyRatios[1] + " Goldbarren. (Auf Lager: " + this.countItemInShopInventory(sellItemId, sellItemData) + ")");
        else
            BCChatUtils.printInfo(player, ChatColor.GOLD, "Dieser Shop verkauft nichts.");
        if (sellRatios[0] > 0 && sellRatios[1] > 0)
            BCChatUtils.printInfo(player, ChatColor.GOLD, "VERKAUFEN: " + sellRatios[0] + " '" + itemName + "' für " + sellRatios[1] + " Goldbarren.");
        else
            BCChatUtils.printInfo(player, ChatColor.GOLD, "Dieser Shop kauft nichts an.");
    }
    // /////////////////////////////////
    //
    // HANDLE RIGHTCLICK
    //
    // /////////////////////////////////
    public void handleRightClick(Player player, Sign sign, Chest chest) {
        if (!isShopFinished()) {
            BCChatUtils.printInfo(player, ChatColor.RED, "Dieser Shop ist noch nicht fertiggestellt.");
            return;
        }

        String playerName = BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$");
        if (BCCore.isShopOwner(player.getName(), playerName)) {
            /** IF PLAYER IS THE SHOPOWNER */
            if (isActive()) {
                /** DEACTIVATE SHOP */
                restoreInventory(chest);
            } else {
                /** ACTIVATE SHOP */
                shopInventory.clear();
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item != null) {
                        if (item.getTypeId() > 0) {
                            shopInventory.add(new BCItemStack(item.getTypeId(), item.getDurability(), item.getAmount()));
                        }
                    }
                }
                chest.getInventory().clear();
            }
            setActive(!isActive());
            if (isActive()) {
                setCreationTime(System.currentTimeMillis());
                player.sendMessage(ChatColor.DARK_AQUA + "Der Shop ist jetzt " + ChatColor.GREEN + "aktiviert" + ChatColor.DARK_AQUA + ".");
            } else {
                player.sendMessage(ChatColor.DARK_AQUA + "Der Shop ist jetzt " + ChatColor.RED + "deaktiviert" + ChatColor.DARK_AQUA + ".");
            }
            saveShop();
            BCCore.getPageUpdater().addMarket(this);
        } else {
            /** IF PLAYER IS NOT THE SHOPOWNER = BUY / SELL */
            if (isActive()) {
                String[] itemSplit = BCShop.getSplit(BCShop.getSpecialTextOnLine(sign.getLine(1), "{", "}"));
                Integer[] buyRatios = BCShop.getRatios(sign.getLine(2));
                Integer[] sellRatios = buyRatios;
                if (sign.getLine(3).length() > 0)
                    sellRatios = BCShop.getRatios(sign.getLine(3));

                int sellItemId = 0;
                short sellItemData = 0;

                try {
                    sellItemId = BCCore.getItemId(itemSplit[0]);
                    sellItemData = Short.valueOf(itemSplit[1]);
                } catch (Exception e) {
                    System.out.println("LOCATION: " + sign.getBlock().getLocation().toString());
                    e.printStackTrace();
                    return;
                }

                if (!BCCore.isAllowedItem(itemSplit[0]))
                    return;

                int sellItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), sellItemId, sellItemData);
                int nuggetItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_NUGGET.getId());
                int goldIngotItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_INGOT.getId());
                int goldBlockItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_BLOCK.getId());
                nuggetItemCountInChest = nuggetItemCountInChest + (9 * goldIngotItemCountInChest) + (9 * 9 * goldBlockItemCountInChest);

                // ////////////////////////////
                // CATCH OTHER/WRONG ITEMS
                // ////////////////////////////
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item == null)
                        continue;

                    if (item.getTypeId() < 1)
                        continue;

                    String itemName = Material.getMaterial(sellItemId).name();
                    if (sellItemId == Material.POTION.getId())
                        itemName = Potions.getName(sellItemData);

                    if ((item.getTypeId() != sellItemId || item.getDurability() != sellItemData) && item.getTypeId() != Material.GOLD_INGOT.getId() && item.getTypeId() != Material.GOLD_NUGGET.getId() && item.getTypeId() != Material.GOLD_BLOCK.getId()) {
                        if (buyRatios[0] > 0 && buyRatios[1] > 0)
                            BCChatUtils.printInfo(player, ChatColor.GOLD, "KAUFEN: " + buyRatios[0] + " '" + itemName + "' für " + buyRatios[1] + " Goldbarren. (Auf Lager: " + this.countItemInShopInventory(sellItemId, sellItemData) + ")");
                        if (sellRatios[0] > 0 && sellRatios[1] > 0)
                            BCChatUtils.printInfo(player, ChatColor.GOLD, "VERKAUFEN: " + sellRatios[0] + " '" + itemName + "' für " + sellRatios[1] + " Goldbarren.");
                        return;
                    }
                }

                // ////////////////////////////
                // CATCH SELL & BUY
                // ////////////////////////////
                if (sellItemCountInChest == 0 && nuggetItemCountInChest == 0) {
                    if (buyRatios[0] > 0 && buyRatios[1] > 0)
                        BCChatUtils.printInfo(player, ChatColor.GOLD, "KAUFEN: " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' für " + buyRatios[1] + " Goldbarren. (Auf Lager: " + this.countItemInShopInventory(sellItemId, sellItemData) + ")");
                    if (sellRatios[0] > 0 && sellRatios[1] > 0)
                        BCChatUtils.printInfo(player, ChatColor.GOLD, "VERKAUFEN: " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' für " + sellRatios[1] + " Goldbarren.");
                    return;
                }
                if (sellItemCountInChest > 0 && nuggetItemCountInChest > 0) {
                    BCChatUtils.printError(player, "Du kannst nur kaufen ODER verkaufen, nicht beides zugleich.");
                    return;
                }

                // ////////////////////////////
                // SELL / BUY ITEMS
                // ////////////////////////////
                if (nuggetItemCountInChest > sellItemCountInChest) {
                    if (buyRatios[0] > 0 && buyRatios[1] > 0) {
                        /** CHECK PERMISSION */
                        if (!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.buy." + Material.getMaterial(sellItemId).name().toLowerCase())) {
                            BCChatUtils.printError(player, "Du darfst kein '" + Material.getMaterial(sellItemId).name() + "' kaufen.");
                            return;
                        }

                        /** BUY ITEMS */
                        float blockPerNugget = (float) ((float) buyRatios[0] / (float) buyRatios[1] / 9.0f);
                        double bBlocks = Math.floor(blockPerNugget * nuggetItemCountInChest);
                        int boughtBlocks = (int) bBlocks;
                        double ratio = ((double) buyRatios[0] / (double) buyRatios[1]);
                        int restGoldNuggets = (int) (nuggetItemCountInChest - (boughtBlocks / ratio * 9));

                        // AT LEAST ONE BLOCK MUST BE BOUGHT
                        if (boughtBlocks < 1) {
                            BCChatUtils.printError(player, "Du bekommst keine Items für " + nuggetItemCountInChest + " Goldnuggets.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte mehr Gold in die Kiste legen.");
                            return;
                        }

                        // ENOUGH ITEMS IN INVENTORY
                        if (!hasAmountOfItem(sellItemId, sellItemData, boughtBlocks)) {
                            BCChatUtils.printError(player, "Dieser Shop hat nur noch " + countItemInShopInventory(sellItemId, sellItemData) + " '" + Material.getMaterial(sellItemId).name() + "'.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Du hast versucht " + boughtBlocks + " zu kaufen.");
                            return;
                        }

                        // MORE BLOCKS THAN INVENTORYSIZE?
                        if (boughtBlocks > 27 * 64) {
                            BCChatUtils.printError(player, "Du kannst nur maximal " + (27 * 64) + " Items auf einmal kaufen.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Du hast versucht " + boughtBlocks + " Items zu kaufen.");
                            return;
                        }

                        // GET NEW SHOPINVENTORY
                        int blockCount = countItemInShopInventory(sellItemId, sellItemData);
                        int goldNuggetCount = nuggetItemCountInChest - restGoldNuggets + countItemInShopInventory(Material.GOLD_NUGGET.getId(), (byte) 0);
                        int goldIngotCount = countItemInShopInventory(Material.GOLD_INGOT.getId(), (byte) 0);
                        int goldBlockCount = countItemInShopInventory(Material.GOLD_BLOCK.getId(), (byte) 0);
                        goldNuggetCount = goldNuggetCount + goldIngotCount * 9 + goldBlockCount * 9 * 9;
                        blockCount = blockCount - boughtBlocks;

                        goldIngotCount = (int) Math.floor(goldNuggetCount / 9);
                        goldNuggetCount = goldNuggetCount - goldIngotCount * 9;
                        goldBlockCount = (int) Math.floor(goldIngotCount / 9);
                        goldIngotCount = goldIngotCount - goldBlockCount * 9;

                        // GET STACKCOUNTS
                        int stacksizeGoldNugget = (int) Math.ceil((float) goldNuggetCount / 64);
                        int stacksizeGoldIngot = (int) Math.ceil((float) goldIngotCount / 64);
                        int stacksizeGoldBlock = (int) Math.ceil((float) goldBlockCount / 64);
                        int stacksizeItem = (int) Math.ceil((float) blockCount / 64);

                        // MORE BLOCKS THAN INVENTORYSIZE?
                        if (stacksizeGoldNugget + stacksizeGoldIngot + stacksizeGoldBlock + stacksizeItem > 27) {
                            BCChatUtils.printError(player, "Die Kisten des Usershops sind überfüllt.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte gib dem Shop-Besitzer bescheid.");
                            return;
                        }

                        // UPDATE SHOPINVENTORY
                        shopInventory = new ArrayList<BCItemStack>();
                        if (goldNuggetCount > 0)
                            shopInventory.add(new BCItemStack(Material.GOLD_NUGGET.getId(), (byte) 0, goldNuggetCount));
                        if (goldIngotCount > 0)
                            shopInventory.add(new BCItemStack(Material.GOLD_INGOT.getId(), (byte) 0, goldIngotCount));
                        if (goldBlockCount > 0)
                            shopInventory.add(new BCItemStack(Material.GOLD_BLOCK.getId(), (byte) 0, goldBlockCount));
                        if (blockCount > 0)
                            shopInventory.add(new BCItemStack(sellItemId, sellItemData, blockCount));

                        // CLEAR INVENTORY
                        chest.getInventory().clear();

                        // ADD ITEM
                        ItemStack newItem = new ItemStack(sellItemId, boughtBlocks);
                        if (sellItemData > 0)
                            newItem.setDurability(sellItemData);
                        chest.getInventory().addItem(newItem);

                        // ADD RESTGOLD
                        int restSave = restGoldNuggets;
                        if (restGoldNuggets > 0) {
                            int restIngotCount = (int) Math.floor(restGoldNuggets / 9);
                            restGoldNuggets = restGoldNuggets - restIngotCount * 9;
                            int restBlockCount = (int) Math.floor(restIngotCount / 9);
                            restIngotCount = restIngotCount - restBlockCount * 9;

                            if (restGoldNuggets > 0) {
                                ItemStack restNuggets = new ItemStack(Material.GOLD_NUGGET.getId(), restGoldNuggets);
                                chest.getInventory().addItem(restNuggets);
                            }
                            if (restIngotCount > 0) {
                                ItemStack restIngots = new ItemStack(Material.GOLD_INGOT.getId(), restIngotCount);
                                chest.getInventory().addItem(restIngots);
                            }
                            if (restBlockCount > 0) {
                                ItemStack restBlocks = new ItemStack(Material.GOLD_BLOCK.getId(), restBlockCount);
                                chest.getInventory().addItem(restBlocks);
                            }
                        }

                        // SAVE SHOP
                        setLastUsedTime(System.currentTimeMillis());
                        saveShop();

                        int usedNuggets = nuggetItemCountInChest - restSave;
                        int usedIngots = (int) Math.floor(usedNuggets / 9);
                        usedNuggets = usedNuggets - (usedIngots * 9);
                        int usedBlocks = (int) Math.floor(usedIngots / 9);
                        usedIngots = usedIngots - usedBlocks * 9;

                        // PRINT INFO
                        String text = "";
                        if (usedNuggets > 0) {
                            text += usedNuggets + " Goldnuggets";
                        }
                        if (usedIngots > 0) {
                            if (!text.equalsIgnoreCase(""))
                                text += ", ";
                            text += usedIngots + " Goldbarren";
                        }
                        if (usedBlocks > 0) {
                            if (!text.equalsIgnoreCase(""))
                                text += ", ";
                            text += usedBlocks + " Goldblöcke";
                        }

                        BCChatUtils.printInfo(player, ChatColor.GOLD, "Du hast " + boughtBlocks + " x '" + Material.getMaterial(sellItemId) + "' für " + text + " gekauft.");
                        BCCore.getPageUpdater().addMarket(this);
                        return;
                    } else {
                        BCChatUtils.printInfo(player, ChatColor.RED, "Dieser Shop verkauft nichts.");
                        return;
                    }
                } else {
                    if (sellRatios[0] > 0 && sellRatios[1] > 0) {
                        /** CHECK PERMISSION */
                        if (!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.sell." + Material.getMaterial(sellItemId).name().toLowerCase())) {
                            BCChatUtils.printError(player, "Du darfst kein '" + Material.getMaterial(sellItemId).name() + "' verkaufen.");
                            return;
                        }
                        /** SELL ITEMS */
                        float blocksPerNugget = (float) ((float) sellRatios[0] / (float) sellRatios[1] / 9.0f);
                        float bNuggets = (float) ((float) sellItemCountInChest / (float) blocksPerNugget);
                        int boughtNuggets = (int) (Math.floor(bNuggets));
                        int goldIngotCount = (int) Math.floor(boughtNuggets / 9);
                        int goldNuggetCount = boughtNuggets - goldIngotCount * 9;
                        int goldBlockCount = (int) Math.floor(goldIngotCount / 9);
                        goldIngotCount = goldIngotCount - goldBlockCount * 9;

                        // AT LEAST ONE NUGGET/INGOT/BLOCK IS NEEDED
                        if (goldNuggetCount < 1 && goldIngotCount < 1 && goldBlockCount < 1) {
                            BCChatUtils.printError(player, "Für " + sellItemCountInChest + " x '" + Material.getMaterial(sellItemId).name().toLowerCase() + "' bekommst du keine Goldnuggets.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte mehr Items in die Kiste legen.");
                            return;
                        }

                        // ENOUGH GOLD IN INVENTORY
                        int nuggetsInShop = countItemInShopInventory(Material.GOLD_NUGGET.getId(), Short.valueOf("0"));
                        nuggetsInShop += countItemInShopInventory(Material.GOLD_INGOT.getId(), Short.valueOf("0")) * 9;
                        nuggetsInShop += countItemInShopInventory(Material.GOLD_BLOCK.getId(), Short.valueOf("0")) * 9 * 9;
                        if (boughtNuggets > nuggetsInShop) {
                            BCChatUtils.printError(player, "Die Shop hat nur noch " + nuggetsInShop + " Goldnuggets.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Du hast versucht " + boughtNuggets + " Goldnuggets zu bekommen.");
                            return;
                        }

                        // GET NEW COUNT FOR SHOP
                        int newNuggetsInShop = nuggetsInShop - boughtNuggets;
                        int newItemsInShop = sellItemCountInChest + countItemInShopInventory(sellItemId, sellItemData);

                        // GET ITEMCOUNTS FOR SOLD ITEMS
                        int sellGoldIngotCount = (int) Math.floor(boughtNuggets / 9);
                        int sellGoldNuggetCount = boughtNuggets - sellGoldIngotCount * 9;
                        int sellGoldBlockCount = (int) Math.floor(sellGoldIngotCount / 9);
                        sellGoldIngotCount = sellGoldIngotCount - sellGoldBlockCount * 9;

                        // GET STACKCOUNTS FOR CHEST INVENTORY
                        int stacksizeChestGoldNugget = (int) Math.ceil((float) sellGoldNuggetCount / 64);
                        int stacksizeChestGoldIngot = (int) Math.ceil((float) sellGoldIngotCount / 64);
                        int stacksizeChestGoldBlock = (int) Math.ceil((float) sellGoldBlockCount / 64);

                        // GET ITEMCOUNTS FOR SHOPINVENTORY
                        int shopGoldIngotCount = (int) Math.floor(newNuggetsInShop / 9);
                        int shopGoldNuggetCount = newNuggetsInShop - shopGoldIngotCount * 9;
                        int shopGoldBlockCount = (int) Math.floor(shopGoldIngotCount / 9);
                        shopGoldIngotCount = shopGoldIngotCount - shopGoldBlockCount * 9;

                        // GET STACKCOUNTS FOR SOLD SHOP INVENTORY
                        int stacksizeShopGoldNugget = (int) Math.ceil((float) shopGoldNuggetCount / 64);
                        int stacksizeShopGoldIngot = (int) Math.ceil((float) shopGoldIngotCount / 64);
                        int stacksizeShopGoldBlock = (int) Math.ceil((float) shopGoldBlockCount / 64);
                        int stacksizeShopItem = (int) Math.ceil((float) newItemsInShop / 64);

                        // MORE BLOCKS THAN SHOP-INVENTORYSIZE?
                        if (stacksizeShopItem + stacksizeShopGoldNugget + stacksizeShopGoldIngot + stacksizeShopGoldBlock > 27) {
                            BCChatUtils.printError(player, "Die Kisten des Usershops sind überfüllt.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte gib dem Shop-Besitzer bescheid.");
                            return;
                        }

                        // MORE BLOCKS THAN CHEST-INVENTORYSIZE?
                        if (stacksizeChestGoldNugget + stacksizeChestGoldIngot + stacksizeChestGoldBlock > 27) {
                            BCChatUtils.printError(player, "Du hast versucht zu viel auf einmal zu verkaufen.");
                            BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte entferne ein paar Items oder gib dem Shop-Besitzer bescheid.");
                            return;
                        }

                        // UPDATE SHOPINVENTORY
                        shopInventory = new ArrayList<BCItemStack>();
                        if (shopGoldNuggetCount > 0)
                            shopInventory.add(new BCItemStack(Material.GOLD_NUGGET.getId(), (byte) 0, shopGoldNuggetCount));
                        if (shopGoldIngotCount > 0)
                            shopInventory.add(new BCItemStack(Material.GOLD_INGOT.getId(), (byte) 0, shopGoldIngotCount));
                        if (shopGoldBlockCount > 0)
                            shopInventory.add(new BCItemStack(Material.GOLD_BLOCK.getId(), (byte) 0, shopGoldBlockCount));
                        if (newItemsInShop > 0)
                            shopInventory.add(new BCItemStack(sellItemId, sellItemData, newItemsInShop));

                        // CLEAR INVENTORY
                        chest.getInventory().clear();

                        // ADD GOLD TO CHEST
                        if (sellGoldNuggetCount > 0) {
                            ItemStack newGoldNugget = new ItemStack(Material.GOLD_NUGGET.getId(), sellGoldNuggetCount);
                            chest.getInventory().addItem(newGoldNugget);
                        }
                        if (sellGoldIngotCount > 0) {
                            ItemStack newGoldIngot = new ItemStack(Material.GOLD_INGOT.getId(), sellGoldIngotCount);
                            chest.getInventory().addItem(newGoldIngot);
                        }
                        if (sellGoldBlockCount > 0) {
                            ItemStack newGoldBlock = new ItemStack(Material.GOLD_BLOCK.getId(), sellGoldBlockCount);
                            chest.getInventory().addItem(newGoldBlock);
                        }

                        // PRINT INFO
                        String text = "";
                        if (sellGoldNuggetCount > 0) {
                            text += goldNuggetCount + " Goldnuggets";
                        }
                        if (sellGoldIngotCount > 0) {
                            if (!text.equalsIgnoreCase(""))
                                text += ", ";
                            text += goldIngotCount + " Goldbarren";
                        }
                        if (sellGoldBlockCount > 0) {
                            if (!text.equalsIgnoreCase(""))
                                text += ", ";
                            text += goldBlockCount + " Goldblöcke";
                        }
                        setLastUsedTime(System.currentTimeMillis());
                        saveShop();
                        BCChatUtils.printInfo(player, ChatColor.GOLD, "Du hast " + sellItemCountInChest + " x '" + Material.getMaterial(sellItemId) + "' für " + text + " verkauft.");
                        BCCore.getPageUpdater().addMarket(this);
                        return;
                    } else {
                        BCChatUtils.printInfo(player, ChatColor.RED, "Dieser Shop kauft nichts an.");
                        return;
                    }
                }
            } else {
                BCChatUtils.printError(player, "Dieser Shop ist momentan nicht aktiviert!");
                BCChatUtils.printInfo(player, ChatColor.GRAY, "Bitte gib dem Shop-Besitzer bescheid.");
                return;
            }
        }
    }
    // /////////////////////////////////
    //
    // METHODS FOR REAL INVENTORY
    //
    // /////////////////////////////////
    public void restoreInventory(Chest chest) {
        // DROP OLD INVENTORY
        for (ItemStack item : chest.getInventory().getContents()) {
            if (item != null) {
                if (item.getTypeId() > 0 && item.getAmount() > 0) {
                    chest.getWorld().dropItem(chest.getBlock().getRelative(BlockFace.UP).getLocation(), item.clone());
                }
            }
        }
        // CLEAR INVENTORY
        chest.getInventory().clear();

        // RESTORE SHOPINVENTORY
        for (BCItemStack item : shopInventory) {
            if (item.getItem() != null) {
                if (item.getId() > 0) {
                    chest.getInventory().addItem(item.getItem());
                }
            }
        }
        shopInventory.clear();
    }

    public void updateInventory2(int itemID, short SubID, int updateAmount) {
        boolean found = false;

        // UPDATE ITEMSTACK
        for (BCItemStack item : shopInventory) {
            if (item.getItem() != null) {
                if (item.getId() == itemID && item.getSubId() == SubID) {
                    item.setAmount(item.getAmount() + updateAmount);
                    found = true;
                    break;
                }
            }
        }

        // DELETE ITEMSTACKS WITH AMOUNT < 1
        for (int i = shopInventory.size() - 1; i >= 0; i--) {
            if (shopInventory.get(i).getAmount() < 1) {
                shopInventory.remove(i);
            }
        }

        // ADD ITEMSTACK, IF NOT FOUND
        if (!found && updateAmount > 0) {
            shopInventory.add(new BCItemStack(itemID, SubID, updateAmount));
            found = true;
        }
    }

    // HAS ITEMS IN SHOPINVENTORY
    public boolean hasAmountOfItem(int itemID, short SubID, int count) {
        return countItemInShopInventory(itemID, SubID) >= count;
    }

    // COUNT ITEM IN SHOPINVENTORY
    public int countItemInShopInventory(int itemID, int SubID) {
        int count = 0;
        for (BCItemStack item : shopInventory) {
            if (item.getItem() != null) {
                if (item.getId() == itemID && item.getSubId() == SubID) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

    public int countShopInventory() {
        int count = 0;
        for (BCItemStack item : shopInventory) {
            if (item.getItem() != null) {
                if (item.getAmount() > 0)
                    count += item.getAmount();
            }
        }
        return count;
    }

    // /////////////////////////////////
    //
    // SAVE SHOP
    //
    // /////////////////////////////////
    public void saveShop() {
        File folder = new File("plugins/BuyCraft/UserShops/" + toString() + ".bcf");
        folder.mkdirs();

        if (folder.exists()) {
            folder.delete();
        }

        try {
            ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/BuyCraft/UserShops/" + toString() + ".bcf")));
            objOut.writeObject(this);
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // /////////////////////////////////
    //
    // GETTER & SETTER
    //
    // /////////////////////////////////
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public boolean isShopFinished() {
        return shopFinished;
    }

    public void setShopFinished(boolean shopFinished) {
        this.shopFinished = shopFinished;
    }
}
