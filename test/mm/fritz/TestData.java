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

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import mm.fritz.AuthenticationManager.Authentication;

public class TestData {

	@Test
	public void testData() {
		PropertyManager prop = new PropertyManager();
		final String TEST = "test-"+RandomUtils.nextInt();
		DatabaseManager db = new DatabaseManager(prop);
		assert db.tryStart(TEST) == true;
		db.logStart(TEST);
		assert db.tryStart(TEST) == false;
	}
	
	@Test
	public void testLogin() {
		PropertyManager prop = new PropertyManager();
		AuthenticationManager auth = new AuthenticationManager(prop);
		Authentication isOk;
		isOk = auth.tryLogin("invalid","pass");
		assert isOk == Authentication.LOGIN_ERROR;
		isOk = auth.tryLogin("user","user");
		assert isOk == Authentication.MINECRAFT_USER;
		isOk = auth.tryLogin("admin","admin");
		assert isOk == Authentication.MINECRAFT_ADMIN;
	}
}
