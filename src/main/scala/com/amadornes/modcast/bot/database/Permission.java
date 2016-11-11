package com.amadornes.modcast.bot.database;

/**
 * Created by rewbycraft on 11/8/16.
 */
public enum Permission {
	NONE(0), GUEST(10), MOD(20), ADMIN(30);
	
	private int level;
	
	Permission(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public static Permission withLevel(int level) {
		for (Permission permission : values())
			if (permission.getLevel() == level)
				return permission;
		return null;
	}
	
	public static Permission parsePermission(String str) {
		for (Permission p : values())
			if (str.equalsIgnoreCase(String.valueOf(p.getLevel())) || p.toString().equalsIgnoreCase(str))
				return p;
		
		return null;
	}
}
