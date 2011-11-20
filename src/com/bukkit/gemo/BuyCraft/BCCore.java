package com.bukkit.gemo.BuyCraft;

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
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import com.bukkit.gemo.utils.UtilPermissions;

public class BCCore extends JavaPlugin
{
    public static Server server;
      
    private static HashMap<String, String> aliasList = new HashMap<String, String>();
    private static HashMap<Integer, String> itemListByID = new HashMap<Integer, String>();
    private static HashMap<String, Integer> itemListByName = new HashMap<String, Integer>();
    
    // VARIABLEN
	public BCBlockListener blockListener;
	public BCEntityListener entityListener;
	public BCPlayerListener playerListener;
	///////////////////////////////////
	//
	// MAIN METHODS
	//
	///////////////////////////////////	
	
	// AUSGABE IN DER CONSOLE
	public static void printInConsole(String str)
	{
		// TODO Auto-generated method stub
		System.out.println("[ BuyCraft ]: " + str);
	}
	
	// ON DISABLE
	@Override
	public void onDisable()
	{
		System.out.println("BuyCraft by GeMo disabled");
	}
	
	// ON ENABLE
	@Override
	public void onEnable()
	{
		server = getServer();
		loadAliases();
		loadItems();
		PluginManager pm = getServer().getPluginManager();		
		
		// LISTENER REGISTRIEREN
		blockListener = new BCBlockListener(this);	
		entityListener = new BCEntityListener(this);
		playerListener = new BCPlayerListener(this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, 			this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, 	this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, 	this.blockListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, 			this.blockListener, 	Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, 		this.entityListener, 	Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, 		this.playerListener, 	Event.Priority.Normal, this);
		 
	    // PluginDescriptionFile LESEN
	    PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}

	///////////////////////////////////
	//
	// METHODS FOR ITEMS
	//
	///////////////////////////////////	
	public void loadItems()
	{
		try
		{        	
			BufferedReader in = new BufferedReader(new FileReader("plugins/BuyCraft/items.txt"));
			String zeile = null;
			while ((zeile = in.readLine()) != null)
			{
				String[] split = zeile.split(",");
				if(split.length > 1)
				{
					try
					{
						itemListByID.put(Integer.valueOf(split[1]), split[0].toLowerCase());
						itemListByName.put(split[0].toLowerCase(), Integer.valueOf(split[1]));
					}
					catch(Exception e)
					{
						printInConsole("Cannot parse: " + zeile); 						
					}
				}
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			printInConsole("Fehler beim lesen der Datei: plugins/BuyCraft/items.txt");  			
		}  
	}
	
	public static boolean isAllowedItem(String txt)
	{
		txt = txt.toLowerCase();
		try
		{
			return itemListByID.containsKey(Integer.valueOf(txt));
		}
		catch(Exception e)
		{
			return itemListByName.containsKey(txt.toLowerCase());
		}
	}
	
	public static int getItemId(String txt)
	{
		txt = txt.toLowerCase();
		try
		{
			if(itemListByID.containsKey(Integer.valueOf(txt)))
			{
				return Integer.valueOf(txt);
			}
			else
			{
				return -1;
			}
		}
		catch(Exception e)
		{
			if(itemListByName.containsKey(txt))
			{
				return itemListByName.get(txt);
			}
			else
			{
				return -1;
			}
		}
	}
		
	///////////////////////////////////
	//
	// ON COMMAND
	//
	///////////////////////////////////	
	@Override
	public boolean onCommand(CommandSender sender, Command command,String commandLabel, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			if(commandLabel.equalsIgnoreCase("buycraft") && UtilPermissions.playerCanUseCommand(player, "buycraft.*"))
			{
				if(args != null)
				{
					if(args.length == 1)
					{
						if(args[0].equalsIgnoreCase("listalias"))
						{
							BCChatUtils.printLine(player, ChatColor.AQUA, "List of aliases:");
							for(Entry<String, String> entry : aliasList.entrySet())
							{
								BCChatUtils.printLine(player, ChatColor.GRAY, entry.getKey() + " - Alias: " + entry.getValue());
							}
							return true;
						}
					}
					if(args.length == 2)
					{
						if(args[0].equalsIgnoreCase("delalias"))
						{
							if(!aliasList.containsKey(args[1].toLowerCase()))
							{
								BCChatUtils.printError(player, "Player '"+args[1]+"' had no alias!");
								return true;
							}
							aliasList.remove(args[1].toLowerCase());
							BCChatUtils.printSuccess(player, "Alias for Player '"+args[1]+"' removed!");
							saveAliases();
							return true;
						}
					}
					else if(args.length == 3)
					{
						if(args[0].equalsIgnoreCase("setalias"))
						{
							for(String thisName : aliasList.values())
							{
								if(thisName.equalsIgnoreCase(args[2]))
								{
									BCChatUtils.printError(player, "Alias '"+args[2]+"' is already taken!");
									return true;
								}
							}
							aliasList.put(args[1].toLowerCase(), args[2]);
							saveAliases();
							BCChatUtils.printSuccess(player, "Alias '" + args[2] + "' created for player '" + args[1] + "'.");
						}
					}
				}
		    }	
		}
		return true;
	}
	
	///////////////////////////////////
	//
	// ALIASES
	//
	///////////////////////////////////	
	
	// IS OWNER
	public static boolean isShopOwner(String playerName, String aliasName)
	{
		if(playerName.equalsIgnoreCase(aliasName))
		{
			return true;
		}
		
		if(!aliasList.containsKey(playerName.toLowerCase()))
			return false;
		
		return aliasList.get(playerName.toLowerCase()).equalsIgnoreCase(aliasName);
	}
	
	// GET ALIAS
	public static String getAlias(String playerName)
	{
		if(!aliasList.containsKey(playerName.toLowerCase()))
			return playerName;
		
		return aliasList.get(playerName.toLowerCase());
	}		
	
	// SAVE ALIASES
	public static void saveAliases()
	{
		File folder = new File("plugins/BuyCraft/aliases.bcf");
        folder.mkdirs();
        
		if(folder.exists())
		{
			folder.delete();
		}
        
		try
		{
			ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/BuyCraft/aliases.bcf")));
			objOut.writeObject(aliasList);
			objOut.close(); 
		}
		catch (IOException e) 
		{
			e.printStackTrace();    		
		} 
	}
	
	// LOAD ALIASES
	@SuppressWarnings("unchecked")
	public static void loadAliases()
	{
		File folder = new File("plugins/BuyCraft/aliases.bcf");
        folder.mkdirs();        
		if(!folder.exists())
		{
			aliasList = new HashMap<String, String>();
			return;
		}
		
		try		
		{
			ObjectInputStream objIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream("plugins/BuyCraft/aliases.bcf")));
			aliasList =(HashMap<String, String>) objIn.readObject();		
			objIn.close(); 							
		}
		catch (Exception e) 
		{
			aliasList = new HashMap<String, String>();
			BCCore.printInConsole("Error while reading file: plugins/BuyCraft/aliases.bcf");  
		} 
	}
}
