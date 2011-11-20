package com.bukkit.gemo.BuyCraft;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import com.bukkit.gemo.utils.BlockUtils;
import com.bukkit.gemo.utils.SignUtils;
import com.bukkit.gemo.utils.UtilPermissions;

public class BCBlockListener extends BlockListener
{
	public static HashMap<String, BCUserShop> userShopList;
	
	///////////////////////////////
	//
	// CONSTRUCTOR
	//
	///////////////////////////////
	public BCBlockListener(BCCore plugin)
	{
		userShopList = new HashMap<String, BCUserShop>();
		BCBlockListener.loadUserShops();
	}

	///////////////////////////////
	//
	// BLOCKLOCATION TO STRING
	//
	///////////////////////////////
	public static String BlockToString(Block block)
	{
		return block.getWorld().getName() + "__" + block.getX() + "_" + block.getY() + "_" + block.getZ(); 
	}
	
	///////////////////////////////
	//
	// GET RELATIVE SIGN
	//
	///////////////////////////////
	public static Sign getRelativeSign(Block block)
	{			
		if(block.getRelative(0, 1, 0).getTypeId() == Material.WALL_SIGN.getId())
		{
			/** 	NORMAL SHOP		*/
			Sign sign = (Sign)block.getRelative(0, 1, 0).getState();			
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
					|| userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				return sign;
			}
		}
		else
		{
			/** 	NEW SHOPTYPE	*/
			Block anchor = block.getRelative(0, 2, 0);
			
			if(anchor.getRelative(+1, 0, 0).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(+1, 0, 0).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return sign;
				}
			}
			else if(anchor.getRelative(-1, 0, 0).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(-1, 0, 0).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return sign;
				}
			}
			if(anchor.getRelative(0, 0, +1).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(0, 0, +1).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return sign;
				}
			}
			if(anchor.getRelative(0, 0, -1).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(0, 0, -1).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return sign;
				}
			}
		}		
		return null;
	}
	
	///////////////////////////////
	//
	// GET RELATIVE CHEST
	//
	///////////////////////////////
	public static Chest getRelativeChest(Block block)
	{
		Chest chest = null;		
		if(block.getRelative(0, -1, 0).getTypeId() == Material.CHEST.getId())
		{
			/** 	NORMAL SHOP		*/
			if(BlockUtils.isDoubleChest(block.getRelative(0, -1, 0)) != null)
				return null;		
			
			return (Chest)block.getRelative(0, -1, 0).getState();
		}
		else
		{
			/** 	NEW SHOPTYPE	*/
			Block anchor = SignUtils.getSignAnchor((Sign)block.getState()).getBlock();
			if(anchor.getRelative(0, -2, 0).getTypeId() == Material.CHEST.getId())
			{
				if(BlockUtils.isDoubleChest(anchor.getRelative(0, -2, 0)) != null)
					return null;		
				
				return (Chest)anchor.getRelative(0, -2, 0).getState();
			}
		}				
		return chest;
	}
	
	///////////////////////////////
	//
	// IS SIGNANCHOR
	//
	///////////////////////////////
	public static boolean isSignAnchor(Block block)
	{
		if(block.getRelative(+1, 0, 0).getTypeId() == Material.WALL_SIGN.getId())
		{
			Sign sign = (Sign)block.getRelative(+1, 0, 0).getState();			
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
					|| userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				return true;
			}
		}
		else if(block.getRelative(-1, 0, 0).getTypeId() == Material.WALL_SIGN.getId())
		{
			Sign sign = (Sign)block.getRelative(-1, 0, 0).getState();			
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
					|| userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				return true;
			}
		}
		if(block.getRelative(0, 0, +1).getTypeId() == Material.WALL_SIGN.getId())
		{
			Sign sign = (Sign)block.getRelative(0, 0, +1).getState();			
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
					|| userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				return true;
			}
		}
		if(block.getRelative(0, 0, -1).getTypeId() == Material.WALL_SIGN.getId())
		{
			Sign sign = (Sign)block.getRelative(0, 0, -1).getState();			
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
					|| userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				return true;
			}
		}
		return false;
	}
	
	///////////////////////////////
	//
	// HAS RELATIVE SIGN
	//
	///////////////////////////////
	public static int hasRelativeSign(Block block)
	{			
		if(block.getRelative(0, 1, 0).getTypeId() == Material.WALL_SIGN.getId())
		{
			/** 	NORMAL SHOP		*/
			Sign sign = (Sign)block.getRelative(0, 1, 0).getState();			
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
					|| userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				return 1;
			}
		}
		else
		{
			/** 	NEW SHOPTYPE	*/
			Block anchor = block.getRelative(0, 2, 0);
			
			if(anchor.getRelative(+1, 0, 0).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(+1, 0, 0).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return 1;
				}
			}
			else if(anchor.getRelative(-1, 0, 0).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(-1, 0, 0).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return 1;
				}
			}
			if(anchor.getRelative(0, 0, +1).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(0, 0, +1).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return 1;
				}
			}
			if(anchor.getRelative(0, 0, -1).getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)anchor.getRelative(0, 0, -1).getState();			
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$")
						|| userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					return 1;
				}
			}
		}		
		return 0;
	}

	///////////////////////////////
	//
	// HAS RELATIVE CHEST
	//
	///////////////////////////////
	public static int hasRelativeChest(Block block)
	{			
		if(block.getRelative(0, -1, 0).getTypeId() == Material.CHEST.getId())
		{
			/** 	NORMAL SHOP		*/
			if(BlockUtils.isDoubleChest(block.getRelative(0, -1, 0)) != null)
				return -1;		
			
			return 1;
		}
		else
		{
			/** 	NEW SHOPTYPE	*/
			Block anchor = SignUtils.getSignAnchor((Sign)block.getState()).getBlock();
			if(anchor.getRelative(0, -2, 0).getTypeId() == Material.CHEST.getId())
			{
				if(BlockUtils.isDoubleChest(anchor.getRelative(0, -2, 0)) != null)
					return -1;					
				return 1;
			}
		}		
		return 0;
	}
	
	///////////////////////////////
	//
	// LOAD USERSHOPS
	//
	///////////////////////////////
	public static void loadUserShops()
	{
		File folder = new File("plugins/BuyCraft/UserShops");
		folder.mkdirs();
		
		File[] fileList = folder.listFiles();
		for(File file : fileList)
		{
			if(!file.isFile())
				continue;
			
			if(!file.getName().endsWith(".bcf"))
				continue;
			
			try
			{
				ObjectInputStream objIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file.getAbsolutePath())));
				BCUserShop shop = (BCUserShop) objIn.readObject( );
				objIn.close(); 				
				
				userShopList.put(shop.toString(), shop);				
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				BCCore.printInConsole("Error while reading file: " + file.getName());  
			} 
		}
		BCCore.printInConsole("loaded " + userShopList.size() + " UserShops");  
	}
	
	///////////////////////////////
	//
	// ON SIGN CHANGE
	//
	///////////////////////////////
	@Override
	public void onSignChange(SignChangeEvent event)
	{
		Block block = event.getBlock();
		if(block.getTypeId() != Material.WALL_SIGN.getId())
			return;
		
		Sign sign = (Sign)block.getState();
		if(sign == null)
			return;
		
		int shopType = -1;
		if(event.getLine(0).equalsIgnoreCase("$SHOP$"))
		{
			// INFINITE SHOP
			shopType = 0;
		}
		else if(event.getLine(0).startsWith("$") && event.getLine(0).endsWith("$"))
		{
			// USER SHOP
			shopType = 1;
		}
		
		// NOT A SHOPSIGN
		if(shopType == -1)
			return;
		
		Player player = event.getPlayer();
		
		////////////////////////////
		//CHECK PERMISSION
		////////////////////////////
		if(shopType == 0)
		{
			/** INFINITE SHOP */
			if(!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.create"))
			{
				BCChatUtils.printError(player, "You are not allowed to create infinite shops.");
				SignUtils.cancelSignCreation(event);
				return;
			}
		}
		else if(shopType == 1)
		{
			/** USER SHOP */
			if(!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.create"))
			{
				BCChatUtils.printError(player, "You are not allowed to create usershops.");
				SignUtils.cancelSignCreation(event);
				return;
			}
			if(!BCCore.isShopOwner(player.getName(), BCShop.getSpecialTextOnLine(event.getLine(0), "$", "$")))
			{
				BCChatUtils.printError(player, "You cannot create usershops for other users.");
				SignUtils.cancelSignCreation(event);
				return;
			}
		}
		
		////////////////////////////
		// SEARCH CHEST
		////////////////////////////
		int chestCheck = hasRelativeChest(block);
		if(chestCheck == 0)
		{
			BCChatUtils.printError(player, "No chest found.");
			BCChatUtils.printInfo(player, ChatColor.GRAY, "Place the chest before placing the sign.");
			SignUtils.cancelSignCreation(event);
			return;
		}
		else if(chestCheck == -1)
		{
			BCChatUtils.printError(player, "Doublechests are not allowed for shops.");
			SignUtils.cancelSignCreation(event);
			return;
		}
		
		////////////////////////////
		// CHECK ITEMTYPE
		////////////////////////////
		String[] itemSplit = BCShop.getSplit(BCShop.getSpecialTextOnLine(event.getLine(1), "{", "}"));
		if(!BCCore.isAllowedItem(itemSplit[0]))
		{
			BCChatUtils.printError(player, "Item '" + itemSplit[0] + "' not found.");
			SignUtils.cancelSignCreation(event);
			return;
		}
		
		// CHECK BUYRATIOS
		if(!BCShop.checkCreation(event.getLines()))
		{
			BCChatUtils.printError(player, "Wrong Syntax on line 3 or line 4!");
			SignUtils.cancelSignCreation(event);
			return;
		}
		
		////////////////////////////
		// HANDLE CREATION
		////////////////////////////
		if(shopType == 0)
		{
			/** INFINITE SHOP */
			event.setLine(0, event.getLine(0).toUpperCase());
			BCChatUtils.printSuccess(player, "Infiniteshop created!");
		}
		else if(shopType == 1)
		{
			/** USER SHOP */
			event.setLine(0, "$" + BCCore.getAlias(player.getName()) + "$");
			BCUserShop shop = new BCUserShop(sign.getBlock().getWorld().getName(), sign.getX(), sign.getY(), sign.getZ());
			userShopList.put(shop.toString(), shop);
			shop.saveShop();
			BCChatUtils.printSuccess(player, "Usershop created!");
		}				
	}
	
	///////////////////////////////
	//
	// ON BLOCK BREAK
	//
	///////////////////////////////
	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		
		if(isSignAnchor(block))
		{
			BCChatUtils.printError(event.getPlayer(), "Please remove the shop first.");
			event.setCancelled(true);
			return;		
		}
		
		if(block.getTypeId() != Material.CHEST.getId() && block.getTypeId() != Material.WALL_SIGN.getId())
		{
			return;
		}
		
		Player player = event.getPlayer();
		if(block.getTypeId() == Material.CHEST.getId())
		{
			if(hasRelativeSign(block) == 1)
			{				
				Sign sign = getRelativeSign(block);				
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$"))
				{
					/**		CHECK INFINITE SHOP 	*/
					if(!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.create"))
					{
						BCChatUtils.printError(player, "You are not allowed to destroy infinite shops.");
						event.setCancelled(true);
						return;
					}
					else
					{
						BCChatUtils.printSuccess(player, "Removed infinite shop.");
						return;
					}
				}
				else if(userShopList.containsKey(BlockToString(sign.getBlock())))
				{
					if(!BCCore.isShopOwner(player.getName(), BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$"))
							&& !UtilPermissions.playerCanUseCommand(player, "buycraft.*"))
					{
						/**		CHECK USERSHOP 	*/
						{
							BCChatUtils.printError(player, "You are not allowed to destroy usershops from other users.");
							event.setCancelled(true);
							return;
						}
					}
					else
					{
						// REMOVE SHOP &  SHOPFILE
						BCUserShop shop = userShopList.get(BlockToString(sign.getBlock()));
						if(shop != null)
						{
							shop.restoreInventory((Chest)block.getState());
							File shopFile = new File("plugins/BuyCraft/UserShops/" + shop.toString() + ".bcf");
							if(shopFile.exists())
							{
								shopFile.delete();
							}
							userShopList.remove(BlockToString(sign.getBlock()));
							shop = null;
							BCChatUtils.printSuccess(player, "Removed usershop.");
							return;
						}	
						else
						{
							BCChatUtils.printError(player, "No usershop found at this location.");
							return;
						}
					}
				}
			}
		}
		else if(block.getTypeId() == Material.WALL_SIGN.getId())
		{
			Sign sign = (Sign)block.getState();
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$"))
			{
				/**		CHECK INFINITE SHOP 	*/
				if(!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.create"))
				{
					BCChatUtils.printError(player, "You are not allowed to destroy infinite shops.");
					event.setCancelled(true);
					return;
				}
				else
				{
					BCChatUtils.printSuccess(player, "Removed infinite shop.");
					return;
				}
			}
			else if(userShopList.containsKey(BlockToString(sign.getBlock())))
			{
				/**		CHECK USERSHOP 		*/
				if(!BCCore.isShopOwner(player.getName(), BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$"))
						&& !UtilPermissions.playerCanUseCommand(player, "buycraft.*"))
				{
					BCChatUtils.printError(player, "You are not allowed to destroy usershops from other users.");
					event.setCancelled(true);
					return;
				}
				else
				{
					// REMOVE SHOP &  SHOPFILE
					BCUserShop shop = userShopList.get(BlockToString(sign.getBlock()));
					if(shop != null)
					{
						shop.restoreInventory(getRelativeChest(block));
						File shopFile = new File("plugins/BuyCraft/UserShops/" + shop.toString() + ".bcf");
						if(shopFile.exists())
						{
							shopFile.delete();
						}
						userShopList.remove(BlockToString(sign.getBlock()));
						shop = null;
						BCChatUtils.printSuccess(player, "Removed usershop.");
						return;
					}
					else
					{
						BCChatUtils.printError(player, "No usershop found at this location.");
						return;
					}
				}
			}
		}
	}

	///////////////////////////////
	//
	// ON  PISTON EXTEND
	//
	///////////////////////////////
	@Override
	public void onBlockPistonExtend(BlockPistonExtendEvent event)
	{
//		try
//		{
//			if(!checkPistonEvent(event.getBlocks()))
//			{
//				event.setCancelled(true);
//			}
//		}
//		catch(Exception e) {}
	}
	
	///////////////////////////////
	//
	// ON PISTON RETRACT
	//
	///////////////////////////////	

	@Override
	public void onBlockPistonRetract(BlockPistonRetractEvent event)
	{
//		try
//		{
//			if(!event.isSticky())
//				return;
//			
//			List<Block> blockList = new ArrayList<Block>();
//			blockList.add(event.getRetractLocation().getBlock());
//			if(!checkPistonEvent(blockList))
//			{
//				event.setCancelled(true);
//			}
//		}
//		catch(Exception e) {}
	}

	///////////////////////////////
	//
	// CHECK PISTONEVENT
	//
	///////////////////////////////	
	public boolean checkPistonEvent(List<Block> blockList)
	{
		for(Block block : blockList)
		{
			if(isSignAnchor(block))
			{
				return false;		
			}
			
			if(block.getTypeId() == Material.CHEST.getId())
			{
				if(hasRelativeSign(block) == 1)
				{				
					Sign sign = getRelativeSign(block);				
					if(sign.getLine(0).equalsIgnoreCase("$SHOP$"))
					{
						/**		CHECK INFINITE SHOP 	*/
						return false;			
					}
					else if(BCBlockListener.userShopList.containsKey(BCBlockListener.BlockToString(sign.getBlock())))
					{
						/**		CHECK USERSHOP 	*/
						return false;		
					}
				}
			}
			else if(block.getTypeId() == Material.WALL_SIGN.getId())
			{
				Sign sign = (Sign)block.getState();
				if(sign.getLine(0).equalsIgnoreCase("$SHOP$"))
				{
					/**		CHECK INFINITE SHOP 	*/
					return false;	
					
				}
				else if(BCBlockListener.userShopList.containsKey(BCBlockListener.BlockToString(sign.getBlock())))
				{
					/**		CHECK USERSHOP 		*/
					return false;	
				}
			}
		}
		return true;
	}
}