package net.meano.PlayerManager;

import net.meano.DataBase.ClientStatu;
import org.bukkit.entity.Player;

public class ClientCheck implements Runnable {
	public int ContinuousDays;
	public Player player;
	public PlayerManagerMain PMM;
	public ClientCheck(int CDays, Player p, PlayerManagerMain P){
		ContinuousDays = CDays;
		player = p;
		PMM = P;
	}
	@Override
	public void run(){
		if(!player.isOnline()) return;
		String PlayerName = player.getName();
		if(PMM.SQLData.GetClientStatu(PlayerName).equals(ClientStatu.Online)){
			
		} else {
			
		}
	}
}
