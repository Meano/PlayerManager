package net.meano.PlayerManager;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class BukkitListeners implements Listener {
	BukkitMain PM;

	//初始化
	public BukkitListeners(BukkitMain GetPlugin) {
		PM = GetPlugin;
		//Perm = (PermissionsPlugin) Bukkit.getPluginManager().getPlugin("PermissionsBukkit");
	}
	
	// 玩家预登陆事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) throws SQLException, InterruptedException {
		String PlayerName = event.getName();
		String PlayerUUID = event.getUniqueId().toString();

		PM.SQLData.Open();
		ResultSet PlayerData = PM.SQLData.GetPlayerInfo(PlayerName);
		if(PlayerData == null) {
			event.disallow(Result.KICK_OTHER, "未注册玩家，请使用服务器专用客户端注册！");
			return;
		}

		if(PM.PlayerMap.containsKey(PlayerUUID)) {
			PM.PlayerMap.remove(PlayerUUID);
		}
		
		PM.PlayerMap.put(PlayerUUID, new PlayerInfo(PlayerData, PM.PlayerDataMap));
		PM.SQLData.Close();
	}
	
	// 玩家加入游戏事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
		String PlayerUUID = event.getPlayer().getUniqueId().toString();
		PlayerInfo playerInfo = PM.PlayerMap.get(PlayerUUID);
		if(playerInfo == null) {
			return;
		}

		int Gem = playerInfo.GetInteger("Gem");
		event.getPlayer().sendMessage(ChatColor.GOLD + "[积分] 可用积分" + Gem + "。");
		playerInfo.SetRedisKey("player.status", "joined");
		
		DecimalFormat df = new DecimalFormat("0.00");
		int viewDistance = Math.max(Math.min(playerInfo.GetInteger("ViewDistance"), 16), 0);
		if(Gem == 0) {
			if(viewDistance > 6) {
				event.getPlayer().sendMessage(ChatColor.GOLD + "[视界] 积分不足不可修改可视区块大小。");
			}
			viewDistance = 6;
		}
		if(viewDistance > 6) {
			event.getPlayer().sendMessage(
				ChatColor.GOLD +
				"[视界] 可视" + viewDistance + "区块，消耗" + df.format((viewDistance * viewDistance / 36.0)) + "积分/小时" +
				(viewDistance > 10 ? "，建议不要在玩家较多时提升视界。" : "。")
			);
			PM.getLogger().info(event.getPlayer().getName() + " Set ViewDistance to " + viewDistance);
		}
		// TODO
//		event.getPlayer().setViewDistance(viewDistance);
		
		playerInfo.setLoginTime();
		//PM.getLogger().info(event.getPlayer().getName() + " login at " + playerInfo.getLoginTime().toString());
	}
	
	// 玩家退出游戏事件
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
		String PlayerUUID = event.getPlayer().getUniqueId().toString();
		PlayerInfo playerInfo = PM.PlayerMap.get(PlayerUUID);
		if(playerInfo == null) {
			return;
		}
		
		// TODO
		int viewDistance = 6;//event.getPlayer().getViewDistance();
		DecimalFormat df = new DecimalFormat("0.00");
		double onlineTimeHour =  playerInfo.getOnlineTime() / (1000 * 60 * 60.0);
		double viewDistanceCostDouble = (viewDistance > 6) ? (viewDistance * viewDistance / 36.0) * onlineTimeHour : 0;
		String viewDistanceCostBefore = playerInfo.GetRedisKey("player.ViewDistanceCost");
		viewDistanceCostDouble += Double.parseDouble((viewDistanceCostBefore == null || viewDistanceCostBefore == "") ? "0" : viewDistanceCostBefore);
		
		int viewDistanceCost = (int)viewDistanceCostDouble;
		PM.getLogger().info(
			String.format(
				"View distance %s, Online time %s h, Cost %s, Remain Cost %s.",
				viewDistance,
				df.format(onlineTimeHour),
				df.format(viewDistanceCostDouble),
				df.format(viewDistanceCostDouble % 1.0)
			)
		);
		if(viewDistanceCost > 0) {
			PM.SQLData.CostGem(PlayerUUID, viewDistanceCost, "视界" + viewDistance + ",时长" + df.format(onlineTimeHour) + "h.");
		}
		playerInfo.SetRedisKey("player.ViewDistanceCost", df.format(viewDistanceCostDouble % 1.0));

		PM.PlayerMap.remove(PlayerUUID);
	}
}
