package net.meano.PlayerManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.meano.DataBase.ClientStatu;
import org.bukkit.Bukkit;
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
	}
	
	//玩家登陆游戏事件
	@EventHandler(priority = EventPriority.LOWEST)
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
			player.sendMessage(ChatColor.YELLOW + "使用官方客户端登陆服服务器可享受不限时游戏时间。");
			player.sendMessage(ChatColor.BLUE + "非官方客户端限定每天游戏1小时，加Q群326355263下载官方客户端。");
			player.sendMessage(ChatColor.GREEN + "服务器所有游戏内容都是免费的，捐助玩家可获得皮肤、称号、方块帽子。");
			PMM.getLogger().info(PlayerName + " 新添加入数据库");
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
		}
	}

	//Normal套餐登陆处理
	public void NormalLogin(Player player){
		TextComponent DownloadClient = new TextComponent("单击此处下载官方客户端,");
		DownloadClient.setClickEvent(new ClickEvent(Action.OPEN_URL,"http://shang.qq.com/wpa/qunwpa?idkey=1ee02d962c1e049aad634dd2c65c3d65d0005ccd3bfec21a833aa4191495bd1e"));
		DownloadClient.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").color(ChatColor.AQUA).append("单击此链接，下载官方客户端，官方客户端不限在线时间。").create()));
		DownloadClient.setBold(true);
		DownloadClient.setColor(ChatColor.GREEN);
		TextComponent AddGroup = new TextComponent("单击此处加入官方Q群。");
		AddGroup.setClickEvent(new ClickEvent(Action.OPEN_URL,"http://shang.qq.com/wpa/qunwpa?idkey=1ee02d962c1e049aad634dd2c65c3d65d0005ccd3bfec21a833aa4191495bd1e"));
		AddGroup.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("").color(ChatColor.AQUA).append("单击此链接，加入Meano服官方Q群，QQ群号326355263。").create()));
		AddGroup.setBold(true);
		AddGroup.setColor(ChatColor.GREEN);
		DownloadClient.addExtra(AddGroup);
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
