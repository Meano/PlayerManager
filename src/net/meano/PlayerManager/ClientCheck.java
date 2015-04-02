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
			player.sendMessage(ChatColor.BLUE + "感谢您使用专用客户端登陆Meano服");
			if (PlayerCombo.equals("Normal") || PlayerCombo.equals("B")) {
				player.sendMessage(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "每天使用专用客户端登陆，超过两小时，当天计入连续登陆天数，第二天将获得（5*连续天数）奖励分钟数");
				if(ContinuousDays == 0){
					player.sendMessage(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "昨天没有超过两小时的专有客户端登陆记录，今天未获得在线奖励时间");
				}else if (ContinuousDays > 0){
					int Minute = (ContinuousDays>7)?30:(ContinuousDays*5);
					player.sendMessage(ChatColor.DARK_PURPLE + ChatColor.BOLD.toString() + "您已连续登陆" + ContinuousDays +"天，获得" + Minute + "分钟奖励时间，尽快使用，下周一早六点将清零。");
				}
			}
		} else {
			player.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "您未使用Meano服专用客户端登陆Meano服，无法获得服务器在线时间奖励等权利。");
			player.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "普通用户每天使用专用客户端登陆，超过两小时，当天计入连续登陆天数，第二天将获得（5*连续天数）奖励分钟数");
		}
	}
}
