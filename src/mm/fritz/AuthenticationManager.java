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
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

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
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, 
		        "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://"+LDAP_Server+":"+LDAP_Port);
		env.put(Context.SECURITY_PRINCIPAL, "uid="+uid+","+LDAP_User);
		env.put(Context.SECURITY_CREDENTIALS, pass);
		try {
		    Authentication found=Authentication.NO_MINECRAFT_USER;
		    Context ctx = (Context) new InitialContext(env);
		    
		    LdapContext userGroup = (LdapContext) ctx.lookup(LDAP_UserGroup);
			Attributes users = userGroup.getAttributes("", new String[] {"memberUid"});

			for (NamingEnumeration<? extends Attribute> ae = users.getAll(); ae.hasMore();) {
			    Attribute attr = ae.next();
			    NamingEnumeration<String> e = (NamingEnumeration<String>) attr.getAll();
			    while (e.hasMore()) {
			    	String val = e.next();
			    	LOG.debug("val {}",val);
					if (val.equals(uid)) {
				    	found=Authentication.MINECRAFT_USER;
					}
				}
			}
			

		    LdapContext adminGroup = (LdapContext) ctx.lookup(LDAP_AdminGroup);
			Attributes admins = adminGroup.getAttributes("", new String[] {"memberUid"});

			for (NamingEnumeration<? extends Attribute> ae = admins.getAll(); ae.hasMore();) {
			    Attribute attr = ae.next();
			    NamingEnumeration<String> e = (NamingEnumeration<String>) attr.getAll();
			    while (e.hasMore()) {
			    	String val = e.next();
			    	LOG.debug("val {}",val);
					if (val.equals(uid)) {
				    	found=Authentication.MINECRAFT_ADMIN;
					}
				}
			}

	    	LOG.debug("found: {}",found);
			return found;
		} catch (NamingException e) {
			LOG.error("Naming Exception",e);
			return Authentication.LOGIN_ERROR;	
		}

	}

}
