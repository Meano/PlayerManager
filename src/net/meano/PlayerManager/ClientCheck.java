package net.meano.PlayerManager;

import org.bukkit.entity.Player;

public class ClientCheck implements Runnable {
	public int ContinuousDays;
	public Player player;
	public BukkitMain PMM;
	public String PlayerCombo;

	public ClientCheck(int CDays, Player p, String Combo, BukkitMain P){
		ContinuousDays = CDays;
		player = p;
		PlayerCombo = Combo;
		PMM = P;
	}
	@Override
	public void run(){
		/*if(!player.isOnline()) return;
		String PlayerName = player.getName();
		if(PMM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			player.sendMessage("§b§l>>感谢您使用专用客户端登陆Meano服");
			if (PlayerCombo.equals("Normal")) {
				player.sendMessage("§b§l>>可以不限时进行游戏，如果您有能力，也希望您能通过");
				player.sendMessage("§c§l>>购买服务器套餐的方式支持我们，可拥有自选皮肤，自定称号，各种方块帽子随意戴等权利。");
			} else if(PlayerCombo.equals("B")){
				player.sendMessage("§b§l>>可获得8小时每天的在线时间，感谢您对服务器的支持！");
			}
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
		} else {
			player.sendMessage("§b§l>>您未使用Meano服专用客户端登陆服务器.");
			player.sendMessage("§b§l>>§c§l无法获得§b§l不限时游戏资格，每天只有1小时在线时间。");
			player.playSound(player.getLocation(), Sound.ENTITY_COW_HURT, 1, 0);
		}*/
		
	}
}
