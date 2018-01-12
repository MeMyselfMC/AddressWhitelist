package io.memyself.addresswhitelist;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bukkit.Bukkit;

import eu.theindra.geoip.api.GeoIP;

public class Utilities {
	
	private static AddressWhitelist aw;
	
	public Utilities(AddressWhitelist instance) {
		aw = instance;
	}
	
	public static String getIpAddressLocation(String ip) {
		InetAddress inetAddress = null;
		
		try {
			inetAddress = InetAddress.getByName(ip);
			
			if(inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
				return aw.getConfig().getString("locale.placeholders.ip-address-location.local-loopback-address");
			} else {
				if(Bukkit.getPluginManager().isPluginEnabled("GeoipAPI")) {
					GeoIP inetAddressGeoData = new GeoIP(inetAddress);
					
					boolean cityUnknown = (inetAddressGeoData.city == null || inetAddressGeoData.city.isEmpty());
					boolean countryUnknown = (inetAddressGeoData.countryName == null || inetAddressGeoData.countryName.isEmpty());
					
					if(cityUnknown || countryUnknown) {
						if(cityUnknown) {
							if(countryUnknown) return aw.getConfig().getString("locale.placeholders.ip-address-location.unknown-location");
							else return inetAddressGeoData.countryName;
						} else if(countryUnknown) {
							if(cityUnknown) return aw.getConfig().getString("locale.placeholders.ip-address-location.unknown-location");
							else return inetAddressGeoData.city;
						} else return inetAddressGeoData.city + ", " + inetAddressGeoData.countryName;
					} else return inetAddressGeoData.city + ", " + inetAddressGeoData.countryName;
				} else return aw.getConfig().getString("locale.placeholders.ip-address-location.geoipapi-not-found");
			}
		} catch(UnknownHostException ex) {
			return aw.getConfig().getString("locale.placeholders.ip-address-location.invalid-address");
		}
	}
	
	public static String getIpAddressStatus(String ip) {
		if(aw.getConfig().getStringList("list").contains(ip)) {
			if(aw.getConfig().getBoolean("options.inverted")) return aw.getConfig().getString("locale.placeholders.ip-address-status.blacklisted");
			else return aw.getConfig().getString("locale.placeholders.ip-address-status.whitelisted");
		} else {
			if(aw.getConfig().getBoolean("options.inverted")) return aw.getConfig().getString("locale.placeholders.ip-address-status.not-blacklisted");
			else return aw.getConfig().getString("locale.placeholders.ip-address-status.not-whitelisted");
		}
	}
	
}