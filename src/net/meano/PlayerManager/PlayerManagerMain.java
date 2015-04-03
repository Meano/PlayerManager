package net.meano.PlayerManager;

import java.io.File;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;
import net.meano.DataBase.SQLite;
import net.meano.PlayerServer.Server;
import net.meano.PlayerManager.MinuteTick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerManagerMain extends JavaPlugin {
	public static PlayerManagerMain PMM; 
	public Server PlayerSocket;
	public SQLite SQLData;
	public boolean isUpdate = false;
	public String[] SetWhitelist = new String[3];
	public void onEnable() {
		PMM = this;
		getLogger().info("PlayerManager 0.2,by Meano. 正在载入.");
		PluginManager PM = Bukkit.getServer().getPluginManager();
		PM.registerEvents(new PlayerManagerListeners(this), this);
		SQLData = new SQLite(new File(getDataFolder(), "PMData.db"), this);
		SocketInitialize();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new MinuteTick(this), 1 * 15 * 12, 1 * 60 * 20);
		SetWhitelist[0] = "Meano";
		SetWhitelist[1] = "Meano";
		SetWhitelist[2] = "Meano";
		/*
		 * for(OfflinePlayer p: Bukkit.getOfflinePlayers()){ String
		 * BufferFileName; BufferFileName =
		 * p.getUniqueId().toString()+"_["
		 * +p.getName()+"]["+getDateString
		 * (p.getFirstPlayed())+"]["+getDateString
		 * (p.getLastPlayed())+"].tmp"; File BufferFile = new
		 * File(this.getDataFolder(),BufferFileName);
		 * if(!BufferFile.exists()){ try { BufferFile.createNewFile(); }
		 * catch (IOException e) { e.printStackTrace(); } }
		 * getLogger().info(BufferFileName); }
		 */
		// getLogger().info(Bukkit.getOfflinePlayer("Meano").getUniqueId().toString());
	}

	public void onDisable() {
		SQLData.Close();
		getLogger().info("正在关闭端口25566。");
		PlayerSocket.CloseServer();
	}
	
	public String getDateString(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		@SuppressWarnings("resource")
		Formatter ft = new Formatter(Locale.CHINA);
		return ft.format("%1$tY年%1$tm月%1$td日", cal).toString().replaceAll(":", "_");
	}

	public int getTimeMinutes(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return cal.get(Calendar.MINUTE);
	}

	public int getTimeHours(long millis) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public String getWeekString(long dt) {
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dt);
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0)
			w = 0;
		return weekDays[w];
	}

	public void SocketInitialize() {
		PlayerSocket = new Server(this);
		PlayerSocket.start();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("playermanager")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("combo")) {
					if (!sender.isOp())
						return true;
					if (args.length == 4) {
						String ComboType = null;
						long ExpireTime = System.currentTimeMillis();
						if (!SQLData.HasPlayer(args[1])) {
							sender.sendMessage("没有这个玩家！");
							return true;
						}
						if (args[2].equalsIgnoreCase("a")) {
							ComboType = "A";
						} else if (args[2].equalsIgnoreCase("b")) {
							ComboType = "B";
						} else if (args[2].equalsIgnoreCase("c")) {
							ComboType = "C";
						} else if (args[2].equalsIgnoreCase("forever")) {
							ComboType = "Forever";
						} else if (args[2].equalsIgnoreCase("normal")) {
							ComboType = "Normal";
						} else {
							sender.sendMessage("没有此套餐");
							return true;
						}
						if (args[3].endsWith("d")) {
							ExpireTime += ((long) Integer.parseInt(args[3].replace("d", ""))) * 1000 * 60 * 60 * 24;
						} else if (args[3].endsWith("mon")) {
							ExpireTime += ((long) Integer.parseInt(args[3].replace("mon", ""))) * 1000 * 60 * 60 * 24 * 30;
						} else {
							sender.sendMessage("时间参数错误");
							return true;
						}
						SQLData.Close();
						SQLData.Open();
						SQLData.SetComboType(args[1], ComboType);
						SQLData.SetComboExpireTime(args[1], ExpireTime);
						sender.sendMessage("成功更新玩家" + args[1] + "的套餐为: " + ComboType + "套餐,套餐到期日" + getDateString(ExpireTime));
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm combo 玩家 套餐 时间");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("check")) {
					if (!sender.isOp())
						return true;
					if (args.length == 2) {
						if (!SQLData.HasPlayer(args[1])) {
							sender.sendMessage(ChatColor.BLUE + "没有这个玩家！");
							return true;
						}
						String ComboType = null;
						int TodayLimit = 0;
						int AwardMinute = 0;
						long ExpireTime = 0;
						int ContinuousDays = 0;
						int OnlineMinute = 0;
						SQLData.Close();
						SQLData.Open();
						ComboType = SQLData.GetComboType(args[1]);
						TodayLimit = SQLData.GetTodayLimitMinute(args[1]);
						ExpireTime = SQLData.GetComboExpireTime(args[1]);
						AwardMinute = SQLData.GetAwardMinute(args[1]);
						ContinuousDays = SQLData.GetContinuousDays(args[1]);
						OnlineMinute = SQLData.GetOnlineMinutes(args[1]);
						sender.sendMessage(ChatColor.BLUE + "玩家" + args[1] + "游戏时间情况查询:");
						sender.sendMessage(ChatColor.YELLOW + "时长套餐类型:" + ComboType + "套餐");
						if (ComboType.equals("Normal")) {
							sender.sendMessage(ChatColor.YELLOW + "玩家今日剩余时长: " + TodayLimit + " 分钟");
							sender.sendMessage(ChatColor.YELLOW + "玩家累积任务奖励时长: " + AwardMinute + " 分钟");
						} else if (ComboType.equals("Forever")) {
							sender.sendMessage(ChatColor.YELLOW + "套餐永不过期");
						} else if (ComboType.equals("B")) {
							sender.sendMessage(ChatColor.YELLOW + "玩家今日剩余时长: " + TodayLimit + " 分钟");
							sender.sendMessage(ChatColor.YELLOW + "玩家累积任务奖励时长: " + AwardMinute + " 分钟");
							sender.sendMessage(ChatColor.YELLOW + "套餐到期日: " + getDateString(ExpireTime));
						} else {
							sender.sendMessage(ChatColor.YELLOW + "套餐到期日: " + getDateString(ExpireTime));
						}
						if(sender.hasPermission("PlayerManager.Award"))
							sender.sendMessage(ChatColor.YELLOW + "玩家可用奖励其他玩家分钟数: " + AwardMinute + " 分钟");
						sender.sendMessage(ChatColor.YELLOW + "玩家今日使用专用客户端在线时长: " + OnlineMinute + " 分钟");
						sender.sendMessage(ChatColor.YELLOW + "玩家连续使用专用客户端在线天数: " + ContinuousDays + " 天");
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm check 玩家");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("me")) {
					if (sender instanceof Player) {
						String ComboType;
						int TodayLimit;
						int AwardMinute;
						int ContinuousDays;
						int OnlineMinute;
						long ExpireTime;
						SQLData.Close();
						SQLData.Open();
						ComboType = SQLData.GetComboType(sender.getName());
						TodayLimit = SQLData.GetTodayLimitMinute(sender.getName());
						ExpireTime = SQLData.GetComboExpireTime(sender.getName());
						AwardMinute = SQLData.GetAwardMinute(sender.getName());
						ContinuousDays = SQLData.GetContinuousDays(sender.getName());
						OnlineMinute = SQLData.GetOnlineMinutes(sender.getName());
						sender.sendMessage(ChatColor.BLUE + "玩家" + sender.getName() + "游戏时间情况查询:");
						sender.sendMessage(ChatColor.YELLOW + "时长套餐类型:" + ComboType + "套餐");
						if (ComboType.equals("Normal")) {
							sender.sendMessage(ChatColor.YELLOW + "玩家今日剩余免费时长:" + TodayLimit + "分钟");
							sender.sendMessage(ChatColor.YELLOW + "玩家累积任务奖励时长:" + AwardMinute + "分钟");
							sender.sendMessage(ChatColor.YELLOW + "玩家连续使用专用客户端登陆天数:" + ContinuousDays + "天");
							sender.sendMessage(ChatColor.YELLOW + "玩家今日使用专用客户端分钟数:" + OnlineMinute + "分钟,超过120分将计入连续登陆天数。");
						} else if (ComboType.equals("B")) {
							sender.sendMessage(ChatColor.YELLOW + "玩家今日剩余剩余时长:" + TodayLimit + "分钟");
							sender.sendMessage(ChatColor.YELLOW + "玩家累积任务奖励时长:" + AwardMinute + "分钟");
							sender.sendMessage(ChatColor.YELLOW + "玩家连续使用专用客户端登陆天数:" + ContinuousDays + "天");
							sender.sendMessage(ChatColor.YELLOW + "玩家今日使用专用客户端分钟数:" + OnlineMinute + "分钟,超过120分将计入连续登陆天数。");
							sender.sendMessage(ChatColor.YELLOW + "套餐到期日: " + getDateString(ExpireTime) + " 在此之前的周五六日您的游戏时间不受不限制。");
						} else if (ComboType.equals("C") || ComboType.equals("A")) {
							sender.sendMessage(ChatColor.YELLOW + "套餐到期日: " + getDateString(ExpireTime) + " 在此之前您的游戏时间都不受不限制。");
						} else {
							sender.sendMessage(ChatColor.RED + "尊贵的永久玩家，您的在线时长不受限制！");
						}
						return true;
					} else {
						sender.sendMessage("这条命令只能由玩家执行");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("today")) {
					if (!sender.isOp())
						return true;
					if (args.length == 3) {
						SQLData.Close();
						SQLData.Open();
						if (SQLData.HasPlayer(args[1])) {
							SQLData.SetTodayLimitMinute(args[1], Integer.parseInt(args[2]));
							sender.sendMessage(ChatColor.BLUE + "成功设定玩家 " + args[1] + " 的分钟数为 " + args[2]);
						} else {
							sender.sendMessage(ChatColor.BLUE + "没有这个玩家");
						}
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm today 玩家 分钟数");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("award")) {
					if (!sender.isOp() && !sender.hasPermission("PlayerManager.Award"))
						return true;
					if (args.length == 3) {
						if (SQLData.HasPlayer(args[1])) {
							if (!sender.isOp()) {
								int HasAwardMinute = SQLData.GetAwardMinute(sender.getName());
								if (HasAwardMinute < Integer.parseInt(args[2])) {
									sender.sendMessage(ChatColor.BLUE + "你的可用奖励分钟数为" + HasAwardMinute + "不足以完成这次玩家分钟数奖励");
									return true;
								} else {
									HasAwardMinute = HasAwardMinute - Integer.parseInt(args[2]);
									SQLData.SetAwardMinute(sender.getName(), HasAwardMinute);
									sender.sendMessage(ChatColor.BLUE + "可用奖励在线时间剩余" + HasAwardMinute + "分钟。");
								}
							}
							int AwardMinute = SQLData.GetAwardMinute(args[1]);
							sender.sendMessage(ChatColor.BLUE + "玩家 " + args[1] + " 的服务器任务奖励分钟数为 " + AwardMinute);
							AwardMinute += Integer.parseInt(args[2]);
							SQLData.SetAwardMinute(args[1], AwardMinute);
							sender.sendMessage(ChatColor.BLUE + "成功增加玩家 " + args[1] + " 的分钟数 " + args[2] + " 分钟，当前玩家拥有" + AwardMinute + "任务奖励分钟。");
						} else {
							sender.sendMessage(ChatColor.BLUE + "没有这个玩家");
						}
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm award 玩家 分钟数");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("relimit")) {
					if (!sender.isOp())
						return true;
					if (args.length == 2) {
						SQLData.UpdateLimitTime(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.BLUE + "成功刷新所有玩家的分钟数");
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm relimit 分钟数");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("reaward")) {
					if (!sender.isOp())
						return true;
					if (args.length == 2) {
						SQLData.UpdateAwardTime(Integer.parseInt(args[1]));
						sender.sendMessage(ChatColor.BLUE + "成功刷新所有套餐玩家的奖励分钟数");
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm reaward 分钟数");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("whitelist")) {
					if (!sender.hasPermission("PlayerManager.Whitelist")) {
						sender.sendMessage(ChatColor.RED + "你没有权限添加！");
						return true;
					}
					if (args.length == 2) {
						int i = 0;
						for (i = 0; i < 3; i++) {
							if (SetWhitelist[i].equals("Meano")) {
								SetWhitelist[i] = args[1];
								sender.sendMessage(ChatColor.BLUE + args[1] + "成功添加到，白名单列表。");
								return true;
							}
						}
						sender.sendMessage(ChatColor.BLUE + "白名单列表已满，清除了" + SetWhitelist[0] + SetWhitelist[1] + SetWhitelist[2] + "的预订白名单，若其中玩家还没进来，请重新添加。");
						SetWhitelist[0] = args[1];
						SetWhitelist[1] = "Meano";
						SetWhitelist[2] = "Meano";
						sender.sendMessage(ChatColor.BLUE + "成功添加白名单预订列表。");
						return true;
					} else {
						sender.sendMessage("参数不正确：/pm whitelist 玩家ID");
						return true;
					}
				}else if(args[0].equalsIgnoreCase("info")){
					if(sender instanceof Player){
						Player Pinfo = (Player) sender;
						sender.sendMessage(ChatColor.BLUE+"亲爱的玩家 ["+Pinfo.getName()+"] 你在"+getDateString(Pinfo.getFirstPlayed())+"来到了这个服务器。");
						return true;
					}else{
						sender.sendMessage("只能由玩家使用这条指令！");
					}
					
				}else {
					sender.sendMessage("/pm me 查询自己的时长或套餐剩余情况");
					return true;
				}
			} else {
				sender.sendMessage("/pm me 查询自己的时长或套餐剩余情况");
				return true;
			}
		}
		return false;
	}
}
