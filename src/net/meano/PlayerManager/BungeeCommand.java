package net.meano.PlayerManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BungeeCommand extends Command {

	public BungeeMain PM;
	public BungeeCommand(BungeeMain pm) {
		super("pm");
		PM = pm;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("PlayerManager")) return;
		if (args.length >= 3) {
			if(args[0].equalsIgnoreCase("charge")) {
				String playerName = args[1];
				TextComponent resultText = new TextComponent();
				PM.SQLData.Open();
				ResultSet playerData = PM.SQLData.GetPlayerInfo(playerName);
				if(playerData == null) {
					resultText.setText(playerName + "不存在！");
					resultText.setColor(ChatColor.RED);
					sender.sendMessage(resultText);
					return;
				}
				try {
					String playerUUID = playerData.getString("PlayerUUID");
					playerName = playerData.getString("PlayerName");
					int chargeGem = Integer.parseInt(args[2]);
					if(chargeGem <= 0) {
						resultText.setText("积分需要大于0！");
						resultText.setColor(ChatColor.RED);
						sender.sendMessage(resultText);
						return;
					}
					String message = args.length == 4 ? args[3] : "增加积分";
					PM.SQLData.ChargeGem(playerUUID, chargeGem, message);
					resultText.setText(playerName + "增加积分: " + chargeGem + "点，附加信息: " + message);
					resultText.setColor(ChatColor.AQUA);
					sender.sendMessage(resultText);
				} catch (SQLException e) {
					resultText.setText(e.getLocalizedMessage());
					resultText.setColor(ChatColor.RED);
					sender.sendMessage(resultText);
				}
				
			}
		}
	}
}
