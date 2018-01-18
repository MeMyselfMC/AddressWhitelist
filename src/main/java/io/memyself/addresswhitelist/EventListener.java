package io.memyself.addresswhitelist;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.entity.EntityType;

import org.bukkit.inventory.Inventory;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class EventListener
  implements Listener {
	
	private AddressWhitelist aw;
	
	public EventListener(AddressWhitelist instance) {
		aw = instance;
	}
	
	public static List<String> deniedLoginAttempts = new ArrayList<String>();
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		if(!aw.getConfig().getBoolean("options.inverted")) {
			if(!Utilities.isIpAddressListed(event.getAddress().getHostAddress())) {
				event.setKickMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.not-in-whitelist")));
				event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
				
				deniedLoginAttempts.add(event.getUniqueId() + ":" + event.getName() + ":" + System.currentTimeMillis() + ":" + event.getAddress().getHostAddress() + ":NOT_WHITELISTED");
				
				if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] " + event.getName() + " tried to join but was denied because their IP address (" + event.getAddress().getHostAddress() + ") is not whitelisted.");
				
				String broadcastMsg = aw.getConfig().getString("locale.notifications.join-attempt-not-whitelisted");
				
				if(broadcastMsg != null && !broadcastMsg.isEmpty()) {
					broadcastMsg = broadcastMsg.replaceAll("%player_name%", event.getName());
					broadcastMsg = broadcastMsg.replaceAll("%ip_address%", event.getAddress().getHostAddress());
					
					Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', broadcastMsg), "addresswhitelist.notify");
				}
			}
		} else {
			if(Utilities.isIpAddressListed(event.getAddress().getHostAddress())) {
				event.setKickMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.in-blacklist")));
				event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
				
				deniedLoginAttempts.add(event.getUniqueId() + ":" + event.getName() + ":" + System.currentTimeMillis() + ":" + event.getAddress().getHostAddress() + ":BLACKLISTED");
				
				if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] " + event.getName() + " tried to join but was denied because their IP address (" + event.getAddress().getHostAddress() + ") is blacklisted.");
				
				String broadcastMsg = aw.getConfig().getString("locale.notifications.join-attempt-blacklisted");
				
				if(broadcastMsg != null && !broadcastMsg.isEmpty()) {
					broadcastMsg = broadcastMsg.replaceAll("%player_name%", event.getName());
					broadcastMsg = broadcastMsg.replaceAll("%ip_address%", event.getAddress().getHostAddress());
					
					Bukkit.broadcast(ChatColor.translateAlternateColorCodes('&', broadcastMsg), "addresswhitelist.notify");
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
		
		if(inv.getType().equals(InventoryType.CHEST)) {
			if(inv.getName().equals(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("options.session-gui.title"))) && inv.getSize() == aw.getConfig().getInt("options.session-gui.size")) {
				if(event.getWhoClicked().getType().equals(EntityType.PLAYER)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryDrag(InventoryDragEvent event) {
		Inventory inv = event.getInventory();
		
		if(inv.getType().equals(InventoryType.CHEST)) {
			if(inv.getName().equals(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("options.session-gui.title"))) && inv.getSize() == aw.getConfig().getInt("options.session-gui.size")) {
				if(event.getWhoClicked().getType().equals(EntityType.PLAYER)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
}