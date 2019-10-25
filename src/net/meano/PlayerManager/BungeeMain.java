package net.meano.PlayerManager;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.meano.DataBase.DBManager;
import net.meano.DataBase.MySQL;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BungeeMain extends Plugin {
	public static BungeeMain Instance;
	public DBManager SQLData;
	public JedisPool RedisPool;
	
	ConfigurationProvider ConfigProvider;
	Configuration Config;
	
	public Map<String, PlayerInfo> PlayerMap = new HashMap<String, PlayerInfo>();
	public Map<String, String> PlayerDataMap = new HashMap<String, String>();
	
	public void onEnable() {
		Instance = this;
		if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
		ConfigProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		try {
			Config = ConfigProvider.load(new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String redisHost = Config.getString("Redis.Host", "localhost");
		int redisPort = Config.getInt("Redis.Port", 6379);
		String redisPassword = Config.getString("Redis.Password", "");
		RedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 2000, redisPassword);
		if(RedisPool == null) {
			getLogger().warning("Redis init failed!");
		}

		getProxy().getPluginManager().registerListener(this, new BungeeListener(this));
		getProxy().getPluginManager().registerCommand(this, new BungeeCommand(this));
		
		for(String key : Config.getSection("PlayerDBMap").getKeys()) {
			PlayerDataMap.put(key, Config.getString("PlayerDBMap." + key));
		}

		String SQLHost = Config.getString("MySQL.Host", "localhost");
		String SQLPort = String.valueOf(this.Config.getInt("MySQL.Port", 3306));
		String SQLDatabase = Config.getString("MySQL.Database", "Minecraft");
		String SQLUsername = Config.getString("MySQL.Username", "root");
		String SQLPassword = Config.getString("MySQL.Password", "");
		String SQLTable = Config.getString("MySQL.Table", "PMPlayers");
		
		SQLData = new MySQL(SQLHost, SQLPort, SQLDatabase, SQLUsername, SQLPassword, SQLTable);
	}
	
	public PlayerInfo GetPlayerInfo(String player) {
		SQLData.Open();
		ResultSet PlayerData = SQLData.GetPlayerInfo(player);
		if(PlayerData == null) {
			SQLData.Close();
			return null;
		}
		
		String playerUUID;
		try {
			playerUUID = PlayerData.getString("PlayerUUID");
		} catch (SQLException e) {
			e.printStackTrace();
			SQLData.Close();
			return null;
		}
		if(playerUUID == null) {
			SQLData.Close();
			return null;
		}
		
		if(PlayerMap.containsKey(playerUUID)) {
			PlayerMap.remove(playerUUID);
		}
		PlayerMap.put(playerUUID, new PlayerInfo(PlayerData, PlayerDataMap));
		return PlayerMap.get(playerUUID);
	}

	public void onDisable() {
		SQLData.Close();
	}
}
