package net.meano.DataBase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.TimeZone;
import net.meano.PlayerManager.PlayerManagerMain;

public class SQLite{
	private File DataBaseFile;
	private Connection DataBaseConnection;
	public PlayerManagerMain PMM;

	public SQLite(File dbFile, PlayerManagerMain plugin) {
		PMM = plugin;
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
			// 运行命令 如果表不存在则建立表PMPlayers，存储玩家列表
			DataBaseStatement.executeUpdate(	"CREATE TABLE IF NOT EXISTS PMPlayers "
										+ "(PlayerName VARCHAR(30) NOT NULL UNIQUE, "
										+ "UUID VARCHAR(130) NOT NULL UNIQUE, "
										+ "PlayerLevel INT NOT NULL, "
										+ "TodayFirstLogin INTEGER NOT NULL, "
										+ "ComboType VARCHAR(10) NOT NULL, "
										+ "TodayLimitMinute INT NOT NULL, "
										+ "ComboExpireTime INTEGER NOT NULL, "
										+ "ClientStatu VARCHAR(10) NOT NULL, "
										+ "ClientNoCheck VARCHAR(10) NOT NULL, "
										+ "AwardMinute INT NOT NULL," 
										+ "ContinuousDays INT NOT NULL,"
										+ "OnlineMinutes INT NOT NULL);");
			DatabaseMetaData md = DataBaseConnection.getMetaData();
			ResultSet rs = md.getColumns(null, null, "PMPlayers", "ContinuousDays");
			if (!rs.next()) {
				PMM.getLogger().info("正在插入新列ContinuousDays。");
				PreparedStatement ps = DataBaseConnection.prepareStatement("ALTER TABLE PMPlayers ADD ContinuousDays INT NOT NULL DEFAULT ( 0 );");
				ps.executeUpdate();
				PMM.getLogger().info("新列ContinuousDays插入完成。");
			}
			rs = md.getColumns(null, null, "PMPlayers", "OnlineMinutes");
			if (!rs.next()) {
				PMM.getLogger().info("正在插入新列OnlineMinutes。");
				PreparedStatement ps = DataBaseConnection.prepareStatement("ALTER TABLE PMPlayers ADD OnlineMinutes INT NOT NULL DEFAULT ( 0 );");
				ps.executeUpdate();
				PMM.getLogger().info("新列OnlineMinutes插入完成。");
			}
			rs = md.getColumns(null, null, "PMPlayers", "AwardMinute");
			if (!rs.next()) {
				PMM.getLogger().info("正在插入新列AwardMinute。");
				PreparedStatement ps = DataBaseConnection.prepareStatement("ALTER TABLE PMPlayers ADD AwardMinute INT NOT NULL DEFAULT ( 0 );");
				ps.executeUpdate();
				PMM.getLogger().info("新列AwardMinute插入完成。");
			}
		} catch (SQLException e) {
			PMM.getLogger().info(e.getLocalizedMessage());
		}
	}

	public void Open() {
		try {
			this.DataBaseConnection = DriverManager.getConnection("jdbc:sqlite:" + DataBaseFile.getPath());
		} catch (SQLException e) {

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

	// 添加玩家
	public void AddNewPlayer(String PlayerName, String UUID) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("INSERT INTO PMPlayers"
													+ "(PlayerName, " 
													+ "UUID, " 
													+ "PlayerLevel, " 
													+ "TodayFirstLogin, " 
													+ "ComboType," 
													+ "TodayLimitMinute, " 
													+ "ComboExpireTime, " 
													+ "ClientStatu, " 
													+ "ClientNoCheck, " 
													+ "AwardMinute,"
													+ "ContinuousDays," 
													+ "OnlineMinutes)" 
													+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?);");
			ps.setString(1, PlayerName.toLowerCase());
			ps.setString(2, UUID);
			ps.setInt(3, 0);
			ps.setLong(4, System.currentTimeMillis());
			ps.setString(5, "Normal");
			ps.setInt(6, 120);
			ps.setLong(7, 0);
			ps.setString(8, "Offline");
			ps.setString(9, "false");
			ps.setInt(10, 0);
			ps.setInt(11, 0);
			ps.setInt(12, 0);
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 获取日首次登陆时间
	public long GetTodayFirstLogin(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getLong("TodayFirstLogin");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}
	//计算距上一次登陆相差的时间
	public int CalculateDaysLast(String PlayerName){
		long TimeTodayFirstLogin = GetTodayFirstLogin(PlayerName);
		return CalculateDaysDiff(System.currentTimeMillis(),TimeTodayFirstLogin);
	}
	
	//判断是否为日首次登陆
	public boolean isTodayFirstPlay(String PlayerName) {
		if (CalculateDaysLast(PlayerName)<= 0) {
			return false;
		} else {
			return true;
		}
	}
	//计算两个long time的天数之差
	public int CalculateDaysDiff(long TimeFirst,long TimeSecond){
		return CalculateDays(TimeFirst)-CalculateDays(TimeSecond);
	}
	
	//计算long time所属天数
	public int CalculateDays(long TimeToCalculate){
		Calendar CalculateDate= Calendar.getInstance();
		CalculateDate.setTimeInMillis(TimeToCalculate);
		CalculateDate.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return CalculateDate.get(Calendar.DAY_OF_YEAR)+(CalculateDate.get(Calendar.YEAR)*1000);
	}

	// 更新日首次登陆时间
	public void UpdateTodayFirstLogin(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET TodayFirstLogin=? WHERE PlayerName=?;");
			ps.setLong(1, System.currentTimeMillis());
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 获取套餐类型
	public String GetComboType(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getString("ComboType");
			} else
				return null;
		} catch (SQLException e) {
			return null;
		}
	}

	// 设置套餐类型
	public void SetComboType(String PlayerName, String ComboType) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ComboType=? WHERE PlayerName=?;");
			ps.setString(1, ComboType);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 获得当天限时时间
	public int GetTodayLimitMinute(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("TodayLimitMinute");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// 设置当天限长时间
	public void SetTodayLimitMinute(String PlayerName, int TodayLimitMinute) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET TodayLimitMinute=? WHERE PlayerName=?;");
			ps.setInt(1, TodayLimitMinute);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 获得套餐天数
	public long GetComboExpireTime(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getLong("ComboExpireTime");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// 设定套餐天数
	public void SetComboExpireTime(String PlayerName, long ExpireTime) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ComboExpireTime=? WHERE PlayerName=?;");
			ps.setLong(1, ExpireTime);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 获得套餐天数
	public int GetAwardMinute(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("AwardMinute");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// 设定套餐天数
	public void SetAwardMinute(String PlayerName, long MinuteTime) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET AwardMinute=? WHERE PlayerName=?;");
			ps.setLong(1, MinuteTime);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 刷新免费玩家时间
	public void UpdateLimitTime(int min) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET TodayLimitMinute=? WHERE ComboType=? or ComboType=?;");
			ps.setInt(1, min);
			ps.setString(2, "Normal");
			ps.setString(3, "B");
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}

	// 刷新AC玩家奖励时间
	public void UpdateAwardTime(int min) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET AwardMinute=? WHERE ComboType in(?,?,?);");
			ps.setInt(1, min);
			ps.setString(2, "A");
			ps.setString(3, "C");
			ps.setString(4, "Forever");
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	//更改客户端在线状态
	public void SetClientStatu(String PlayerName,ClientStatu Statu) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ClientStatu=? WHERE PlayerName=?;");
			ps.setString(1, Statu.name());
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	//获取客户端在线状态
	public ClientStatu GetClientStatu(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return ClientStatu.valueOf(result.getString("ClientStatu"));
			} else
				return null;
		} catch (SQLException e) {
			return null;
		}
	}
	
	// 获得连续登陆天数
	public int GetContinuousDays(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("ContinuousDays");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// 设定连续登陆天数
	public void SetContinuousDays(String PlayerName, long ContinuousDays) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET ContinuousDays=? WHERE PlayerName=?;");
			ps.setLong(1, ContinuousDays);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	// 获得专用客户端在线分钟数
	public int GetOnlineMinutes(String PlayerName) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("SELECT * FROM PMPlayers WHERE PlayerName=?;");
			ps.setString(1, PlayerName.toLowerCase());
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return result.getInt("OnlineMinutes");
			} else
				return -1;
		} catch (SQLException e) {
			return -1;
		}
	}

	// 设定专用客户端在线分钟数
	public void SetOnlineMinutes(String PlayerName, long OnlineMinutes) {
		try {
			PreparedStatement ps = DataBaseConnection.prepareStatement("UPDATE PMPlayers SET OnlineMinutes=? WHERE PlayerName=?;");
			ps.setLong(1, OnlineMinutes);
			ps.setString(2, PlayerName.toLowerCase());
			ps.executeUpdate();
		} catch (SQLException e) {

		}
	}
	
	public Connection getConnection() {
		return this.DataBaseConnection;
	}
}
