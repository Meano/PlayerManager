package net.meano.DataBase;

import java.sql.Connection;

public interface SQLData {
	// 开启数据库
	public void Open();

	// 关闭数据库
	public void Close();

	// 获得数据库连接
	public Connection getConnection();
}
