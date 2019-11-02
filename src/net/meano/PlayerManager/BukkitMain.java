package net.meano.PlayerManager;

import net.meano.DataBase.DBManager;
import net.meano.DataBase.MySQL;
import net.meano.DataBase.SQLite;
import net.meano.PlayerServer.Server;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.util.*;

public class BukkitMain extends JavaPlugin {
	public static BukkitMain Instance;

	public FileConfiguration Config = getConfig();
	
	public DBManager SQLData;
	public JedisPool RedisPool;
	
	public Map<String, PlayerInfo> PlayerMap = new HashMap<String, PlayerInfo>();
	public Map<String, String> PlayerDataMap = new HashMap<String, String>();
	
	public Server PlayerSocket;
	public boolean isUpdate = false;

	public void onEnable() {
		Instance = this;
		File PluginConfigFile = new File(getDataFolder(), "config.yml");
		if (!PluginConfigFile.exists()) {
			saveDefaultConfig();
		}

		String redisHost = Config.getString("Redis.Host", "localhost");
		int redisPort = Config.getInt("Redis.Port", 6379);
		String redisPassword = Config.getString("Redis.Password", "");
		RedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 2000, redisPassword);
		if(RedisPool == null) {
			getLogger().warning("Redis init failed!");
		}

		for(String key : Config.getConfigurationSection("PlayerDBMap").getKeys(false)) {
			PlayerDataMap.put(key, Config.getString("PlayerDBMap." + key));
		}

		String SQLHost = Config.getString("MySQL.Host", "localhost");
		String SQLPort = String.valueOf(Config.getInt("MySQL.Port", 3306));
		String SQLDatabase = Config.getString("MySQL.Database", "Minecraft");
		String SQLUsername = Config.getString("MySQL.Username", "root");
		String SQLPassword = Config.getString("MySQL.Password", "");
		String SQLTable = Config.getString("MySQL.Table", "PMPlayers");

		if (!Config.getBoolean("MySQL.Enable")) {
			SQLData = new SQLite(new File(getDataFolder(), "PMData.db"));
		} else {
			SQLData = new MySQL(SQLHost, SQLPort, SQLDatabase, SQLUsername, SQLPassword, SQLTable);
		}
		
		Bukkit.getServer().getPluginManager().registerEvents(new BukkitListeners(this), this);
	}

	public void onDisable() {
		SQLData.Close();
	}

	public String getDateString(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		@SuppressWarnings("resource")
		Formatter ft = new Formatter(Locale.CHINA);
		return ft.format("%1$tY年%1$tm月%1$td日", cal).toString().replaceAll(":", "_");
	}

	public int getTimeMinutes(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return cal.get(Calendar.MINUTE);
	}

	public int getTimeHours(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public String getWeekString(long dt) {
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dt);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return weekDays[w];
	}
}
