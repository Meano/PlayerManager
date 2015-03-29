package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

public class PlayerManagerListeners implements Listener {
	PlayerManagerMain PPM;
	PermissionsPlugin Perm;

	public PlayerManagerListeners(PlayerManagerMain GetPlugin) {
		PPM = GetPlugin;
		Perm = (PermissionsPlugin) Bukkit.getPluginManager().getPlugin("PermissionsBukkit");
	}
	
	//玩家预登陆事件
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		String PreLoginIP = event.getAddress().getHostName();
		String PlayerName = event.getName();
		OfflinePlayer player = Bukkit.getOfflinePlayer(event.getUniqueId());
		// 白名单
		if (!player.isWhitelisted()) {
			PPM.getLogger().info("玩家不在白名单中");
			for (String p : PPM.SetWhitelist) {
				if (p.equalsIgnoreCase(PlayerName)) {
					Bukkit.broadcast(p + "在白名单中预订列表中，成功添加白名单！", "PlayerManager.Whitelist");
					Bukkit.getOfflinePlayer(event.getUniqueId()).setWhitelisted(true);
				}
			}
		}
		boolean isOnline = false;
		String OnlinePlayerName = null;
		for (Player P : PPM.getServer().getOnlinePlayers()) {
			if (P.getAddress().getHostName().equals(PreLoginIP)) {
				OnlinePlayerName = P.getName();
				if(!OnlinePlayerName.equals(event.getName())){
					isOnline = true;
					continue;
				}
			}
		}
		if (isOnline) {
			Bukkit.broadcast("玩家" + OnlinePlayerName + "试图用" + event.getName() + "登陆游戏，有小号登陆嫌疑，ip地址" + PreLoginIP, "PlayerManager.Warn");
		}
		PPM.SQLData.Close();
		PPM.SQLData.Open();
		// 存在此玩家
		if (PPM.SQLData.HasPlayer(PlayerName)) {
			if (PPM.SQLData.GetComboType(PlayerName).equals("Normal")) {
				if ((PPM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PPM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
					event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "亲爱的免费玩家，您的免费时长和为服务器做任务获得的时长已经用完，您可选择购买服务器无限时套餐，或等待6点或18点的时长更新重新登陆游戏。");
				}
			} else if (PPM.SQLData.GetComboType(PlayerName).equals("B")) {
				String Week = PPM.getWeekString(System.currentTimeMillis());
				if (Week.equals("星期日") || Week.equals("星期六") || Week.equals("星期五")) {
				} else {
					if ((PPM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PPM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
						event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "亲爱的套餐B玩家，今天是" + Week + "，您的免费时长和为服务器做任务获得的时长已经用完，您可选择补差价购买服务器无限时套餐A，或等待6点或18点的时长更新重新登陆游戏。");
					}
				}
			}
		}
	}
	
	//玩家登陆游戏事件
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		String PlayerName = event.getPlayer().getName();
		String PlayerCombo = null;
		boolean FirstPlay = false;
		// 白名单
		for (int i = 0; i < 3; i++) {
			if (PPM.SetWhitelist[i].equalsIgnoreCase(PlayerName)) {
				PPM.SetWhitelist[i] = "Meano";
				event.getPlayer().setWhitelisted(true);
				PPM.getLogger().info("白名单验证开始处理： " + PlayerName + " 加入白名单！");
			}
		}
		PPM.SQLData.Close();
		PPM.SQLData.Open();
		if (!PPM.SQLData.HasPlayer(event.getPlayer().getName())) {
			PPM.SQLData.AddNewPlayer(event.getPlayer().getName(), event.getPlayer().getUniqueId().toString());
			event.getPlayer().sendMessage(ChatColor.AQUA + "亲爱的玩家，你好！因服务器的运行需要大量的时间和金钱进行维护，现改变制度。");
			event.getPlayer().sendMessage(ChatColor.AQUA + "为服务器长久发展，又不破坏游戏内公平，限定免费玩家每天有4小时游戏时间。");
			event.getPlayer().sendMessage(ChatColor.AQUA + "之前捐助过服务器的将获得专享套餐，长期在服务器游戏的玩家可以选择以下两种套餐：");
			event.getPlayer().sendMessage(ChatColor.GREEN + "套餐A：25元/月，一个月内游戏不限时，并给予当月" + ChatColor.YELLOW + "10次称号更改和2次皮肤修改。");
			event.getPlayer().sendMessage(ChatColor.GREEN + "套餐B：12元/月，一个月内周五六日不限时，并给予当月" + ChatColor.YELLOW + "3次称号更改。");
			PPM.getLogger().info(PlayerName + " 新添加入数据库");
		} else {
			FirstPlay = PPM.SQLData.isTodayFirstPlay(PlayerName);
			if (FirstPlay) { // 日第一次登陆
				PPM.getLogger().info(PlayerName + " 今天第一次登陆，距上一次登陆已有 " + PPM.SQLData.CalculateDaysLast(PlayerName) + " 天。");
				PPM.SQLData.UpdateTodayFirstLogin(PlayerName);
			} else {
				PPM.getLogger().info(PlayerName + "今日多次登陆，剩余可用在线时间 " + PPM.SQLData.GetTodayLimitMinute(PlayerName) + "分。");
			}
			boolean ComboSuit = false;
			PlayerCombo = PPM.SQLData.GetComboType(PlayerName);
			if (PlayerCombo.equals("Normal")) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "亲爱的免费玩家，你好！服务器的运行需要大量的时间和金钱进行维护。");
				event.getPlayer().sendMessage(ChatColor.GREEN + "为了服务器长久发展，又不破坏游戏内公平，限定免费玩家每天4小时的游戏时间");
				event.getPlayer().sendMessage(ChatColor.GREEN + "到下次6点或18点时长更新前还剩余" + PPM.SQLData.GetTodayLimitMinute(PlayerName) + "分钟游戏时间");
				event.getPlayer().sendMessage(ChatColor.GREEN + "可以通过支付宝，微信，电话卡支付不限时套餐，详询群326355263。使用/pm me查询时长。");
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
			} else if (PlayerCombo.equals("A")) {
				long ExpireTime = PPM.SQLData.GetComboExpireTime(PlayerName);
				if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
					PPM.SQLData.SetComboType(PlayerName, "Normal");
					PPM.SQLData.SetTodayLimitMinute(PlayerName, 120);
					event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的A套餐玩家，你好！感谢您对服务器的支持与付出！");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有4小时免费游戏时间，祝玩的愉快！");
				} else {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的A套餐玩家，你好！感谢您对服务器的支持与付出！");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "您的套餐到期日为:" + PPM.getDateString(ExpireTime) + "，祝玩的愉快！");
				}
				// 套餐A权限处理
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboA")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboA");
			} else if (PlayerCombo.equals("B")) {
				long ExpireTime = PPM.SQLData.GetComboExpireTime(PlayerName);
				if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
					PPM.SQLData.SetComboType(PlayerName, "Normal");
					PPM.SQLData.SetTodayLimitMinute(PlayerName, 120);
					event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的B套餐玩家，你好！感谢您对服务器的支持与付出！");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有4小时免费游戏时间，祝玩的愉快！");
				} else {
					String Week = PPM.getWeekString(System.currentTimeMillis());
					if (Week.equals("星期日") || Week.equals("星期六") || Week.equals("星期五")) {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的B套餐玩家，你好！感谢您对服务器的支持与付出！");
						event.getPlayer().sendMessage(ChatColor.YELLOW + "您的套餐到期日为:" + PPM.getDateString(ExpireTime) + "，祝玩的愉快！");
					} else {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的B套餐玩家，你好！感谢您对服务器的支持与付出！");
						event.getPlayer().sendMessage(ChatColor.YELLOW + "今天是工作日，您只有免费游戏时间，剩余免费时间：" + PPM.SQLData.GetTodayLimitMinute(PlayerName) + "分钟，祝玩的愉快！");
					}
				}
				// 套餐B权限处理
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboB")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboB");
			} else if (PlayerCombo.equals("C")) {
				long ExpireTime = PPM.SQLData.GetComboExpireTime(PlayerName);
				long CurrentTime = System.currentTimeMillis();
				long SpaceTime = CurrentTime - event.getPlayer().getLastPlayed();
				if (SpaceTime > 1000 * 60 * 60 * 24) {
					ExpireTime += SpaceTime - 1000 * 60 * 60 * 24;
					PPM.SQLData.SetComboExpireTime(event.getPlayer().getName(), ExpireTime);
					event.getPlayer().sendMessage("C套餐玩家" + event.getPlayer().getName() + "延时" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "天");
					PPM.getLogger().info("C套餐玩家" + event.getPlayer().getName() + "延时" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "天");
				}
				if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
					PPM.SQLData.SetComboType(PlayerName, "Normal");
					PPM.SQLData.SetTodayLimitMinute(PlayerName, 120);
					event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的C套餐玩家，你好！感谢您对服务器的支持与付出！");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有4小时免费游戏时间");
				} else {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "亲爱的C套餐玩家，你好！感谢您对服务器的支持与付出！");
					event.getPlayer().sendMessage(ChatColor.YELLOW + "您的套餐到期日为:" + PPM.getDateString(ExpireTime) + "，祝玩的愉快。");
				}
				// 套餐C权限处理
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboC")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboC");
			} else if (PlayerCombo.equals("Forever")) {
				event.getPlayer().sendMessage(ChatColor.RED + "亲爱的永久玩家，你好！感谢您对服务器的支持与付出！");
				event.getPlayer().sendMessage(ChatColor.RED + "您可以享受永久无限时的玩乐，祝玩的愉快。");
				// 套餐Forever权限处理
				for (Group GroupofPlayer : Perm.getGroups(event.getPlayer().getUniqueId())) {
					if (GroupofPlayer.getName().contains("Combo")) {
						if (GroupofPlayer.getName().equals("ComboA")) {
							ComboSuit = true;
							continue;
						}
						Perm.RemoveGroup(event.getPlayer(), GroupofPlayer.getName());
					}
				}
				if (!ComboSuit)
					Perm.AddGroup(event.getPlayer(), "ComboA");
			}
		}
	}
	
	public void CalculateContinuousDays(Player player){
		String PlayerName = player.getName();
		if(PPM.SQLData.GetOnlineMinutes(PlayerName)>120){
			int ContinuousDays = PPM.SQLData.GetContinuousDays(PlayerName);		//连续登陆天数
			int AwardMinute = PPM.SQLData.GetAwardMinute(PlayerName);			//奖励分钟数
			player.sendMessage(ChatColor.GREEN + "玩家你好，你已连续使用服务器专用客户端进行登陆并在线时长超过两小时 " + ContinuousDays + "天。");
			if(ContinuousDays < 7){
				player.sendMessage(ChatColor.GREEN + "获得奖励在线时间 " + ContinuousDays*5 + " 分钟。");
				PPM.SQLData.SetAwardMinute(PlayerName, AwardMinute+5*ContinuousDays);
			}else{
				player.sendMessage(ChatColor.GREEN + "获得奖励在线时间 30 分钟。");
				PPM.SQLData.SetAwardMinute(PlayerName, AwardMinute+30);
			}
			//持续登陆天数+1
			PPM.SQLData.SetContinuousDays(PlayerName, ContinuousDays+1);
		}else {
			//断签
			player.sendMessage(ChatColor.GREEN + "玩家你好，昨天你未使用服务器专用客户端进行登陆并在线时长超过两小时，连续登陆天数清零。");
			PPM.SQLData.SetContinuousDays(PlayerName, 0);
		}
		player.sendMessage(ChatColor.GREEN + "连续每天使用专用客户端登陆并且在线时间超过2小时将获得(连续登陆天数x5)分钟的奖励时间。");
		player.sendMessage(ChatColor.GREEN + "奖励时间每周日晚六点清零，请尽快使用。");
	}
	//玩家退出游戏事件
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String PlayerName = event.getPlayer().getName();
		if(PPM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			PPM.SQLData.SetClientStatu(PlayerName, ClientStatu.Offline);
			PPM.getLogger().info("使用专用客户端的玩家: " + PlayerName + "已经离线。");
		}
	}
}
