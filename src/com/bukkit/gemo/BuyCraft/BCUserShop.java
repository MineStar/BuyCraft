package com.bukkit.gemo.BuyCraft;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.utils.UtilPermissions;

public class BCUserShop extends BCShop implements Serializable
{	
	private static final long serialVersionUID = 5197016944101717903L;
	private boolean isActive = false;
	private ArrayList<BCItemStack> shopInventory = null;
	
	///////////////////////////////////
	//
	// CONSTRUCTORS
	//
	///////////////////////////////////
	public BCUserShop()
	{
		super();
		shopInventory = new ArrayList<BCItemStack>();
	}
	
	public BCUserShop(String worldName, int x, int y, int z)
	{
		super(worldName, x, y, z);
		shopInventory = new ArrayList<BCItemStack>();		
	}
	
	///////////////////////////////////
	//
	// HANDLE RIGHTCLICK
	//
	///////////////////////////////////		
	public void handleRightClick(Player player, Sign sign, Chest chest)
	{
		String playerName = BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$");
		if(BCCore.isShopOwner(player.getName(), playerName))
		{
			if(isActive())
			{
				/**		DEACTIVATE SHOP	*/
				restoreInventory(chest);
			}
			else
			{
				/**		ACTIVATE SHOP	*/
				shopInventory.clear();
				for(ItemStack item : chest.getInventory().getContents())
				{
					if(item != null)
					{
						if(item.getTypeId() > 0)
						{
							shopInventory.add(new BCItemStack(item.getTypeId(), item.getDurability(), item.getAmount()));
						}
					}
				}
				chest.getInventory().clear();
			}
			setActive(!isActive());
			if(isActive())
			{
				player.sendMessage(ChatColor.DARK_AQUA + "The shop is now " + ChatColor.GREEN + "activated" + ChatColor.DARK_AQUA + "!");
			}
			else
			{
				player.sendMessage(ChatColor.DARK_AQUA + "The shop is now " + ChatColor.RED + "disabled" + ChatColor.DARK_AQUA + "!");
			}
			saveShop();
		}
		else
		{
			if(isActive())
			{
				String[] itemSplit = BCShop.getSplit(BCShop.getSpecialTextOnLine(sign.getLine(1), "{", "}"));
				Integer[] buyRatios = BCShop.getRatios(sign.getLine(2));
				Integer[] sellRatios = buyRatios;
				if(sign.getLine(3).length() > 0)
					sellRatios = BCShop.getRatios(sign.getLine(3));
				
				int sellItemId = BCCore.getItemId(itemSplit[0]);
				byte sellItemData = Byte.valueOf(itemSplit[1]);
				
				if(!BCCore.isAllowedItem(itemSplit[0]))
					return;
				
				int sellItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), sellItemId, sellItemData);
				int goldItemCountInChest = BCShop.countItemInInventory(chest.getInventory(), Material.GOLD_INGOT.getId());
				
				//////////////////////////////
				// CATCH OTHER/WRONG ITEMS
				//////////////////////////////
				for(ItemStack item : chest.getInventory().getContents())
				{
					if(item == null)
						continue;
					
					if(item.getTypeId() < 1)
						continue;
					
					if(item.getTypeId() != sellItemId && item.getTypeId() != Material.GOLD_INGOT.getId())
					{
						if(buyRatios[0] > 0 && buyRatios[1] > 0)			
							BCChatUtils.printInfo(player, ChatColor.GOLD, "You can BUY " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + buyRatios[1] + " gold.");
						if(sellRatios[0] > 0 && sellRatios[1] > 0)	
							BCChatUtils.printInfo(player, ChatColor.GOLD, "You can SELL " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + sellRatios[1] + " gold.");
						return;
					}
					else if(item.getDurability() != sellItemData)
					{
						if(buyRatios[0] > 0 && buyRatios[1] > 0)			
							BCChatUtils.printInfo(player, ChatColor.GOLD, "You can BUY " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + buyRatios[1] + " gold.");
						if(sellRatios[0] > 0 && sellRatios[1] > 0)	
							BCChatUtils.printInfo(player, ChatColor.GOLD, "You can SELL " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + sellRatios[1] + " gold.");
						return;
					}
				}	
				
				//////////////////////////////
				// CATCH SELL & BUY
				//////////////////////////////
				if(sellItemCountInChest == 0 && goldItemCountInChest == 0)
				{
					if(buyRatios[0] > 0 && buyRatios[1] > 0)			
						BCChatUtils.printInfo(player, ChatColor.GOLD, "You can BUY " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + buyRatios[1] + " gold.");
					if(sellRatios[0] > 0 && sellRatios[1] > 0)	
						BCChatUtils.printInfo(player, ChatColor.GOLD, "You can SELL " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + sellRatios[1] + " gold.");
					return;
				}
				if(sellItemCountInChest > 0 && goldItemCountInChest > 0)
				{
					BCChatUtils.printError(player, "You can only sell OR buy things, not both at the same time.");
					return;
				}
				
				//////////////////////////////
				// SELL / BUY ITEMS
				//////////////////////////////	
				if(goldItemCountInChest > sellItemCountInChest)
				{
					if(buyRatios[0] > 0 && buyRatios[1] > 0)
					{
						/**		CHECK PERMISSION	*/
						if(!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.buy." + Material.getMaterial(sellItemId).name().toLowerCase()))
						{
							BCChatUtils.printError(player, "You are not allowed to buy '" + Material.getMaterial(sellItemId).name() + "'.");
							return;
						}
						
						/**		BUY ITEMS	*/
						int restGoldCount = goldItemCountInChest % buyRatios[1];
						int soldGoldCount = goldItemCountInChest - restGoldCount;
						int newItemCount = goldItemCountInChest * buyRatios[0];	
						newItemCount = ((int)(newItemCount/buyRatios[1]) - buyRatios[0]/buyRatios[1]*restGoldCount);
						
						if(soldGoldCount < buyRatios[1])
						{
							BCChatUtils.printInfo(player, ChatColor.GOLD, "You need at least " + buyRatios[1] + " gold to buy " + buyRatios[0] + " '" + Material.getMaterial(sellItemId) + "'.");
							return;
						}
						
						// ENOUGH ITEMS IN INVENTORY
						if(newItemCount > countItemInShopInventory(sellItemId, sellItemData))
						{
							BCChatUtils.printError(player, "The maximum amount of items you can buy here is " + countItemInShopInventory(sellItemId, sellItemData));
							BCChatUtils.printInfo(player, ChatColor.GRAY, "You tried to buy " + newItemCount);
							return;
						}
							
						// INVENTORY FULL?
						if(newItemCount + countShopInventory() > 26*64)
						{
							BCChatUtils.printError(player, "The inventory of this shop is full.");
							BCChatUtils.printInfo(player, ChatColor.GRAY, "Please contact the shopowner!");
							return;
						}
						
						// UPDATE SHOPINVENTORY
						updateInventory(sellItemId, sellItemData, -newItemCount);
						updateInventory(Material.GOLD_INGOT.getId(), Short.valueOf("0"), soldGoldCount);
						
						// CLEAR INVENTORY
						chest.getInventory().clear();
						
						// ADD ITEM
						ItemStack newItem = new ItemStack(sellItemId, newItemCount);
						if(sellItemData > 0)
							newItem.setDurability(sellItemData);
						chest.getInventory().addItem(newItem);
						
						// ADD RESTGOLD
						if(restGoldCount > 0)
						{
							ItemStack newGold = new ItemStack(Material.GOLD_INGOT.getId(), restGoldCount);
							chest.getInventory().addItem(newGold);
						}
						
						saveShop();
						BCChatUtils.printInfo(player, ChatColor.GOLD, "You bought " + newItemCount + " '" + Material.getMaterial(sellItemId) + "' for " + soldGoldCount + " gold. (Rest: " + restGoldCount + " gold)");
						return;
					}
					else
					{
						BCChatUtils.printInfo(player, ChatColor.RED, "This shop does not sell anything.");
						return;
					}			
				}
				else
				{		
					if(sellRatios[0] > 0 && sellRatios[1] > 0)
					{
						/**		CHECK PERMISSION	*/
						if(!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.sell." + Material.getMaterial(sellItemId).name().toLowerCase()))
						{
							BCChatUtils.printError(player, "You are not allowed to sell '" + Material.getMaterial(sellItemId).name() + "'.");
							return;
						}
						/**		SELL ITEMS	*/	
						int restItemCount = sellItemCountInChest % sellRatios[0];
						int soldItemCount = sellItemCountInChest - restItemCount;
						int newGoldCount = sellRatios[1] * (int)(soldItemCount/sellRatios[0]);
						if(soldItemCount < sellRatios[0])
						{
							BCChatUtils.printInfo(player, ChatColor.GOLD, "The minimum amount to sell is " + sellRatios[0] + " '" + Material.getMaterial(sellItemId) + "' for " + sellRatios[1] + " gold.");
							return;
						}			
						
						// ENOUGH GOLD IN INVENTORY
						if(newGoldCount > countItemInShopInventory(Material.GOLD_INGOT.getId(), Short.valueOf("0")))
						{
							BCChatUtils.printError(player, "The maximum amount of items you can sell here is " + countItemInShopInventory(sellItemId, sellItemData));
							BCChatUtils.printInfo(player, ChatColor.GRAY, "You tried to sell " + sellItemCountInChest);
							return;
						}
						
						// INVENTORY FULL?
						if(newGoldCount + countShopInventory() > 26*64)
						{
							BCChatUtils.printError(player, "The inventory of this shop is full.");
							BCChatUtils.printInfo(player, ChatColor.GRAY, "Please contact the shopowner!");
							return;
						}
					
						// UPDATE SHOPINVENTORY
						updateInventory(Material.GOLD_INGOT.getId(), Short.valueOf("0"), -newGoldCount);
						updateInventory(sellItemId, sellItemData, +soldItemCount);
						
						// CLEAR INVENTORY
						chest.getInventory().clear();
						
						// ADD GOLD
						ItemStack newGold = new ItemStack(Material.GOLD_INGOT.getId(), newGoldCount);
						chest.getInventory().addItem(newGold);
						
						// ADD RESTITEM
						if(restItemCount > 0)
						{
							ItemStack newItem = new ItemStack(sellItemId, restItemCount);
							if(sellItemData > 0)
								newItem.setDurability(sellItemData);
							chest.getInventory().addItem(newItem);
						}
						saveShop();
						BCChatUtils.printInfo(player, ChatColor.GOLD, "You sold " + soldItemCount + " '" + Material.getMaterial(sellItemId) + "' for " + newGoldCount + " gold. (Rest: " + restItemCount + " " + Material.getMaterial(sellItemId) + ")");
						return;
					}
					else
					{
						BCChatUtils.printInfo(player, ChatColor.RED, "This shop does not buy anything.");
						return;
					}	
				}
			}
			else
			{
				BCChatUtils.printError(player, "This shop is not activated.");
				BCChatUtils.printInfo(player, ChatColor.GRAY, "Please contact the shopowner!");
				return;
			}
		}
	}
	
