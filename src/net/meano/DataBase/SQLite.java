package net.meano.DataBase;

import net.meano.PlayerManager.BukkitMain;
import net.meano.PlayerManager.BungeeMain;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class SQLite implements DBManager{
	private File DataBaseFile;
	private Connection DataBaseConnection;
	public BukkitMain PMM = BukkitMain.Instance;

	public BukkitMain PMBukkit = BukkitMain.Instance;
	public BungeeMain PMBungee = BungeeMain.Instance;
	
	public SQLite(File dbFile) {
		//PMM = plugin;
		DataBaseFile = dbFile; // 数据库文件
		File dbDir = dbFile.getParentFile(); // 获取文件路径
		dbDir.mkdir(); // 创建文件夹
		if (!dbFile.exists()) { // 如果数据库文件不存在
			try {
				dbFile.createNewFile(); // 创建数据库文件
			} catch (IOException e) {
			}
		}
		try {
			Class.forName("org.sqlite.JDBC"); // 载入sqlite类
		} catch (ClassNotFoundException e) {
		}
		Open(); // 数据库链接打开

		try {
			// 数据库操作
			Statement DataBaseStatement = DataBaseConnection.createStatement();
			// 超时设置30s
			DataBaseStatement.setQueryTimeout(30);
		} catch (SQLException e) {
			PMM.getLogger().info(e.getLocalizedMessage());
		}
	}

	@Override
	public void LogInfo(String msg) {
		if(PMBukkit != null)
			LogInfo(msg);

		if(PMBungee != null) 
			PMBungee.getLogger().info(msg);
	}

	public void Open() {
		try {
			this.DataBaseConnection = DriverManager.getConnection("jdbc:sqlite:" + DataBaseFile.getPath());
		} catch (SQLException e) {
			PMM.getLogger().info("连接SQLlite数据库失败！" + e.getMessage());
		}
	}

	public void Close() {
		try {
			if (DataBaseConnection != null && !DataBaseConnection.isClosed())
				DataBaseConnection.close();
		} catch (SQLException e) {
		}
	}

	// 查找有无此玩家
	public boolean HasPlayer(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next())
				return true;
			else
				return false;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public ResultSet GetPlayerInfo(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next())
				return result;
			else
				return null;
		} catch (SQLException e) {
			return null;
		}
	}
	
	public void UpdatePlayerUUID(String PlayerName, String PlayerUUID) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET PlayerUUID=? WHERE PlayerName=?;");
			ps.setString(1, PlayerUUID);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	public Connection getConnection() {
		return this.DataBaseConnection;
	}

	@Override
	public boolean UpdatePlayerInfo(String playerUUID, Map<String, Object> playerMap, List<String> updateList) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean CostGem(String playerUUID, int Gem, String Message) {
		return false;
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ChargeGem(String playerUUID, int chargeGem, String message) {
		// TODO Auto-generated method stub
		return false;
	}
}
