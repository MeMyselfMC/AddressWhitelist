package io.memyself.addresswhitelist;

import java.io.File;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.meta.SkullMeta;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager
  implements CommandExecutor {
	
	private AddressWhitelist aw;
	
	public CommandManager(AddressWhitelist instance) {
		aw = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(cmd.getName().equalsIgnoreCase("addresswhitelist")) {
			if(args.length < 3) {
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("reload")) {
						if(sender.hasPermission("addresswhitelist.reload")) {
							if(!new File(aw.getDataFolder(), "config.yml").exists()) {
								aw.saveDefaultConfig();
							}
							aw.reloadConfig();
							
							if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] Configuration reloaded!");
							
							if(aw.getConfig().getString("locale.config-reloaded") != null && !aw.getConfig().getString("locale.config-reloaded").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.config-reloaded")));
							}
							
							int kickCount = 0;
							
							for(Player player : Bukkit.getOnlinePlayers()) {
								if(!aw.getConfig().getBoolean("options.inverted")) {
									if(!aw.getConfig().getStringList("list").contains(player.getAddress().getHostString())) {
										player.kickPlayer(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.not-in-whitelist")));
										
										kickCount++;
									}
								} else {
									if(aw.getConfig().getStringList("list").contains(player.getAddress().getHostString())) {
										player.kickPlayer(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.in-blacklist")));
										
										kickCount++;
									}
								}
							}
							
							if(kickCount > 0) {
								if(kickCount == 1) if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] 1 player was kicked because their IP address was " + (!aw.getConfig().getBoolean("options.inverted") ? "not whitelisted." : "blacklisted."));
								else if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] " + kickCount + " players were kicked because their IP addresses were " + (!aw.getConfig().getBoolean("options.inverted") ? "not whitelisted." : "blacklisted."));
							}
						} else {
							if(aw.getConfig().getString("locale.error.no-permission") != null && !aw.getConfig().getString("locale.error.no-permission").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.no-permission")));
							}
						}
					} else if(args[0].equalsIgnoreCase("mode")) {
						if(sender.hasPermission("addresswhitelist.mode")) {
							if(!aw.getConfig().getBoolean("options.inverted")) aw.getConfig().set("options.inverted", true);
							else aw.getConfig().set("options.inverted", false);
							aw.saveConfig();
							
							if(aw.getConfig().getString("locale.mode-changed") != null && !aw.getConfig().getString("locale.mode-changed").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.mode-changed").replaceAll("%mode%", (!aw.getConfig().getBoolean("options.inverted") ? "whitelist" : "blacklist"))));
							}
						} else {
							if(aw.getConfig().getString("locale.error.no-permission") != null && !aw.getConfig().getString("locale.error.no-permission").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.no-permission")));
							}
						}
					} else if(args[0].equalsIgnoreCase("list")) {
						if(sender.hasPermission("addresswhitelist.list")) {
							if(aw.getConfig().getString("locale.list.header") != null && !aw.getConfig().getString("locale.list.header").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.list.header").replaceAll("%mode%", (!aw.getConfig().getBoolean("options.inverted") ? "Whitelist" : "Blacklist"))));
							}
							
							for(String entry : aw.getConfig().getStringList("list")) {
								String entryMsg = aw.getConfig().getString("locale.list.entry");
								
								if(entryMsg != null && !entryMsg.isEmpty()) {
									entryMsg = entryMsg.replaceAll("%ip_address%", entry);
									entryMsg = entryMsg.replaceAll("%ip_address_location%", Utilities.getIpAddressLocation(entry));
									
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', entryMsg));
								}
							}
							
							if(aw.getConfig().getString("locale.list.footer") != null && !aw.getConfig().getString("locale.list.footer").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.list.footer").replaceAll("%mode%", (!aw.getConfig().getBoolean("options.inverted") ? "Whitelist" : "Blacklist"))));
							}
						} else {
							if(aw.getConfig().getString("locale.error.no-permission") != null && !aw.getConfig().getString("locale.error.no-permission").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.no-permission")));
							}
						}
					} else if(args[0].equalsIgnoreCase("session")) {
						if(sender instanceof Player) {
							if(sender.hasPermission("addresswhitelist.session")) {
								if(aw.getConfig().getInt("options.session-gui.size") >= 9 && aw.getConfig().getInt("options.session-gui.size") <= 54 && String.valueOf((aw.getConfig().getInt("options.session-gui.size") / 9)).length() == 1) {
									Inventory menu = Bukkit.createInventory(null, aw.getConfig().getInt("options.session-gui.size"), ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("options.session-gui.title")));
									
									if(EventListener.deniedLoginAttempts.size() > 0) {
										int menuItemCount = 0;
										for(String deniedLoginAttemptLogEntry : EventListener.deniedLoginAttempts) {
											if(menuItemCount < menu.getSize()) {
												menu.addItem(getDeniedLoginAttemptItemBase(deniedLoginAttemptLogEntry));
												
												menuItemCount++;
											} else break;
										}
										
										((Player) sender).openInventory(menu);
									} else {
										if(aw.getConfig().getString("locale.no-denied-login-attempts") != null && !aw.getConfig().getString("locale.no-denied-login-attempts").isEmpty()) {
											sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.no-denied-login-attempts")));
										}
									}
								} else aw.getLogger().warning("Invalid session GUI size given in config.yml!");
							} else {
								if(aw.getConfig().getString("locale.error.no-permission") != null && !aw.getConfig().getString("locale.error.no-permission").isEmpty()) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.no-permission")));
								}
							}
						} else {
							if(aw.getConfig().getString("locale.error.only-player-sender") != null && !aw.getConfig().getString("locale.error.only-player-sender").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.only-player-sender")));
							}
						}
					} else {
						if(aw.getConfig().getString("locale.error.invalid-arguments") != null && !aw.getConfig().getString("locale.error.invalid-arguments").isEmpty()) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.invalid-arguments")));
						}
					}
				} else if(args.length == 2) {
					if(args[0].equalsIgnoreCase("add")) {
						if(sender.hasPermission("addresswhitelist.add")) {
							if(!args[1].isEmpty()) {
								List<String> list = aw.getConfig().getStringList("list");
								
								if(!list.contains(args[1])) {
									list.add(args[1]);
									
									aw.getConfig().set("list", list);
									aw.saveConfig();
									
									if(aw.getConfig().getString("locale.address-added-to-list") != null && !aw.getConfig().getString("locale.address-added-to-list").isEmpty()) {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.address-added-to-list").replaceAll("%ip_address%", args[1])));
									}
									
									int kickCount = 0;
									
									for(Player player : Bukkit.getOnlinePlayers()) {
										if(!aw.getConfig().getBoolean("options.inverted")) {
											if(!aw.getConfig().getStringList("list").contains(player.getAddress().getHostString())) {
												player.kickPlayer(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.not-in-whitelist")));
												
												kickCount++;
											}
										} else {
											if(player.getAddress().getHostString().equalsIgnoreCase(args[1])) {
												player.kickPlayer(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.in-blacklist")));
												
												kickCount++;
											}
										}
									}
									
									if(kickCount > 0) {
										if(kickCount == 1) if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] IP address list updated, 1 player was kicked because their IP address was " + (!aw.getConfig().getBoolean("options.inverted") ? "not whitelisted." : "blacklisted."));
										else if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] IP address list updated, " + kickCount + " players were kicked because their IP addresses were " + (!aw.getConfig().getBoolean("options.inverted") ? "not whitelisted." : "blacklisted."));
									}
								} else {
									if(aw.getConfig().getString("locale.error.address-already-listed") != null && !aw.getConfig().getString("locale.error.address-already-listed").isEmpty()) {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.address-already-listed").replaceAll("%ip_address%", args[1])));
									}
								}
							} else {
								if(aw.getConfig().getString("locale.error.invalid-arguments") != null && !aw.getConfig().getString("locale.error.invalid-arguments").isEmpty()) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.invalid-arguments")));
								}
							}
						} else {
							if(aw.getConfig().getString("locale.error.no-permission") != null && !aw.getConfig().getString("locale.error.no-permission").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.no-permission")));
							}
						}
					} else if(args[0].equalsIgnoreCase("remove")) {
						if(sender.hasPermission("addresswhitelist.remove")) {
							if(!args[1].isEmpty()) {
								List<String> list = aw.getConfig().getStringList("list");
								
								if(list.contains(args[1])) {
									list.remove(args[1]);
									
									aw.getConfig().set("list", list);
									aw.saveConfig();
									
									if(aw.getConfig().getString("locale.address-removed-from-list") != null && !aw.getConfig().getString("locale.address-removed-from-list").isEmpty()) {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.address-removed-from-list").replaceAll("%ip_address%", args[1])));
									}
									
									if(!aw.getConfig().getBoolean("options.inverted")) {
										int kickCount = 0;
										
										for(Player player : Bukkit.getOnlinePlayers()) {
											if(!aw.getConfig().getStringList("list").contains(player.getAddress().getHostString())) {
												player.kickPlayer(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.kick-messages.not-in-whitelist")));
												
												kickCount++;
											}
										}
										
										if(kickCount > 0) {
											if(kickCount == 1) if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] IP address list updated, 1 player was kicked because their IP address was not whitelisted.");
											else if(aw.getConfig().getBoolean("options.debug")) aw.getLogger().info("[DEBUG] IP address list updated, " + kickCount + " players were kicked because their IP addresses were not whitelisted.");
										}
									}
								} else {
									if(aw.getConfig().getString("locale.error.address-not-listed") != null && !aw.getConfig().getString("locale.error.address-not-listed").isEmpty()) {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.address-not-listed").replaceAll("%ip_address%", args[1])));
									}
								}
							} else {
								if(aw.getConfig().getString("locale.error.invalid-arguments") != null && !aw.getConfig().getString("locale.error.invalid-arguments").isEmpty()) {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.invalid-arguments")));
								}
							}
						} else {
							if(aw.getConfig().getString("locale.error.no-permission") != null && !aw.getConfig().getString("locale.error.no-permission").isEmpty()) {
								sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.no-permission")));
							}
						}
					} else {
						if(aw.getConfig().getString("locale.error.invalid-arguments") != null && !aw.getConfig().getString("locale.error.invalid-arguments").isEmpty()) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.invalid-arguments")));
						}
					}
				} else {
					if(aw.getConfig().getString("locale.command-usage") != null && !aw.getConfig().getString("locale.command-usage").isEmpty()) {
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.command-usage")));
					}
				}
			} else {
				if(aw.getConfig().getString("locale.error.invalid-arguments") != null && !aw.getConfig().getString("locale.error.invalid-arguments").isEmpty()) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aw.getConfig().getString("locale.error.invalid-arguments")));
				}
			}
		}
		return true;
	}
	
	private ItemStack getDeniedLoginAttemptItemBase(String deniedLoginAttemptLogEntry) {
		String[] deniedLoginAttemptData = deniedLoginAttemptLogEntry.split(":", 5);
		
		String timestamp = new SimpleDateFormat(aw.getConfig().getString("options.session-gui.date-format")).format(new Date(Long.parseLong(deniedLoginAttemptData[2])));
		String ip = deniedLoginAttemptData[3];
		String reason = deniedLoginAttemptData[4];
		
		ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		
		meta.setOwner(deniedLoginAttemptData[1]);
		
		String displayName = aw.getConfig().getString("options.session-gui.item.display-name");
		displayName = displayName.replaceAll("%player_name%", deniedLoginAttemptData[1]);
		displayName = displayName.replaceAll("%player_uuid%", deniedLoginAttemptData[0]);
		displayName = displayName.replaceAll("%timestamp%", timestamp);
		displayName = displayName.replaceAll("%ip_address%", ip);
		displayName = displayName.replaceAll("%ip_address_location%", Utilities.getIpAddressLocation(ip));
		displayName = displayName.replaceAll("%ip_address_status%", Utilities.getIpAddressStatus(ip));
		displayName = displayName.replaceAll("%login_denial_reason%", reason.equalsIgnoreCase("NOT_WHITELISTED") ? aw.getConfig().getString("locale.placeholders.login-denial-reason.not-whitelisted") : aw.getConfig().getString("locale.placeholders.login-denial-reason.blacklisted"));
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		
		ArrayList<String> lore = new ArrayList<String>();
		for(String loreLine : aw.getConfig().getStringList("options.session-gui.item.lore")) {
			loreLine = loreLine.replaceAll("%player_name%", deniedLoginAttemptData[1]);
			loreLine = loreLine.replaceAll("%player_uuid%", deniedLoginAttemptData[0]);
			loreLine = loreLine.replaceAll("%timestamp%", timestamp);
			loreLine = loreLine.replaceAll("%ip_address%", ip);
			loreLine = loreLine.replaceAll("%ip_address_location%", Utilities.getIpAddressLocation(ip));
			loreLine = loreLine.replaceAll("%ip_address_status%", Utilities.getIpAddressStatus(ip));
			loreLine = loreLine.replaceAll("%login_denial_reason%", reason.equalsIgnoreCase("NOT_WHITELISTED") ? aw.getConfig().getString("locale.placeholders.login-denial-reason.not-whitelisted") : aw.getConfig().getString("locale.placeholders.login-denial-reason.blacklisted"));
			
			lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
		}
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		
		return item;
	}
	
}