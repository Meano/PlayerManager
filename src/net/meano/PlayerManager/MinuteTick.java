﻿package net.meano.PlayerManager;

//import net.meano.DataBase.ClientStatu;
import net.meano.DataBase.DBManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import net.meano.PlayerManager.BukkitMain;

public class MinuteTick implements Runnable {
	public BukkitMain PMM;
	public DBManager SQLData;
	public MinuteTick(BukkitMain P){
		PMM = P;
		SQLData = P.SQLData;
	}
	@Override
	public void run() {
		SQLData.Close();
		SQLData.Open();
		/*UpdateTime();
		for (Player player : Bukkit.getOnlinePlayers()) {
			String ComboType = SQLData.GetComboType(player.getName());
			if (ComboType.equals("Normal")) {
				MinuteNormal(player);
			} else if (ComboType.equals("B")) {
				MinuteB(player);
			}
		}*/
	}
	
	//更新免费玩家时间
	public void UpdateTime(){
		long LongTime = System.currentTimeMillis();
		if (PMM.getTimeHours(LongTime) == 18) {
			if (PMM.getTimeMinutes(LongTime) < 3 && (!PMM.isUpdate)) {
				//SQLData.UpdateLimitTime(60);
				//SQLData.UpdateAwardTime(240);
				Bukkit.broadcastMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "各位未使用官方客户端的玩家，服务器更新了在线时长，每天18点更新，限制在线时长使用/pm me 查看。");
				PMM.isUpdate = true;
			} else if (PMM.getTimeMinutes(LongTime) > 3) {
				PMM.isUpdate = false;
			}
		}
	}
	
	//Normal套餐每分钟进行的处理
	public void MinuteNormal(Player player){
		/*if(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Offline)){
			int LimitTime = SQLData.GetTodayLimitMinute(player.getName());
			if (LimitTime > 0) {
				if (LimitTime == 1) {
					player.kickPlayer(ChatColor.GOLD + "亲爱的玩家，因为您未使用官方客户端，限定1小时在线时长已经用完，请使用官方客户端登陆服务器以进行不限时游戏。Q群：326355263");
				}
				SQLData.SetTodayLimitMinute(player.getName(), LimitTime - 1);
			}else {
				int AwardMinute = SQLData.GetAwardMinute(player.getName());
				if (AwardMinute > 0) {
					SQLData.SetAwardMinute(player.getName(), AwardMinute - 1);
				} else {
					player.kickPlayer(ChatColor.GOLD + "亲爱的玩家，因为您未使用官方客户端，限定1小时在线时长已经用完，请使用官方客户端登陆服务器以进行不限时游戏。Q群：326355263");
				}
			}
		}else if(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online)){
			int OnlineMinutes = SQLData.GetOnlineMinutes(player.getName());
			if (OnlineMinutes >= 0){
				SQLData.SetOnlineMinutes(player.getName(), OnlineMinutes + 1);
			}
		}*/
	}
	
	//B套餐每分钟进行的处理
	public void MinuteB(Player player){
		/*String Week = PMM.getWeekString(System.currentTimeMillis());
		if (Week.equals("星期日") || Week.equals("星期六") || Week.equals("星期五")) {
		} else {
			int LimitTime = SQLData.GetTodayLimitMinute(player.getName());
			if (LimitTime > 0) {
				if (LimitTime == 1) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&l您的免费时长已经用完，如果您有做服务器任务获得的奖励时间，接下来将消耗奖励时间，否则，您将被踢出游戏").toString());
				}else if(LimitTime < 120) {
					if(!(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Join)||SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online))){
						player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&c&l抱歉，只有使用服务器专有客户端登陆才能完全享受每天免费的8小时游戏时间，否则您只有4小时限制游戏时间！\n\r&a&l有条件的话也希望您能通过购买服务器套餐支持我们，享受自由选择皮肤，头戴各种方块，自定称号，不限时等权利。"));
					}
				}else if(LimitTime == 120){
					if(!(SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Join)||SQLData.GetClientStatu(player.getName()).equals(ClientStatu.Online))){
						player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&c&l抱歉，只有使用服务器专有客户端登陆才能完全享受每天免费的8小时游戏时间，否则您只有4小时限制游戏时间！\n\r&a&l有条件的话也希望您能通过购买服务器套餐支持我们，享受自由选择皮肤，头戴各种方块，自定称号，不限时等权利。"));
					}else{
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l感谢你使用专有客户端登陆服务器进行游戏，你可以享受完整的每天8小时游戏免费游戏在线时间。"));
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&l有条件的话也希望您能通过购买服务器套餐支持我们，享受自由选择皮肤，头戴各种方块，自定称号，不限时等权利。"));
					}
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
		}*/
	}
}
