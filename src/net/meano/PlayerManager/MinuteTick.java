package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import net.meano.DataBase.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.meano.PlayerManager.PlayerManagerMain;

public class MinuteTick implements Runnable {
	public PlayerManagerMain PMM;
	public SQLite SQLData;
	public MinuteTick(PlayerManagerMain P){
		PMM = P;
		SQLData = P.SQLData;
	}
	@Override
	public void run() {
		SQLData.Close();
		SQLData.Open();
		UpdateTime();
		for (Player player : Bukkit.getOnlinePlayers()) {
			String ComboType = SQLData.GetComboType(player.getName());
			if (ComboType.equals("Normal")) {
				MinuteNormal(player);
			} else if (ComboType.equals("B")) {
				MinuteB(player);
			}
		}
	}
	
	//更新免费玩家时间
	public void UpdateTime(){
		long LongTime = System.currentTimeMillis();
		if ((PMM.getTimeHours(LongTime) == 6) || (PMM.getTimeHours(LongTime) == 18)) {
			if (PMM.getTimeMinutes(LongTime) < 3 && (!PMM.isUpdate)) {
				SQLData.UpdateLimitTime(120);
				SQLData.UpdateAwardTime(120);
				Bukkit.broadcastMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "各位免费玩家，服务器已经更新了大家的免费在线时长，每天6点和18点更新，在线时长使用/pm me 查看。");
				PMM.isUpdate = true;
			} else if (PMM.getTimeMinutes(LongTime) > 3) {
				PMM.isUpdate = false;
			}
		} else {
		}
	}
	
	//Normal套餐每分钟进行的处理
	public void MinuteNormal(Player player){
		int LimitTime = SQLData.GetTodayLimitMinute(player.getName());
		if (LimitTime > 0) {
			if (LimitTime == 1) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l您的免费时长已经用完，如果您有做服务器任务获得的奖励时间，接下来将消耗奖励时间，否则，您将被踢出游戏").toString());
			}
			SQLData.SetTodayLimitMinute(player.getName(), LimitTime - 1);
		} else {
			int AwardMinute = SQLData.GetAwardMinute(player.getName());
			if (AwardMinute > 0) {
				SQLData.SetAwardMinute(player.getName(), AwardMinute - 1);
			} else {
				player.kickPlayer(ChatColor.GOLD + "亲爱的免费玩家，今天您的免费时长已经用完，服务器任务奖励时长也已消耗完，您可选择购买服务器无限时套餐重新登陆游戏，或者多做奖励任务来换取时长。");
			}
		}
		if(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online)){
			int OnlineMinutes = SQLData.GetOnlineMinutes(player.getName());
			if (OnlineMinutes >= 0){
				SQLData.SetOnlineMinutes(player.getName(), OnlineMinutes + 1);
			}
		}
	}
	
	//B套餐每分钟进行的处理
	public void MinuteB(Player player){
		String Week = PMM.getWeekString(System.currentTimeMillis());
		if (Week.equals("星期日") || Week.equals("星期六") || Week.equals("星期五")) {
		} else {
			int LimitTime = SQLData.GetTodayLimitMinute(player.getName());
			if (LimitTime > 0) {
				if (LimitTime == 1) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l您的免费时长已经用完，如果您有做服务器任务获得的奖励时间，接下来将消耗奖励时间，否则，您将被踢出游戏").toString());
				}
				SQLData.SetTodayLimitMinute(player.getName(), LimitTime - 1);
			} else {
				int AwardMinute = SQLData.GetAwardMinute(player.getName());
				if (AwardMinute > 0) {
					SQLData.SetAwardMinute(player.getName(), AwardMinute - 1);
				} else {
					player.kickPlayer(ChatColor.GOLD + "亲爱的B套餐玩家，今天是工作日，您今天的免费时长已经用完，服务器任务奖励时长也已消耗完，欢迎您明天再来游戏。");
				}
			}
		}
		if(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online)){
			int OnlineMinutes = SQLData.GetOnlineMinutes(player.getName());
			if (OnlineMinutes >= 0){
				SQLData.SetOnlineMinutes(player.getName(), OnlineMinutes + 1);
			}
		}
	}
}
