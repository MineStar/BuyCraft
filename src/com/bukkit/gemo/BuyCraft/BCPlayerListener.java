package com.bukkit.gemo.BuyCraft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import com.bukkit.gemo.utils.UtilPermissions;

public class BCPlayerListener extends PlayerListener
{
	BCCore plugin = null;
	
	///////////////////////////////
	//
	// CONSTRUCTOR
	//
	///////////////////////////////
	public BCPlayerListener(BCCore plugin)
	{
		this.plugin = plugin;
	}

	///////////////////////////////
	//
	// ON INTERACT
	//
	///////////////////////////////
	@Override
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
			
		if(event.getClickedBlock().getTypeId() == Material.WALL_SIGN.getId())
		{		
			Sign sign = (Sign)event.getClickedBlock().getState();
			if(sign == null)
				return;
	
			int shopType = -1;
			if(sign.getLine(0).equalsIgnoreCase("$SHOP$"))
			{
				// INFINITE SHOP
				shopType = 0;
			}
			else if(sign.getLine(0).startsWith("$") && sign.getLine(0).endsWith("$"))
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
				if(!UtilPermissions.playerCanUseCommand(player, "buycraft.infinite.use"))
				{
					BCChatUtils.printError(player, "You are not allowed to use infinite shops.");
					event.setUseInteractedBlock(Event.Result.DENY);
					event.setUseItemInHand(Event.Result.DENY);
					event.setCancelled(true);
					return;
				}
			}
			else if(shopType == 1)
			{
				/** USER SHOP */
				if(!UtilPermissions.playerCanUseCommand(player, "buycraft.usershop.use"))
				{
					BCChatUtils.printError(player, "You are not allowed to use usershops.");
					event.setUseInteractedBlock(Event.Result.DENY);
					event.setUseItemInHand(Event.Result.DENY);
					event.setCancelled(true);
					return;
				}
			}		
			
			////////////////////////////
			// SEARCH CHEST
			////////////////////////////
			Block block = event.getClickedBlock();
			int chestCheck = BCBlockListener.hasRelativeChest(block);
			if(chestCheck != 1)
			{
				return;
			}
			
			Chest chest = BCBlockListener.getRelativeChest(block);
			////////////////////////////
			// HANDLE CLICK
			////////////////////////////
			
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setUseItemInHand(Event.Result.DENY);
			event.setCancelled(true);
			
			if(shopType == 0)
			{
				/** INFINITE SHOP */
				BCInfiniteShop shop = new BCInfiniteShop();
				shop.handleRightClick(player, sign, chest);
				shop = null;
			}
			else if(shopType == 1)
			{
				/** USER SHOP */
				BCUserShop shop = BCBlockListener.userShopList.get(BCBlockListener.BlockToString(block));
				if(shop != null)
				{
					shop.handleRightClick(player, sign, chest);
					shop = null;
				}
				else
				{
					BCChatUtils.printError(player, "No Usershop found at this location.");
				}
			}	
		}
		if(event.getClickedBlock().getTypeId() == Material.CHEST.getId())
		{	
			Block block = event.getClickedBlock();
			if(BCBlockListener.hasRelativeSign(block) == 1)
			{
				Sign sign =	BCBlockListener.getRelativeSign(block); 
				if(BCBlockListener.userShopList.containsKey(BCBlockListener.BlockToString(sign.getBlock())))
				{
					BCUserShop shop = BCBlockListener.userShopList.get(BCBlockListener.BlockToString(sign.getBlock()));
					if(!shop.isActive() && !BCCore.isShopOwner(event.getPlayer().getName(), BCShop.getSpecialTextOnLine(sign.getLine(0), "$", "$")))
					{
						BCChatUtils.printError(event.getPlayer(), "This shop is not activated.");
						BCChatUtils.printInfo(event.getPlayer(), ChatColor.GRAY, "Please contact the shopowner!");
						event.setUseInteractedBlock(Event.Result.DENY);
						event.setUseItemInHand(Event.Result.DENY);
						event.setCancelled(true);
					}
				}
			}
		}
	}	
}