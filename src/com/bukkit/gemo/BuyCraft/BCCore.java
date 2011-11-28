package com.bukkit.gemo.BuyCraft;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.bukkit.gemo.utils.FlatFile;
import com.bukkit.gemo.utils.UtilPermissions;

public class BCCore extends JavaPlugin {
    public static Server server;

    private static HashMap<String, String> aliasList = new HashMap<String, String>();
    private static HashMap<Integer, String> itemListByID = new HashMap<Integer, String>();
    private static HashMap<String, Integer> itemListByName = new HashMap<String, Integer>();

    private static HashMap<String, BufferedImage> imageList;

    private static TreeMap<String, MarketArea> marketList;

    private static BCCore PluginInstance = null;

    // private final int TEXTURE_SIZE = 32;
    private final int TEXTURE_BLOCK_SIZE = 24;

    // VARIABLEN
    public BCBlockListener blockListener;
    public BCEntityListener entityListener;
    public BCPlayerListener playerListener;

    // Happy Hour Item : If -1 No Happy Hour :(
    public static int happyHourItem = -1;

    // /////////////////////////////////
    //
    // MAIN METHODS
    //
    // /////////////////////////////////

    // AUSGABE IN DER CONSOLE
    public static void printInConsole(String str) {
        // TODO Auto-generated method stub
        System.out.println("[ BuyCraft ]: " + str);
    }

    // ON DISABLE
    @Override
    public void onDisable() {
        System.out.println("BuyCraft by GeMo disabled");
    }

