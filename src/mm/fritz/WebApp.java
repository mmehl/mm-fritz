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
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mm.fritz.AuthenticationManager.Authentication;


	public class WebApp extends HttpServlet {

		private AuthenticationManager auth;
		private DatabaseManager db;
		private static final long serialVersionUID = 1L;
		private static Logger LOG=LoggerFactory.getLogger(WebApp.class);
		private PropertyManager prop=null;
		final static int MAX = 35;
		private ScheduledExecutorService scheduler=null;

		@Override
		public void init(ServletConfig config) throws ServletException {
			LOG.debug("init");
			scheduler = Executors.newSingleThreadScheduledExecutor();
			super.init(config);
			prop = new PropertyManager();
			auth = new AuthenticationManager(prop);
			db = new DatabaseManager(prop);
		}
		@Override
		public void destroy() {
			scheduler.shutdown();
			scheduler = null;
			prop = null;
			db = null;
		}


		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
			LOG.debug("doGet");

		    String pass = request.getParameter("pass");
		    String user = request.getParameter("user");
		    String minutes = request.getParameter("minutes");
		    int minutesBeforeReset=35;
		    if (minutes != null) {
		    	try {
		    		int min = Integer.parseInt(minutes);
		    		if (min<minutesBeforeReset) minutesBeforeReset=min;
		    	} catch (NumberFormatException ex) {
		    		LOG.debug("no int: {}",minutes);
		    	}
		    }
		    Authentication isMinecraft = auth.tryLogin(user, pass);

		    response.setContentType("text/plain");
		    PrintWriter pw = response.getWriter();

		    switch (isMinecraft) {
		    case MINECRAFT_USER:
				pw.println("<p>Passwort ok</p>");
				Boolean tryStart = false;
				try {
					tryStart = db.tryStart(user);
					if (!tryStart) {
						pw.println("<p>Minecraft nur einmal am Tag starten :-)</p>");
					}
				} catch (Error e) {
					pw.println("<p>DB failed: ");
					pw.println(e.getMessage());
					pw.println("</p>");
				}
				if (tryStart) {
					enableMinecraft(user,pw,minutesBeforeReset);
				}
				break;
		    case NO_MINECRAFT_USER:
				pw.println("<p>Kein Minecraft User</p>");
				break;
		    case LOGIN_ERROR:
				pw.println("<p>Passwort not ok</p>");
				break;
		    case MINECRAFT_ADMIN:
				pw.println("<p>Admin login ok</p>");
				enableMinecraft(user,pw,minutesBeforeReset);
				break;
		    case ERROR:
			default:
				pw.println("<p>Fehler</p>");
			}

		    pw.flush();
		    pw.close();
		}

		public void enableMinecraft(String user, PrintWriter pw, int max) {
			EnableDisableMinecraft em;
			try {
				em = new EnableDisableMinecraft(prop);
			} catch (Exception e2) {
				LOG.error("enable failed",e2);
				pw.println("<p>Freigabe fehlgeschlagen</p>");
				return;
			}
			try {
				boolean ok = em.enableMinecraft();
				if (!ok) {
					pw.println("<p>Minecraft war schon freigegeben</p>");
				}
				scheduler.schedule(() -> {
					try {
						em.disableMinecraft();
						em.close();
					} catch (Exception e) {
						LOG.error("disable failed",e);
					}
				}, max, TimeUnit.MINUTES);
			} catch (Exception e1) {
				LOG.error("ex",e1);
				pw.println("<p>Freigabe fehlgeschlagen</p>");
				return;
			}
			db.logStart(user);
			pw.println("<p>Minecraft freigegeben</p>");
		}
}
