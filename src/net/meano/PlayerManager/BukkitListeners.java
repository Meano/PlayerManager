package net.meano.PlayerManager;

import net.md_5.bungee.api.ChatColor;
import net.meano.PermissionsBukkit.PermissionsPlugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitListeners implements Listener {
	BukkitMain PM;
	PermissionsPlugin Perm;

	//初始化
	public BukkitListeners(BukkitMain GetPlugin) {
		PM = GetPlugin;
		Perm = (PermissionsPlugin) Bukkit.getPluginManager().getPlugin("PermissionsBukkit");
	}
	
	//玩家预登陆事件
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
		
		if(PlayerData.getString("PlayerUUID") == null) {
			PM.SQLData.UpdatePlayerUUID(PlayerName, PlayerUUID);
		}

		if(PM.PlayerMap.containsKey(PlayerUUID)) {
			PM.PlayerMap.remove(PlayerUUID);
		}
		
		PM.PlayerMap.put(PlayerUUID, new PlayerInfo(PlayerData, PM.Redis, PM.PlayerDataMap));

		PlayerInfo playerInfo = PM.PlayerMap.get(PlayerUUID);
		String Session = playerInfo.GetRedisKey("player.session");
		if(Session == null) {
			event.disallow(Result.KICK_OTHER, "请使用服务器专用客户端登录！");
			return;
		}
		for(int i = 0; i < 10; i++) {
			String Status = playerInfo.GetRedisKey("player.status");
			if(Status != null && Status.equals("join")) {
				Bukkit.getLogger().info("登录耗时:" + (1 + i) * i * 50 + "ms.");
				return;
			}
			Thread.sleep(100 + i * 100);
		}

		event.disallow(Result.KICK_OTHER, "登录超时，请稍后或重启客户端后重试。");
		PM.SQLData.Close();
		/*
		String PreLoginIP = event.getAddress().getHostName();

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
				if ((PMM.SQLData.GetTodayLimitMinute(PlayerName) <= 0)&&(PMM.SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Offline))) {
					event.disallow(Result.KICK_OTHER, ChatColor.GOLD + "亲爱的玩家，因为您未使用官方客户端，限定1小时在线时长已经用完，请使用官方客户端登陆服务器以进行不限时游戏。Q群：326355263");
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
		 */
	}
	
	//玩家退出游戏事件
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
		String PlayerUUID = event.getPlayer().getUniqueId().toString();
		PM.PlayerMap.remove(PlayerUUID);
	}
	
	// 玩家加入游戏事件
	public int Distance = 5;
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
		String PlayerUUID = event.getPlayer().getUniqueId().toString();
		PlayerInfo playerInfo = PM.PlayerMap.get(PlayerUUID);
		if(playerInfo == null) {
			return;
		}
		//int Merit = playerInfo.GetInt("Merit");
		int Gem = playerInfo.GetInteger("Gem");
		event.getPlayer().sendMessage(ChatColor.GOLD + "[积分] 当前可用积分" + Gem + "点。");
		playerInfo.SetRedisKey("player.status", "joined");
		event.getPlayer().setViewDistance(Distance);
		PM.getLogger().info("Player Distance:" + event.getPlayer().getViewDistance() + "ClientDis:" + event.getPlayer().getClientViewDistance());
		Distance = (Distance + 1) % 32;
//		String PlayerName = event.getPlayer().getName();
//		String PlayerCombo = null;
//		Player player = event.getPlayer();
//		boolean FirstPlay = false;
//		int ContinuousDays = -1;
		// 白名单
		/*for (int i = 0; i < 3; i++) {
			if (PMM.SetWhitelist[i].equalsIgnoreCase(PlayerName)) {
				PMM.SetWhitelist[i] = "Meano";
				player.setWhitelisted(true);
				PMM.getLogger().info("白名单验证开始处理： " + PlayerName + " 加入白名单！");
			}
		}*/
//		PMM.SQLData.Close();
//		PMM.SQLData.Open();
//		ResultSet PlayerInfo = PMM.SQLData.GetPlayer(player.getName());
		/*if(PlayerInfo.getString("PlayerUUID"))
		if (!PMM.SQLData.HasPlayer(player.getName())) {
			PMM.SQLData.AddNewPlayer(player.getName(), player.getUniqueId().toString());
			player.sendMessage(ChatColor.YELLOW + "使用官方客户端登陆服服务器可享受不限时游戏时间。");
			player.sendMessage(ChatColor.BLUE + "非官方客户端限定每天游戏1小时，加Q群326355263下载官方客户端。");
			player.sendMessage(ChatColor.GREEN + "服务器所有游戏内容都是免费的，捐助玩家可获得皮肤、称号、方块帽子。");
			PMM.getLogger().info(PlayerName + " 新添加入数据库");
			Location SpawnLocation = PMM.SQLData.GetSpawnPoint(event.getPlayer().getName());
			player.setBedSpawnLocation(SpawnLocation, true);
			player.teleport(SpawnLocation, TeleportCause.PLUGIN);
		} else {
			//判断是否是今天第一次登陆
			FirstPlay = PMM.SQLData.isTodayFirstPlay(PlayerName);
			if (FirstPlay) {
				PMM.getLogger().info(PlayerName + " 今天第一次登陆，距上一次登陆已有 " + PMM.SQLData.CalculateDaysLast(PlayerName) + " 天。");
				PMM.SQLData.UpdateTodayFirstLogin(PlayerName);
				ContinuousDays = 0;//
				CalculateContinuousDays(player);
			} else {
				PMM.getLogger().info(PlayerName + "今天多次登陆，剩余可用在线时间 " + PMM.SQLData.GetTodayLimitMinute(PlayerName) + "分。");
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
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PMM, new ClientCheck(ContinuousDays, player, PlayerCombo, PMM), 1*20*30);
		}*/
	}

	//Normal套餐登陆处理
	/*	public void NormalLogin(Player player){
		player.sendMessage(ChatColor.YELLOW + "使用官方客户端登陆服服务器可享受不限时游戏时间。");
		player.sendMessage(ChatColor.BLUE + "非官方客户端限定时间1小时,");
		player.spigot().sendMessage(DownloadClient);
		player.sendMessage(ChatColor.GREEN + "服务器所有游戏内容都是免费的，捐助玩家可获得皮肤、称号、方块帽子。");
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
			player.sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家，每日依旧有8小时免费游戏时间，祝玩的愉快！");
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
				player.sendMessage(ChatColor.YELLOW + "今天是工作日，您只有免费游戏时间，剩余免费时间： " + PMM.SQLData.GetTodayLimitMinute(PlayerName) + "分钟，祝玩的愉快！");
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
			player.sendMessage(ChatColor.YELLOW + "您的套餐已经到期，已为您转换为普通免费玩家");
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
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		if(!event.isBedSpawn()){
			Location SpawnLocation = PMM.SQLData.GetSpawnPoint(event.getPlayer().getName());
			event.setRespawnLocation(SpawnLocation);
			event.getPlayer().setBedSpawnLocation(SpawnLocation, true);
		}
	}*/
}
