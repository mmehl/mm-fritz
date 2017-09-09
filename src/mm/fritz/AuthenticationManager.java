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

import java.io.IOException;

import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationManager {
	private static Logger LOG=LoggerFactory.getLogger(AuthenticationManager.class);

	public enum Authentication {
		LOGIN_ERROR,
		NO_MINECRAFT_USER,
		MINECRAFT_USER,
		MINECRAFT_ADMIN,
		ERROR
	}

	private final String LDAP_Server;
	private final int LDAP_Port;
	private final String LDAP_User;
	private final String LDAP_UserGroup;
	private final String LDAP_AdminGroup;
	
	public AuthenticationManager(PropertyManager prop) {
		LDAP_Server = prop.getProperty("mm.fritz.ldap.server", "localhost");
		LDAP_Port = Integer.parseInt(prop.getProperty("mm.fritz.ldap.port", "389")); // TODO: error handling
		LDAP_User = prop.getProperty("mm.fritz.ldap.userDn", "ou=people,dc=localhost");
		LDAP_UserGroup = prop.getProperty("mm.fritz.ldap.userGroupDn", "cn=MinecraftAdmin,ou=group,dc=localhost");
		LDAP_AdminGroup = prop.getProperty("mm.fritz.ldap.adminGroupDn", "cn=MinecraftAdmin,ou=group,dc=localhost");
	}

	Authentication tryLogin(String uid, String pass) {
		LOG.debug("tryLogin {}",uid);
		LdapConnection connection = new LdapNetworkConnection( LDAP_Server, LDAP_Port );
		
			try {
				connection.bind("uid="+uid+","+LDAP_User,pass);
			} catch (LdapException e1) {
				LOG.error("bind failed",e1);
				try {
					connection.close();
				} catch (IOException e) {
					LOG.warn("close failed",e);
				}
				return Authentication.LOGIN_ERROR;
			}
			try {
			// "(objectclass=*)" 
		    EntryCursor cursor = connection.search( LDAP_UserGroup, "(memberUid="+uid+")", SearchScope.OBJECT );
		    Authentication found=Authentication.NO_MINECRAFT_USER;
		    for (Entry entry : cursor) {
		    	found=Authentication.MINECRAFT_USER;
		        System.out.println( entry );
		    }
	    	LOG.debug("found: {}",found);
		    cursor = connection.search( LDAP_AdminGroup, "(memberUid="+uid+")", SearchScope.OBJECT );
		    for (Entry entry : cursor) {
		    	found=Authentication.MINECRAFT_ADMIN;
		        System.out.println( entry );
		    }
		    connection.close();
			return found;
		} catch (IOException | LdapException e) {
			LOG.error("ex",e);
		}
		return Authentication.ERROR;
	}
}
