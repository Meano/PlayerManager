package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import org.bukkit.Sound;
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
			if (PlayerCombo.equals("Normal")) {
				player.sendMessage("§b§l>>可获得8小时每天的在线时间，如果您有能力，也希望您能通过");
				player.sendMessage("§c§l>>购买服务器套餐的方式支持我们，可不限时长，自选皮肤，自定称号等。");
				/*player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "每天使用专用客户端登陆，超过两小时，当天计入连续登陆天数。");
				player.sendMessage("§b§l第二天将获得§c§l<5*连续天数>§b§l分钟的奖励时间。");
				if(ContinuousDays == 0){
					player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "昨天没有超过两小时的专有客户端登陆记录，今天§c§l未获得§b§l在线奖励时间");
				}else if (ContinuousDays > 0){
					int Minute = (ContinuousDays>7)?30:(ContinuousDays*5);
					player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "您已连续登陆" + ContinuousDays +"天，获得" + Minute + "分钟奖励时间，尽快使用，下周一早六点将清零。");
				}*/
			} else if(PlayerCombo.equals("B")){
				player.sendMessage("§b§l>>可获得8小时每天的在线时间，感谢您对服务器的支持！");
			}
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0);
		} else {
			player.sendMessage("§b§l>>您未使用Meano服专用客户端登陆Meano服.");
			player.sendMessage("§b§l>>§c§l无法获得§b§l完整的服务器在线8小时在线时间，每天只有4小时在线时间。");
			//player.sendMessage("§b§l>>免费用户每天使用专用客户端登陆，超过两小时，当天计入连续登陆天数");
			//player.sendMessage("§b§l>>第二天将获得§c§l<5*连续天数>§b§l分钟每星期最多§c§l3.5小时§b§l累计奖励时间。");
			player.playSound(player.getLocation(), Sound.COW_HURT, 1, 0);
		}
		
	}
}
