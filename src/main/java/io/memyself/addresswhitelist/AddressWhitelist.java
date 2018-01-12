package io.memyself.addresswhitelist;

import java.io.File;

import org.bukkit.ChatColor;

import org.bukkit.entity.Player;

import org.bukkit.plugin.java.JavaPlugin;

import io.memyself.addresswhitelist.bstats.MetricsLite;

public class AddressWhitelist
  extends JavaPlugin {
	
	@Override
	public void onEnable() {
		if(!new File(getDataFolder(), "config.yml").exists()) {
			saveDefaultConfig();
			
			reloadConfig();
		}
		new Utilities(this);
		
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
		
		getCommand("addresswhitelist").setExecutor(new CommandManager(this));
		
		if(getConfig().getBoolean("options.metrics")) {
			if(getConfig().getBoolean("options.debug")) getLogger().info("[DEBUG] Will be attempting to submit statistics to bStats.org.");
			
			new MetricsLite(this);
		}
		
		getLogger().info("AddressWhitelist v" + getDescription().getVersion() + " has been enabled!");
		
		int kickCount = 0;
		
		for(Player player : getServer().getOnlinePlayers()) {
			if(!getConfig().getBoolean("options.inverted")) {
				if(!getConfig().getStringList("list").contains(player.getAddress().getHostString())) {
					player.kickPlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("locale.kick-messages.not-in-whitelist")));
					
					kickCount++;
				}
			} else {
				if(getConfig().getStringList("list").contains(player.getAddress().getHostString())) {
					player.kickPlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("locale.kick-messages.in-blacklist")));
					
					kickCount++;
				}
			}
		}
		
		if(kickCount > 0) {
			if(kickCount == 1) if(getConfig().getBoolean("options.debug")) getLogger().info("[DEBUG] 1 player was kicked because their IP address was " + (!getConfig().getBoolean("options.inverted") ? "not whitelisted." : "blacklisted."));
			else if(getConfig().getBoolean("options.debug")) getLogger().info("[DEBUG] " + kickCount + " players were kicked because their IP addresses were " + (!getConfig().getBoolean("options.inverted") ? "not whitelisted." : "blacklisted."));
		}
	}
	
	@Override
	public void onDisable() {
		getLogger().info("AddressWhitelist v" + getDescription().getVersion() + " has been disabled.");
	}
	
}