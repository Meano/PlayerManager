package net.meano.DataBase;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

public interface DBManager {
	public void Open();
	public void Close();
	
	public void LogInfo(String msg);

	public boolean HasPlayer(String playerName);
	public ResultSet GetPlayerInfo(String player);
	public void UpdatePlayerUUID(String playerName, String PlayerUUID);
	public boolean UpdatePlayerInfo(String playerUUID, Map<String, Object> playerMap, List<String> updateList);
	public boolean ChargeGem(String playerUUID, int chargeGem, String message);
	public boolean CostGem(String playerUUID, int costGem, String message);
}
