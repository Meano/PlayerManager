package net.meano.DataBase;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface DBManager {
	public void Open();
	public void Close();
	
	public void LogInfo(String msg);

	public boolean HasPlayer(String playerName);
	public ResultSet GetPlayerInfo(String playerName);
	public void UpdatePlayerUUID(String playerName, String PlayerUUID);
	public boolean UpdatePlayerInfo(String playerUUID, Map<String, Object> playerMap, List<String> updateList);
	public boolean ChargeGem(String playerUUID, int chargeGem, String message);
	public boolean CostGem(String playerUUID, int costGem, String message);
	
/*
	public void SetComboExpireTime(String playerName, long expireTime);
	public void SetComboType(String playerName, String comboType);
	public int GetTodayLimitMinute(String playerName);
	public String GetComboType(String playerName);
	public long GetComboExpireTime(String playerName);
	public int GetAwardMinute(String playerName);
	public int GetContinuousDays(String playerName);
	public int GetOnlineMinutes(String playerName);
	public void SetTodayLimitMinute(String playerName, int parseInt);
	public void SetAwardMinute(String playerName, long awardMinute);
	public void UpdateLimitTime(int parseInt);
	public void UpdateAwardTime(int parseInt);
	public void UpdateNormalAwardTime(int parseInt);
	public void SetSpawnPoint(String playerName, Location spawnLocation);
	public ClientStatu GetClientStatu(String playerName);
	public void SetOnlineMinutes(String playerName, long i);
	public void AddNewPlayer(String playerName, String string);
	public Location GetSpawnPoint(String playerName);
	public int CalculateDaysLast(String playerName);
	public boolean isTodayFirstPlay(String playerName);
	public void UpdateTodayFirstLogin(String playerName);
	public void SetContinuousDays(String playerName, long continuousDays);
	public void SetClientStatu(String playerName, ClientStatu offline);
*/
}