	///////////////////////////////////
	//
	// METHODS FOR REAL INVENTORY
	//
	///////////////////////////////////		
	public void restoreInventory(Chest chest)
	{
		chest.getInventory().clear();
		for(BCItemStack item : shopInventory)
		{
			if(item.getItem() != null)
			{
				if(item.getId() > 0)
				{
					chest.getInventory().addItem(item.getItem());
				}
			}
		}
		shopInventory.clear();
	}
	
	public void updateInventory(int itemID, short SubID, int updateAmount)
	{
		boolean found = false;
		
		// UPDATE ITEMSTACK
		for(BCItemStack item : shopInventory)
		{
			if(item.getItem() != null)
			{
				if(item.getId() == itemID && item.getSubId() == SubID)
				{					
					item.setAmount(item.getAmount() + updateAmount);
					found = true;
					break;
				}
			}
		}
		
		// DELETE ITEMSTACKS WITH AMOUNT < 1
		for(int i = shopInventory.size() - 1; i >= 0 ; i--)			
		{
			if(shopInventory.get(i).getAmount() < 1)
			{
				shopInventory.remove(i);
			}
		}
		
		// ADD ITEMSTACK, IF NOT FOUND
		if(!found && updateAmount > 0)
		{
			shopInventory.add(new BCItemStack(itemID, SubID, updateAmount));
			found = true;
		}
	}
	
