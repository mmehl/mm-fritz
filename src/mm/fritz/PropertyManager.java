package mm.fritz;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyManager extends Properties {

	private static Logger LOG=LoggerFactory.getLogger(PropertyManager.class);
	public PropertyManager() {
		super();
		initProperties();
	}

	void initProperties() {
		InputStream in = null;
		try { 
			in = new FileInputStream(System.getProperty("user.home")+"/.mm.properties");
		} catch (Exception e) {
			LOG.debug("property error",e);
			// ignore
		}
		if (in == null) {
			try {
				in = new FileInputStream("/opt/mm-fritz.properties");
			} catch (Exception e) {
				LOG.debug("property error",e);
				// ignore
			}
		}
		if (in == null) {
			try {
				in = AuthenticationManager.class.getResourceAsStream("mm-fritz.properties");
			} catch (Exception e) {
				LOG.debug("property error",e);
				// ignore
			}
		}
		if (in != null) {
		  try {
			super.load(in);
			in.close();
		  } catch (Exception e) {
			LOG.error("property load error",e);
			// ignore
		  }
		}
	}
}
