package net.meano.PlayerManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import redis.clients.jedis.Jedis;

public class PlayerInfo {

	public BukkitMain PMBukkit;
	public BungeeMain PMBungee;
	
	public void LogInfo(String msg) {
		if(PMBukkit != null)
			PMBukkit.getLogger().info(msg);

		if(PMBungee != null) 
			PMBungee.getLogger().info(msg);
	}
	
	private Map<String, Object> PlayerInfoMap = new HashMap<String, Object>();
	private Jedis Redis; 
	
	PlayerInfo(ResultSet playerData, Jedis redis, Map<String, String> DataClass) {
		try {
			Class.forName("org.bukkit.plugin.java.JavaPlugin");
			PMBukkit = BukkitMain.Instance;
		} catch (ClassNotFoundException e) {
			LogInfo("未找到Bukkit驱动" + e.getLocalizedMessage());
			PMBungee = BungeeMain.Instance;
		}

		Redis = redis;
		for(Map.Entry<String, String> ClassEntry : DataClass.entrySet() ) {
			switch(ClassEntry.getValue()) {
				case "String":
					PlayerInfoMap.put(ClassEntry.getKey(), GetResultString(playerData, ClassEntry.getKey()));
					break;
				case "Integer":
					PlayerInfoMap.put(ClassEntry.getKey(), GetResultInteger(playerData, ClassEntry.getKey()));
					break;
				case "Boolean":
					PlayerInfoMap.put(ClassEntry.getKey(), GetResultBoolean(playerData, ClassEntry.getKey()));
					break;
			}
		}
	}
	
	public String GetString(String keyName) {
		if(PlayerInfoMap.containsKey(keyName)) {
			return (String) PlayerInfoMap.get(keyName);
		}
		else {
			return null;
		}
	}
	
	public Integer GetInteger(String keyName) {
		if(PlayerInfoMap.containsKey(keyName)) {
			return (Integer) PlayerInfoMap.get(keyName);
		}
		else {
			return null;
		}
	}
	
	public Boolean GetBoolean(String keyName) {
		if(PlayerInfoMap.containsKey(keyName)) {
			return (Boolean) PlayerInfoMap.get(keyName);
		}
		else {
			return null;
		}
	}

	public String GetPlayerUUID() {
		return GetString("PlayerUUID");
	}

	public String GetDataUUID() {
		return GetString("UUID");
	}
	
	public String GetPlayerName() {
		return GetString("PlayerName");
	}

	public String GetRedisKey(String keyName) {
		return Redis.get(keyName + "." + GetDataUUID());
	}
	
	public void SetRedisKey(String keyName, String value) {
		Redis.set(keyName + "." + GetDataUUID(), value);
	}
	
	public void SetRedisKey(String keyName, String value, int expire) {
		Redis.set(keyName + "." + GetDataUUID(), value);
		Redis.expire(keyName + "." + GetDataUUID(), expire);
	}
	
	private String GetResultString(ResultSet playerData, String KeyName){
		try {
			return playerData.getString(KeyName) == null ? "" :  playerData.getString(KeyName);
		} catch (SQLException e) {
			return "";
		}
	}
	
	private Integer GetResultInteger(ResultSet playerData,String KeyName) { 
		try {
			return playerData.getInt(KeyName);
		} catch (SQLException e) {
			return 0;
		}
	}
	
	private Boolean GetResultBoolean(ResultSet playerData,String KeyName) { 
		try {
			return playerData.getBoolean(KeyName);
		} catch (SQLException e) {
			return false;
		}
	}

}
