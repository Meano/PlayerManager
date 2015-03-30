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
	PlayerManagerMain PMM;
	PermissionsPlugin Perm;
	
	//初始化
	public PlayerManagerListeners(PlayerManagerMain GetPlugin) {
		PMM = GetPlugin;
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
			PMM.getLogger().info("玩家不在白名单中");
			for (String p : PMM.SetWhitelist) {
				if (p.equalsIgnoreCase(PlayerName)) {
					Bukkit.broadcast(p + "在白名单中预订列表中，成功添加白名单！", "PlayerManager.Whitelist");
					Bukkit.getOfflinePlayer(event.getUniqueId()).setWhitelisted(true);
				}
			}
		}
		boolean isOnline = false;
		String OnlinePlayerName = null;
		for (Player P : PMM.getServer().getOnlinePlayers()) {
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
		PMM.SQLData.Close();
		PMM.SQLData.Open();
		// 存在此玩家
		if (PMM.SQLData.HasPlayer(PlayerName)) {
			if (PMM.SQLData.GetComboType(PlayerName).equals("Normal")) {
				if ((PMM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PMM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
					event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "亲爱的免费玩家，您的免费时长和为服务器做任务获得的时长已经用完，您可选择购买服务器无限时套餐，或等待6点或18点的时长更新重新登陆游戏。");
				}
			} else if (PMM.SQLData.GetComboType(PlayerName).equals("B")) {
				String Week = PMM.getWeekString(System.currentTimeMillis());
				if (Week.equals("星期日") || Week.equals("星期六") || Week.equals("星期五")) {
				} else {
					if ((PMM.SQLData.GetTodayLimitMinute(PlayerName) <= 0) && (PMM.SQLData.GetAwardMinute(PlayerName) <= 0)) {
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
		Player player = event.getPlayer();
		boolean FirstPlay = false;
		int ContinuousDays = -1;
		// 白名单
		for (int i = 0; i < 3; i++) {
			if (PMM.SetWhitelist[i].equalsIgnoreCase(PlayerName)) {
				PMM.SetWhitelist[i] = "Meano";
				player.setWhitelisted(true);
				PMM.getLogger().info("白名单验证开始处理： " + PlayerName + " 加入白名单！");
			}
		}
		PMM.SQLData.Close();
		PMM.SQLData.Open();
		if (!PMM.SQLData.HasPlayer(player.getName())) {
			PMM.SQLData.AddNewPlayer(player.getName(), player.getUniqueId().toString());
			player.sendMessage(ChatColor.AQUA + "亲爱的玩家，你好！因服务器的运行需要大量的时间和金钱进行维护，现改变制度。");
			player.sendMessage(ChatColor.AQUA + "为服务器长久发展，又不破坏游戏内公平，限定免费玩家每天有4小时游戏时间。");
			player.sendMessage(ChatColor.AQUA + "之前捐助过服务器的将获得专享套餐，长期在服务器游戏的玩家可以选择以下两种套餐：");
			player.sendMessage(ChatColor.GREEN + "套餐A：25元/月，一个月内游戏不限时，并给予当月" + ChatColor.YELLOW + "10次称号更改和2次皮肤修改。");
			player.sendMessage(ChatColor.GREEN + "套餐B：12元/月，一个月内周五六日不限时，并给予当月" + ChatColor.YELLOW + "3次称号更改。");
			PMM.getLogger().info(PlayerName + " 新添加入数据库");
		} else {
			//判断是否是今天第一次登陆
			FirstPlay = PMM.SQLData.isTodayFirstPlay(PlayerName);
			if (FirstPlay) {
				PMM.getLogger().info(PlayerName + " 今天第一次登陆，距上一次登陆已有 " + PMM.SQLData.CalculateDaysLast(PlayerName) + " 天。");
				PMM.SQLData.UpdateTodayFirstLogin(PlayerName);
				ContinuousDays = CalculateContinuousDays(player);
			} else {
				PMM.getLogger().info(PlayerName + "今日多次登陆，剩余可用在线时间 " + PMM.SQLData.GetTodayLimitMinute(PlayerName) + "分。");
			}
			PlayerCombo = PMM.SQLData.GetComboType(PlayerName);
			if (PlayerCombo.equals("Normal")) {
				NormalLogin(player);
			} else if (PlayerCombo.equals("A")) {
				ALogin(player);
			} else if (PlayerCombo.equals("B")) {
				BLogin(player);
			} else if (PlayerCombo.equals("C")) {
				CLogin(player);
			} else if (PlayerCombo.equals("Forever")) {
				ForeverLogin(player);
			}
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PMM, new ClientCheck(ContinuousDays, player, PlayerCombo, PMM), 1*20*60);
		}
	}

	//Normal套餐登陆处理
	public void NormalLogin(Player player){
		player.sendMessage(ChatColor.GREEN + "亲爱的免费玩家，你好！服务器的运行需要大量的时间和金钱进行维护。");
		player.sendMessage(ChatColor.GREEN + "为了服务器长久发展，又不破坏游戏内公平，限定免费玩家每天4小时的游戏时间");
		player.sendMessage(ChatColor.GREEN + "到下次6点或18点时长更新前还剩余" + PMM.SQLData.GetTodayLimitMinute(player.getName()) + "分钟游戏时间");
		player.sendMessage(ChatColor.GREEN + "可以通过支付宝，微信，电话卡支付不限时套餐，详询群326355263。使用/pm me查询时长。");
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
	}
	
	//A套餐登陆处理
	public void ALogin(Player player){
		boolean ComboSuit = false;
		String PlayerName = player.getName();
		long ExpireTime = PMM.SQLData.GetComboExpireTime(PlayerName);
		if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
			PMM.SQLData.SetComboType(PlayerName, "Normal");
			PMM.SQLData.SetTodayLimitMinute(PlayerName, 120);
			player.sendMessage(ChatColor.YELLOW + "亲爱的A套餐玩家，你好！感谢您对服务器的支持与付出！");
			player.sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有4小时免费游戏时间，祝玩的愉快！");
		} else {
			player.sendMessage(ChatColor.YELLOW + "亲爱的A套餐玩家，你好！感谢您对服务器的支持与付出！");
			player.sendMessage(ChatColor.YELLOW + "您的套餐到期日为:" + PMM.getDateString(ExpireTime) + "，祝玩的愉快！");
		}
		// 套餐A权限处理
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboA")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboA");
	}
	
	//B套餐登陆处理
	public void BLogin(Player player){
		boolean ComboSuit = false;
		String PlayerName = player.getName();
		long ExpireTime = PMM.SQLData.GetComboExpireTime(PlayerName);
		if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
			PMM.SQLData.SetComboType(PlayerName, "Normal");
			PMM.SQLData.SetTodayLimitMinute(PlayerName, 120);
			player.sendMessage(ChatColor.YELLOW + "亲爱的B套餐玩家，你好！感谢您对服务器的支持与付出！");
			player.sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有4小时免费游戏时间，祝玩的愉快！");
		} else {
			String Week = PMM.getWeekString(System.currentTimeMillis());
			if (Week.equals("星期日") || Week.equals("星期六") || Week.equals("星期五")) {
				player.sendMessage(ChatColor.YELLOW + "亲爱的B套餐玩家，你好！感谢您对服务器的支持与付出！");
				player.sendMessage(ChatColor.YELLOW + "您的套餐到期日为:" + PMM.getDateString(ExpireTime) + "，祝玩的愉快！");
			} else {
				player.sendMessage(ChatColor.YELLOW + "亲爱的B套餐玩家，你好！感谢您对服务器的支持与付出！");
				player.sendMessage(ChatColor.YELLOW + "今天是工作日，您只有免费游戏时间，剩余免费时间：" + PMM.SQLData.GetTodayLimitMinute(PlayerName) + "分钟，祝玩的愉快！");
			}
		}
		// 套餐B权限处理
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboB")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboB");
	}
	
	//C套餐登陆处理
	public void CLogin(Player player){
		boolean ComboSuit = false;
		String PlayerName = player.getName();
		long ExpireTime = PMM.SQLData.GetComboExpireTime(PlayerName);
		long CurrentTime = System.currentTimeMillis();
		long SpaceTime = CurrentTime - player.getLastPlayed();
		if (SpaceTime > 1000 * 60 * 60 * 24) {
			ExpireTime += SpaceTime - 1000 * 60 * 60 * 24;
			PMM.SQLData.SetComboExpireTime(player.getName(), ExpireTime);
			player.sendMessage("C套餐玩家" + player.getName() + "延时" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "天");
			PMM.getLogger().info("C套餐玩家" + player.getName() + "延时" + (SpaceTime / (1000 * 60 * 60 * 24) - 1) + "天");
		}
		if (ExpireTime - System.currentTimeMillis() < 1000 * 60 * 60 * 8) {
			PMM.SQLData.SetComboType(PlayerName, "Normal");
			PMM.SQLData.SetTodayLimitMinute(PlayerName, 120);
			player.sendMessage(ChatColor.YELLOW + "亲爱的C套餐玩家，你好！感谢您对服务器的支持与付出！");
			player.sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有4小时免费游戏时间");
		} else {
			player.sendMessage(ChatColor.YELLOW + "亲爱的C套餐玩家，你好！感谢您对服务器的支持与付出！");
			player.sendMessage(ChatColor.YELLOW + "您的套餐到期日为:" + PMM.getDateString(ExpireTime) + "，祝玩的愉快。");
		}
		// 套餐C权限处理
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboC")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboC");
	}
	
	//Forever套餐登陆处理
	public void ForeverLogin(Player player){
		boolean ComboSuit = false;
		//String PlayerName = player.getName();
		player.sendMessage(ChatColor.RED + "亲爱的永久玩家，你好！感谢您对服务器的支持与付出！");
		player.sendMessage(ChatColor.RED + "您可以享受永久无限时的玩乐，祝玩的愉快。");
		// 套餐Forever权限处理
		for (Group GroupofPlayer : Perm.getGroups(player.getUniqueId())) {
			if (GroupofPlayer.getName().contains("Combo")) {
				if (GroupofPlayer.getName().equals("ComboA")) {
					ComboSuit = true;
					continue;
				}
				Perm.RemoveGroup(player, GroupofPlayer.getName());
			}
		}
		if (!ComboSuit)
			Perm.AddGroup(player, "ComboA");
	}
	
	//计算奖励时间
	public int CalculateContinuousDays(Player player){
		String PlayerName = player.getName();
		int ContinuousDays = 0;
		if(PMM.SQLData.GetOnlineMinutes(PlayerName)>120){
			ContinuousDays = PMM.SQLData.GetContinuousDays(PlayerName);		//连续登陆天数
			int AwardMinute = PMM.SQLData.GetAwardMinute(PlayerName);			//奖励分钟数
			//持续登陆天数+1
			ContinuousDays = ContinuousDays + 1;
			PMM.SQLData.SetContinuousDays(PlayerName, ContinuousDays);
			if(ContinuousDays < 7){
				PMM.SQLData.SetAwardMinute(PlayerName, AwardMinute+5*ContinuousDays);;
			}else{
				PMM.SQLData.SetAwardMinute(PlayerName, AwardMinute+30);
			}
		}else {
			ContinuousDays = 0;
			PMM.SQLData.SetContinuousDays(PlayerName, 0);
		}
		PMM.SQLData.SetOnlineMinutes(PlayerName, 0);
		return ContinuousDays;
	}
	
	//玩家退出游戏事件
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String PlayerName = event.getPlayer().getName();
		if(PMM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			PMM.SQLData.SetClientStatu(PlayerName, ClientStatu.Offline);
			PMM.getLogger().info("使用专用客户端的玩家: " + PlayerName + "已经离线。");
		}
	}
}
