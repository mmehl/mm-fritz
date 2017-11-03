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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEnabler {
	static Logger LOG = LoggerFactory.getLogger(TestEnabler.class);

	@Test
	public void testEnableDisable() throws Exception {
		PropertyManager prop = new PropertyManager();
		EnableDisableMinecraft em = new EnableDisableMinecraft(prop);
		boolean ok = em.enableMinecraft();
		LOG.debug("enable: {}",ok);
		ok=em.disableMinecraft();
		LOG.debug("disable: {}",ok);
		em.close();
	}

	@Test
	public void testEnable2() throws Exception {
		PropertyManager prop = new PropertyManager();
		EnableDisableMinecraft em = new EnableDisableMinecraft(prop);
		boolean ok = em.enableMinecraft();
		LOG.debug("enable1: {}",ok);
		ok = em.enableMinecraft();
		LOG.debug("enable2: {}",ok);
		ok = em.disableMinecraft();
		LOG.debug("disable1: {}",ok);
		ok = em.disableMinecraft();
		LOG.debug("disable2: {}",ok);
		em.close();
	}
}