    // ON ENABLE
    @Override
    public void onEnable() {
        PluginInstance = this;
        server = getServer();
        loadMarkets();
        loadAliases();
        loadItems();
        PluginManager pm = getServer().getPluginManager();

        // LOAD TEXTURES
        loadTextures();

        // LISTENER REGISTRIEREN
        blockListener = new BCBlockListener(this);
        entityListener = new BCEntityListener(this);
        playerListener = new BCPlayerListener(this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener,
                Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, this.blockListener,
                Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, this.blockListener,
                Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.SIGN_CHANGE, this.blockListener,
                Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener,
                Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener,
                Event.Priority.Normal, this);

        // PluginDescriptionFile LESEN
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version "
                + pdfFile.getVersion() + " is enabled!");
    }

    // /////////////////////////////////
    //
    // LOAD TEXTURES
    //
    // /////////////////////////////////
    public void loadTextures() {
        imageList = new HashMap<String, BufferedImage>();

        File dir = new File("plugins/BuyCraft/textures/");
        if (!dir.exists())
            return;

        File[] fileList = dir.listFiles();
        for (File file : fileList) {
            if (!file.isFile())
                continue;

            if (!file.getName().endsWith(".png"))
                continue;

            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    imageList.put(
                            file.getCanonicalFile().getName()
                                    .replace(".png", ""), image);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // /////////////////////////////////
    //
    // DRAW BLOCK
    //
    // /////////////////////////////////
    private void drawBlock(Graphics2D graphic, int x, int z, int TypeID,
            int SubID) {
        String picture = "" + TypeID;
        if (TypeID == 17 || TypeID == 35 || TypeID == 43 || TypeID == 44
                || TypeID == 50 || TypeID == 63 || TypeID == 65 || TypeID == 66
                || TypeID == 68 || TypeID == 75 || TypeID == 76 || TypeID == 98) {
            picture += "-" + SubID;
        }

        BufferedImage blockTex = imageList.get(picture);
        if (blockTex != null) {
            graphic.drawImage(blockTex, x * TEXTURE_BLOCK_SIZE, z
                    * TEXTURE_BLOCK_SIZE, TEXTURE_BLOCK_SIZE,
                    TEXTURE_BLOCK_SIZE, null);
        }
        blockTex = null;
    }

    private void exportMarketPicture(MarketArea area) {
        try {
            // CREATE IMAGE
            BufferedImage image = new BufferedImage(area.getAreaBlockWidth()
                    * TEXTURE_BLOCK_SIZE, area.getAreaBlockLength()
                    * TEXTURE_BLOCK_SIZE, BufferedImage.TRANSLUCENT);

            File outputDir = new File("plugins/BuyCraft/markets/");
            outputDir.mkdir();
            File output = new File("plugins/BuyCraft/markets/"
                    + area.getAreaName() + ".png");
            output.createNewFile();

            Graphics2D graphic = (Graphics2D) image.getGraphics();
            graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // GET WORLDS
            CraftWorld cWorld = (CraftWorld) area.getCorner1().getWorld();
            net.minecraft.server.World nWorld = cWorld.getHandle();

            // PAINT AREA
            for (int x = 0; x < area.getAreaBlockWidth(); x++) {
                for (int z = 0; z < area.getAreaBlockLength(); z++) {
                    for (int y = 0; y <= area.getAreaBlockHeight(); y++) {
                        int TypeID = nWorld.getTypeId(x
                                + area.getCorner1().getBlockX(), y
                                + area.getCorner1().getBlockY(), z
                                + area.getCorner1().getBlockZ());
                        int SubID = nWorld.getData(x
                                + area.getCorner1().getBlockX(), y
                                + area.getCorner1().getBlockY(), z
                                + area.getCorner1().getBlockZ());

                        this.drawBlock(graphic, x, z, TypeID, SubID);
                    }
                }
            }

            // SAVE FILE
            ImageIO.write(image, "png", output);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // /////////////////////////////////
    //
    // SPECIAL ITEMS
    //
    // /////////////////////////////////
    public int getSubID(net.minecraft.server.World world, int x, int y, int z,
            int TypeID, int SubID) {
        return SubID;
    }

    // /////////////////////////////////
    //
    // METHODS FOR ITEMS
    //
    // /////////////////////////////////
    public void loadItems() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(
                    "plugins/BuyCraft/items.txt"));
            String zeile = null;
            while ((zeile = in.readLine()) != null) {
                String[] split = zeile.split(",");
                if (split.length > 1) {
                    try {
                        itemListByID.put(Integer.valueOf(split[1]),
                                split[0].toLowerCase());
                        itemListByName.put(split[0].toLowerCase(),
                                Integer.valueOf(split[1]));
                    }
                    catch (Exception e) {
                        printInConsole("Cannot parse: " + zeile);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            printInConsole("Fehler beim lesen der Datei: plugins/BuyCraft/items.txt");
        }
    }

    public static boolean isAllowedItem(String txt) {
        txt = txt.toLowerCase();
        try {
            return itemListByID.containsKey(Integer.valueOf(txt));
        }
        catch (Exception e) {
            return itemListByName.containsKey(txt.toLowerCase());
        }
    }

    public static int getItemId(String txt) {
        txt = txt.toLowerCase();
        try {
            if (itemListByID.containsKey(Integer.valueOf(txt))) {
                return Integer.valueOf(txt);
            }
            else {
                return -1;
            }
        }
        catch (Exception e) {
            if (itemListByName.containsKey(txt)) {
                return itemListByName.get(txt);
            }
            else {
                return -1;
            }
        }
    }

    public static boolean isItemAllowed(String itemName) {
        int ID = getItemId(itemName);
        return isItemAllowed(ID);
    }

    public static boolean isItemAllowed(int TypeID) {
        return itemListByID.containsKey(TypeID);
    }

    public static String getItemName(int TypeID) {
        if (!isItemAllowed(TypeID))
            return "";
        return itemListByID.get(TypeID);
    }

    // /////////////////////////////////
    //
    // ON COMMAND
    //
    // /////////////////////////////////
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (commandLabel.equalsIgnoreCase("buycraft")
                    && UtilPermissions
                            .playerCanUseCommand(player, "buycraft.*")) {
                if (args != null) {
                    if (args.length == 1) {
                        if (args[0].equalsIgnoreCase("listalias")) {
                            BCChatUtils.printLine(player, ChatColor.AQUA,
                                    "Derzeitige Aliasnamen:");
                            for (Entry<String, String> entry : aliasList
                                    .entrySet()) {
                                BCChatUtils.printLine(
                                        player,
                                        ChatColor.GRAY,
                                        entry.getKey() + " - Alias: "
                                                + entry.getValue());
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("activateshops")) {
                            for (BCUserShop shop : BCBlockListener.userShopList
                                    .values()) {
                                shop.setShopFinished(true);
                                shop.saveShop();
                            }
                            BCChatUtils.printLine(player, ChatColor.AQUA,
                                    BCBlockListener.userShopList.size()
                                            + " Shops updated!");
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("marketmode")) {
                            if (playerListener.getSelections().containsKey(
                                    player.getName())) {
                                playerListener.getSelections().remove(
                                        player.getName());
                                BCChatUtils.printLine(player, ChatColor.AQUA,
                                        "You are no longer in selectionmode!");
                            }
                            else {
                                playerListener.getSelections()
                                        .put(player.getName(),
                                                new MarketSelection());
                                BCChatUtils.printLine(player, ChatColor.AQUA,
                                        "You are now in selectionmode!");
                            }
                            return true;
                        }
                    }
                    if (args.length == 2) {
                        if (args[0].equalsIgnoreCase("delalias")) {
                            if (!aliasList.containsKey(args[1].toLowerCase())) {
                                BCChatUtils.printError(player, "Spieler '"
                                        + args[1] + "' hat keinen Aliasnamen!");
                                return true;
                            }
                            aliasList.remove(args[1].toLowerCase());
                            BCChatUtils.printSuccess(player,
                                    "Alias für Spieler '" + args[1]
                                            + "' entfernt!");
                            saveAliases();
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("savemarket")) {
                            if (playerListener.getSelections().containsKey(
                                    player.getName())) {
                                MarketSelection selection = playerListener
                                        .getSelections().get(player.getName());
                                if (selection.isValid()) {
                                    if (marketList.containsKey(args[1])) {
                                        BCChatUtils
                                                .printError(player,
                                                        "A market with that name already exists!");
                                    }
                                    else {
                                        MarketArea area = new MarketArea(
                                                args[1],
                                                selection.getCorner1(),
                                                selection.getCorner2());
                                        if (area.isValidArea()) {
                                            marketList.put(args[1], area);
                                            this.saveMarkets();
                                            BCChatUtils.printSuccess(player,
                                                    "Market saved as '"
                                                            + args[1] + "'!");
                                        }
                                        else
                                            BCChatUtils
                                                    .printError(player,
                                                            "Internal error while saving market!");
                                    }
                                }
                                else {
                                    BCChatUtils.printError(player,
                                            "Please select 2 Points!");

                                }
                            }
                            else {
                                BCChatUtils.printError(player,
                                        "You are not in selectionmode!");
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("exportmarket")) {
                            if (!marketList.containsKey(args[1])) {
                                BCChatUtils.printError(player, "Market '"
                                        + args[1] + "' not found!");
                            }
                            else {
                                MarketArea area = marketList.get(args[1]);
                                exportMarketPicture(area);
                            }
                            return true;
                        }
                    }
                    else if (args.length == 3) {
                        if (args[0].equalsIgnoreCase("setalias")) {
                            for (String thisName : aliasList.values()) {
                                if (thisName.equalsIgnoreCase(args[2])) {
                                    BCChatUtils
                                            .printError(player, "Alias '"
                                                    + args[2]
                                                    + "' wird schon benutzt!");
                                    return true;
                                }
                            }
                            aliasList.put(args[1].toLowerCase(), args[2]);
                            saveAliases();
                            BCChatUtils.printSuccess(player, "Alias '"
                                    + args[2] + "' für Spieler '" + args[1]
                                    + "' angelegt.");
                        }
                    }
                }
            }
        }
        return true;
    }

    // /////////////////////////////////
    //
    // ALIASES
    //
    // /////////////////////////////////

    // IS OWNER
    public static boolean isShopOwner(String playerName, String aliasName) {
        if (playerName.equalsIgnoreCase(aliasName)) {
            return true;
        }

        if (!aliasList.containsKey(playerName.toLowerCase()))
            return false;

        return aliasList.get(playerName.toLowerCase()).equalsIgnoreCase(
                aliasName);
    }

    // GET ALIAS
    public static String getAlias(String playerName) {
        if (!aliasList.containsKey(playerName.toLowerCase()))
            return playerName;

        return aliasList.get(playerName.toLowerCase());
    }

    // SAVE ALIASES
    public static void saveAliases() {
        File folder = new File("plugins/BuyCraft/aliases.bcf");
        folder.mkdirs();

        if (folder.exists()) {
            folder.delete();
        }

        try {
            ObjectOutputStream objOut = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(
                            "plugins/BuyCraft/aliases.bcf")));
            objOut.writeObject(aliasList);
            objOut.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // LOAD ALIASES
    @SuppressWarnings("unchecked")
    public static void loadAliases() {
        File folder = new File("plugins/BuyCraft/aliases.bcf");
        folder.mkdirs();
        if (!folder.exists()) {
            aliasList = new HashMap<String, String>();
            return;
        }

        try {
            ObjectInputStream objIn = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(
                            "plugins/BuyCraft/aliases.bcf")));
            aliasList = (HashMap<String, String>) objIn.readObject();
            objIn.close();
        }
        catch (Exception e) {
            aliasList = new HashMap<String, String>();
            BCCore.printInConsole("Error while reading file: plugins/BuyCraft/aliases.bcf");
        }
    }

    private void loadMarkets() {
        marketList = new TreeMap<String, MarketArea>();
        String FileName = "BuyCraft/Markets.db";
        try {
            FlatFile config = new FlatFile(FileName, false);
            if (config.readFile()) {
                String areaString = config.getString("markets", "");
                String[] areaSplit = areaString.split(",");
                for (String thisArea : areaSplit) {
                    MarketArea area = new MarketArea(thisArea);
                    if (area.isValidArea()) {
                        marketList.put(area.getAreaName(), area);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        printInConsole(marketList.size() + " Markets loaded!");
    }

    private boolean saveMarkets() {
        String FileName = "BuyCraft/Markets.db";
        File folder = new File("plugins/BuyCraft");
        folder.mkdirs();

        // SAVE MARKETS
        if (new File(FileName).exists())
            new File(FileName).delete();

        try {
            FlatFile config = new FlatFile(FileName, false);
            String areaString = "";
            for (Map.Entry<String, MarketArea> entry : marketList.entrySet()) {
                areaString += entry.getValue().exportArea() + ",";
            }
            config.setString("markets", areaString);
            config.writeFile();
            return true;
        }
        catch (IOException e) {
            printInConsole("Error while saving file: plugins/" + FileName);
            e.printStackTrace();
            return false;
        }
    }

    public static BCCore getPlugin() {
        return PluginInstance;
    }
}
