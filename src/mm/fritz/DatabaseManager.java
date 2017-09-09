/*
Copyright 2017 Michael Mehl

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package mm.fritz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DatabaseManager {

	private static Logger LOG=LoggerFactory.getLogger(DatabaseManager.class);
	private MysqlDataSource dataSource;

	private final String USER;
	private final String PASS;
	private final String SERVER;
	private final String DB;

	public DatabaseManager(PropertyManager prop) {
		USER = prop.getProperty("mm.fritz.db.user", "user");
		PASS = prop.getProperty("mm.fritz.db.pass", "pass");
		SERVER = prop.getProperty("mm.fritz.db.server", "localhost");
		DB = prop.getProperty("mm.fritz.db.db", "db");

		dataSource = new MysqlDataSource();
		dataSource.setUser(USER);
		dataSource.setPassword(PASS);
		dataSource.setServerName(SERVER);
		dataSource.setDatabaseName(DB);
	}

	public Boolean tryStart(String user) {
		LOG.debug("tryStart");
		Connection con=null;
		try {
			con = dataSource.getConnection();
			LocalDate now = LocalDate.now();
			PreparedStatement statement = con.prepareStatement("SELECT timestamp FROM minecraft WHERE timestamp>=? AND flag='start' AND user=?");
			statement.setString(1,now.toString());
			statement.setString(2,user);
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				LOG.debug("tryStart ok");
				return true;
			}
			statement.close();
			try {
				statement = con.prepareStatement("INSERT INTO minecraft (timestamp, flag, user) VALUES (?,?,?)");
			} catch (SQLException e) {
				LOG.error("get connection failed",e);
				return false;
			}
			java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
			try {
				statement.setTimestamp(1, sqlTimestamp);
				statement.setString(2, "versuch");
				statement.setString(3, user);
				statement.executeUpdate();
				statement.close();
			} catch (SQLException e) {
				LOG.error("sql failed",e);
				return false;
			} finally {
				try {
					statement.close();
				} catch (Exception e) {
					LOG.debug("statement close failed",e);
				}
				try {
					con.close();
				} catch (Exception e) {
					LOG.debug("connection close failed",e);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (con != null) con.close();
			} catch (SQLException e) {
				// ignore
				e.printStackTrace();
			}
		}
		return false;
	}

	public void logStart(String user) {
		LOG.debug("logStart");
		Connection con=null;
		PreparedStatement statement=null;
		try {
			con = dataSource.getConnection();
			LocalDate now = LocalDate.now();
			statement = con.prepareStatement("INSERT INTO minecraft (timestamp, flag, user) VALUES (?,?,?)");
			java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
			try {
				statement.setTimestamp(1, sqlTimestamp);
				statement.setString(2, "start");
				statement.setString(3, user);
				statement.executeUpdate();
			} catch (SQLException e) {
				LOG.error("sql failed",e);
			} finally {
				try {
					statement.close();
				} catch (Exception e) {
					LOG.debug("statement close failed",e);
				}
				try {
					con.close();
				} catch (Exception e) {
					LOG.debug("connection close failed",e);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (con != null) con.close();
			} catch (SQLException e) {
				// ignore
				e.printStackTrace();
			}
		}
	}

	}
