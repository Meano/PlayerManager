package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ClientCheck implements Runnable {
	public int ContinuousDays;
	public Player player;
	public PlayerManagerMain PMM;
	public String PlayerCombo;
	public ClientCheck(int CDays, Player p, String Combo, PlayerManagerMain P){
		ContinuousDays = CDays;
		player = p;
		PlayerCombo = Combo;
		PMM = P;
	}
	@Override
	public void run(){
		if(!player.isOnline()) return;
		String PlayerName = player.getName();
		if(PMM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			player.sendMessage("§b§l>>感谢您使用专用客户端登陆Meano服");
			if (PlayerCombo.equals("Normal") || PlayerCombo.equals("B")) {
				player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "每天使用专用客户端登陆，超过两小时，当天计入连续登陆天数。");
				player.sendMessage("§b§l第二天将获得§c§l<5*连续天数>§b§l分钟的奖励时间。");
				if(ContinuousDays == 0){
					player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "昨天没有超过两小时的专有客户端登陆记录，今天§c§l未获得§b§l在线奖励时间");
				}else if (ContinuousDays > 0){
					int Minute = (ContinuousDays>7)?30:(ContinuousDays*5);
					player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "您已连续登陆" + ContinuousDays +"天，获得" + Minute + "分钟奖励时间，尽快使用，下周一早六点将清零。");
				}
			}
		} else {
			player.sendMessage("§b§l>>您未使用Meano服专用客户端登陆Meano服.");
			player.sendMessage("§c§l>>无法获得§b§l服务器在线时间奖励等权利");
			player.sendMessage("§b§l>>免费用户每天使用专用客户端登陆，超过两小时，当天计入连续登陆天数");
			player.sendMessage("§b§l>>第二天将获得§c§l<5*连续天数>§b§l分钟的奖励时间。");
		}
	}
}
