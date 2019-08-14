package net.meano.PlayerServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import net.meano.PlayerManager.BukkitMain;

public class Server extends Thread {
	ServerSocket PMServer = null;
	Socket SocketAccept = null;
	InputStreamReader SocketReader = null;
	BukkitMain PMM;
	String ReceiveString;
	String[] ReceiveClientInfo;
	int ReceiveLength = 0;
	char ReceiveChars[] = new char[30];

	public Server(BukkitMain GetPlugin) {
		PMM = GetPlugin;
		try {
			PMServer = new ServerSocket(PMM.getConfig().getInt("Server.Port"));
		} catch (IOException e) {
			PMM.getLogger().info(e.getCause().getMessage());
		}
	}

	public void CloseServer() {
		try {
			PMServer.close();
		} catch (IOException e) {
			PMM.getLogger().info(e.getCause().getMessage());
		}
	}

	public void run() {
		PMM.getLogger().info("开始监听" + PMServer.getLocalPort() + "端口");
		while (!PMServer.isClosed()) {
			try {
				/*SocketAccept = PMServer.accept();
				SocketAccept.setSoTimeout(2000);
				SocketReader = new InputStreamReader(SocketAccept.getInputStream(),"UTF8");
				ReceiveLength = SocketReader.read(ReceiveChars);
				ReceiveString = new String(ReceiveChars, 0, ReceiveLength);
				SocketAccept.close();
				SocketReader.close();
				ReceiveClientInfo = ReceiveString.split("\\$", 5);
				if (ReceiveClientInfo.length == 5) {
					PMM.getLogger().info("玩家: " + ReceiveClientInfo[1] + " 版本: " + ReceiveClientInfo[2] + " 类型: " + ReceiveClientInfo[3]);
					if(ReceiveClientInfo[3].equals("Connect")){
						PMM.SQLData.SetClientStatu(ReceiveClientInfo[1], ClientStatu.Online);
						PMM.getLogger().info("玩家: " + ReceiveClientInfo[1] + " 使用专有客户端登陆游戏！");
					}else if(ReceiveClientInfo[3].equals("Join")){
						PMM.SQLData.SetClientStatu(ReceiveClientInfo[1], ClientStatu.Join);
						PMM.getLogger().info("玩家: " + ReceiveClientInfo[1] + " 使用专有客户端打开游戏！");
					}
				}
				ReceiveString = null;*/
			} catch (Exception e) {
				try {
					SocketAccept.close();
					SocketReader.close();
				} catch (IOException ex) {
				}
				//PMM.getLogger().info(e.getLocalizedMessage());
			}
		}
	}
}
