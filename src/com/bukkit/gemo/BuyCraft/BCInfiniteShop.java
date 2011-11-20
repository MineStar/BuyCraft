package com.bukkit.gemo.BuyCraft;

import java.io.Serializable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bukkit.gemo.utils.UtilPermissions;

public class BCInfiniteShop extends BCShop implements Serializable
{	
	private static final long serialVersionUID = 3456581809245152700L;

	///////////////////////////////////
	//
	// CONSTRUCTORS
	//
	///////////////////////////////////
	public BCInfiniteShop()
	{
		super();
	}
	
	public BCInfiniteShop(String worldName, int x, int y, int z)
	{
		super(worldName, x, y, z);
	}
	
	///////////////////////////////////
	//
	// HANDLE RIGHTCLICK
	//
	///////////////////////////////////		
	public void handleRightClick(Player player, Sign sign, Chest chest)
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
			
			if((item.getTypeId() != sellItemId && item.getTypeId() != Material.GOLD_INGOT.getId()) || (item.getTypeId() == sellItemId && item.getDurability() != sellItemData))
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
				if(!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.buy." + Material.getMaterial(sellItemId).name().toLowerCase()))
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
				
				if(newItemCount > 27*64)
				{
					BCChatUtils.printError(player, "The maximum amount of items you can buy at once is " + (27*64));
					BCChatUtils.printInfo(player, ChatColor.GRAY, "You tried to buy " + newItemCount);
					return;
				}
			
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
				if(!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.sell." + Material.getMaterial(sellItemId).name().toLowerCase()))
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
				if(newGoldCount > 27*64)
				{
					BCChatUtils.printError(player, "The maximum amount of gold you can buy at once is " + (27*64));
					BCChatUtils.printInfo(player, ChatColor.GRAY, "You tried to get " + newGoldCount);
					return;
				}
				
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
}