	public int countItemInShopInventory(int itemID, short SubID)
	{
		int count = 0;
		for(BCItemStack item : shopInventory)
		{
			if(item.getItem() != null)
			{
				if(item.getId() == itemID && item.getSubId() == SubID)
				{					
					count += item.getAmount();
				}
			}
		}
		return count;
	}
	
	public int countShopInventory()
	{
		int count = 0;
		for(BCItemStack item : shopInventory)
		{
			if(item.getItem() != null)
			{
				if(item.getAmount() > 0)
					count += item.getAmount();
			}
		}
		return count;
	}
	
	///////////////////////////////////
	//
	// SAVE SHOP
	//
	///////////////////////////////////		
	public void saveShop()
	{
		File folder = new File("plugins/BuyCraft/UserShops/" + toString() + ".bcf");
        folder.mkdirs();
        
		if(folder.exists())
		{
			folder.delete();
		}
        
		try
		{
			ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/BuyCraft/UserShops/" + toString() + ".bcf")));
			objOut.writeObject(this);
			objOut.close(); 
		}
		catch (IOException e) 
		{
			e.printStackTrace();    		
		} 
		
		/*
		folder = new File("plugins/BuyCraft/UserShops/" + toString() + ".txt");
        folder.mkdirs();
        
		if(folder.exists())
		{
			folder.delete();
		}
        
		try
		{
			ObjectOutputStream objOut = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("plugins/BuyCraft/UserShops/" + toString() + ".txt")));
			String str = "";
			for(Material mat : Material.values())
			{
				str += "    - buycraft.infinite.buy." + mat.name().toLowerCase() + "\r\n";
				str += "    - buycraft.infinite.sell." + mat.name().toLowerCase() + "\r\n";
			}
			objOut.writeObject(str.getBytes("UTF-8"));
			
			str = "";
			for(Material mat : Material.values())
			{
				str += "    - buycraft.usershop.buy." + mat.name().toLowerCase() + "\r\n";
				str += "    - buycraft.usershop.sell." + mat.name().toLowerCase() + "\r\n";
			}
			objOut.writeObject(str.getBytes("UTF-8"));
			objOut.close(); 
		}
		catch (IOException e) 
		{
			e.printStackTrace();    		
		} 
		*/
	}

	///////////////////////////////////
	//
	// GETTER & SETTER
	//
	///////////////////////////////////		
	public boolean isActive()
	{
		return isActive;
	}

	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}
}
