package net.meano.PlayerManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeListener implements Listener {
	BungeeMain PM;
	BungeeListener(BungeeMain GetPlugin){
		PM = GetPlugin;
	}

    @EventHandler(priority = EventPriority.LOWEST)
	public void onLoginEvent(LoginEvent event) throws SQLException, InterruptedException {
		String PlayerName = event.getConnection().getName();
		String PlayerUUID = event.getConnection().getUniqueId().toString();

		PM.SQLData.Open();
		ResultSet PlayerData = PM.SQLData.GetPlayerInfo(PlayerName);
		if(PlayerData == null) {
			PM.getLogger().info("未注册玩家！");
			return;
		}
		
		if(PlayerData.getString("PlayerUUID") == null) {
			PM.SQLData.UpdatePlayerUUID(PlayerName, PlayerUUID);
		}

		if(PM.PlayerMap.containsKey(PlayerUUID)) {
			PM.PlayerMap.remove(PlayerUUID);
		}
		PM.PlayerMap.put(PlayerUUID, new PlayerInfo(PlayerData, PM.Redis, PM.PlayerDataMap));

		PM.getLogger().info(PlayerName + " is logging in.");
		PM.SQLData.Close();
	}
    
	@EventHandler
	public void onPlayerQuitEvent(PlayerDisconnectEvent event) {
		//String PlayerUUID = event.getPlayer().getUniqueId().toString();
		//PM.PlayerMap.remove(PlayerUUID);
	}
}