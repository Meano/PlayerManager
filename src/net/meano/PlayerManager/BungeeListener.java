package net.meano.PlayerManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BungeeListener implements Listener {
	BungeeMain PM;

	BungeeListener(BungeeMain getPlugin) {
		PM = getPlugin;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLoginEvent(LoginEvent event) throws SQLException, InterruptedException {
		String playerName = event.getConnection().getName();
		String playerUUID = event.getConnection().getUniqueId().toString();
		TextComponent kickText = new TextComponent();
		kickText.setColor(ChatColor.RED);

		try{
			PM.SQLData.Open();
			ResultSet PlayerData = PM.SQLData.GetPlayerInfo(playerName);
			if (PlayerData == null) {
				PM.getLogger().info("未注册玩家！");
				kickText.setText("未注册玩家！请使用服务器专用客户端注册和登陆！");
				event.setCancelled(true);
				event.setCancelReason(kickText);
				PM.SQLData.Close();
				return;
			}

			if (PlayerData.getString("playerUUID") == null) {
				PM.SQLData.UpdatePlayerUUID(playerName, playerUUID);
			}

			if (PM.PlayerMap.containsKey(playerUUID)) {
				PM.PlayerMap.remove(playerUUID);
			}

			PM.PlayerMap.put(playerUUID, new PlayerInfo(PlayerData, PM.PlayerDataMap));

			PlayerInfo playerInfo = PM.PlayerMap.get(playerUUID);
			String Session = playerInfo.GetRedisKey("player.session");
			if(Session == null) {
				kickText.setText("请使用服务器专用客户端登录！");
				event.setCancelled(true);
				event.setCancelReason(kickText);
				PM.SQLData.Close();
				return;
			}
			for(int i = 0; i < 10; i++) {
				String Status = playerInfo.GetRedisKey("player.status");
				if(Status != null && Status.equals("join")) {
					PM.getLogger().info(playerName + " 登录成功，耗时：" + (1 + i) * i * 50 + "ms.");
					return;
				}
				Thread.sleep(100 + i * 100);
			}
			kickText.setText("登录超时，请稍后或重启客户端后重试！");
			event.setCancelled(true);
			event.setCancelReason(kickText);
			PM.SQLData.Close();
		}
		catch (Exception e){
			PM.getLogger().info(e.getMessage());
			kickText.setText("服务器问题，请报告管理员！");
			event.setCancelled(true);
			event.setCancelReason(kickText);
			PM.SQLData.Close();
		}
	}

	public static void setField(Class<?> clazz, Object instance, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field f = clazz.getDeclaredField(field);
		f.setAccessible(true);
		f.set(instance, value);
	}

	@EventHandler
	public void onPlayerPostLoginEvent(PostLoginEvent event) throws NoSuchFieldException, IllegalAccessException {
		//PM.getLogger().info("post uuid:" + event.getPlayer().getPendingConnection().getUniqueId().toString());
	}

	@EventHandler
	public void onPlayerQuitEvent(PlayerDisconnectEvent event) {
		String PlayerUUID = event.getPlayer().getUniqueId().toString();
		PM.PlayerMap.remove(PlayerUUID);
	}
}