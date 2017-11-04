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

	private final String USER;
	private final String PASS;
	private final String SERVER;
	private final String DB;

	private MysqlDataSource dataSource;

	public DatabaseManager(PropertyManager prop) {
		USER = prop.getProperty("mm.fritz.db.user", "user");
		LOG.debug("user {}",USER);
		PASS = prop.getProperty("mm.fritz.db.pass", "pass");
		LOG.debug("pass {}",PASS);
		SERVER = prop.getProperty("mm.fritz.db.server", "localhost");
		LOG.debug("server {}",SERVER);
		DB = prop.getProperty("mm.fritz.db.db", "db");
		LOG.debug("db {}",DB);
		initDatasource();
	}

	private void initDatasource() {
		dataSource = new MysqlDataSource();
		dataSource.setUser(USER);
		dataSource.setPassword(PASS);
		dataSource.setServerName(SERVER);
		dataSource.setDatabaseName(DB);

		dataSource.setAutoReconnect(true);
	}

	public Boolean tryStart(String user) {
		LOG.debug("tryStart {}",user);
		Connection con=null;
		try {
			con = dataSource.getConnection();
			LocalDate now = LocalDate.now();
			PreparedStatement statement = con.prepareStatement("SELECT timestamp FROM minecraft WHERE timestamp>=? AND flag='start' AND user=?");
			statement.setString(1,now.toString());
			statement.setString(2,user);
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				LOG.debug("tryStart {}: ok", user);
				return true;
			}
			LOG.debug("tryStart {}: not ok", user);
			statement.close();
			statement = con.prepareStatement("INSERT INTO minecraft (timestamp, flag, user) VALUES (?,?,?)");
			java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
			try {
				statement.setTimestamp(1, sqlTimestamp);
				statement.setString(2, "versuch");
				statement.setString(3, user);
				statement.executeUpdate();
				statement.close();
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
			LOG.debug("sql exception",e);
			throw new Error(e);
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
				// ignore
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
			LOG.error("sql failed",e);
			// ignore
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
