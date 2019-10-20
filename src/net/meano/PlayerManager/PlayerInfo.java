package net.meano.PlayerManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PlayerInfo {

	public BukkitMain PMBukkit;
	public BungeeMain PMBungee;
	
	private Date LoginTime = new Date();
	
	public void setLoginTime() {
		LoginTime = new Date();
	}
	
	public Date getLoginTime() {
		return LoginTime;
	}
	
	public long getOnlineTime() {
		return (new Date()).getTime() - LoginTime.getTime();
	}

	public JedisPool RedisPool;

	public void LogInfo(String msg) {
		if(PMBukkit != null)
			PMBukkit.getLogger().info(msg);

		if(PMBungee != null) 
			PMBungee.getLogger().info(msg);
	}
	
	private Map<String, Object> PlayerInfoMap = new HashMap<String, Object>();
	
	PlayerInfo(ResultSet playerData, Map<String, String> DataClass) {
		try {
			Class.forName("org.bukkit.plugin.java.JavaPlugin");
			PMBukkit = BukkitMain.Instance;
			RedisPool = PMBukkit.RedisPool;
		} catch (ClassNotFoundException e) {
			LogInfo("未找到Bukkit驱动" + e.getLocalizedMessage());
			PMBungee = BungeeMain.Instance;
			RedisPool = PMBungee.RedisPool;
		}

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

	public String GetGlobalRedisKey(String keyName) {
		Jedis redis = RedisPool.getResource();
		String globalRedisKey = redis.get(keyName);
		redis.close();
		return globalRedisKey;
	}

	public String GetRedisKey(String keyName) {
		Jedis redis = RedisPool.getResource();
		String redisKey = redis.get(keyName + "." + GetDataUUID());
		redis.close();
		return redisKey;
	}
	
	public void SetRedisKey(String keyName, String value) {
		Jedis redis = RedisPool.getResource();
		redis.set(keyName + "." + GetDataUUID(), value);
		redis.close();
	}
	
	public void SetRedisKey(String keyName, String value, int expire) {
		Jedis redis = RedisPool.getResource();
		redis.set(keyName + "." + GetDataUUID(), value);
		redis.expire(keyName + "." + GetDataUUID(), expire);
		redis.close();
	}
	
	private String GetResultString(ResultSet playerData, String KeyName){
		try {
			return playerData.getString(KeyName) == null ? "" :  playerData.getString(KeyName);
		} catch (SQLException e) {
			return "";
		}
	}

	private Integer GetResultInteger(ResultSet playerData, String KeyName) { 
		try {
			return playerData.getInt(KeyName);
		} catch (SQLException e) {
			return 0;
		}
	}
	
	private Boolean GetResultBoolean(ResultSet playerData, String KeyName) { 
		try {
			return playerData.getBoolean(KeyName);
		} catch (SQLException e) {
			return false;
		}
	}

}
