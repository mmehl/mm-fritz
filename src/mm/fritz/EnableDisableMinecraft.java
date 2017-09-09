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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;


public class EnableDisableMinecraft {
		static Logger LOG = LoggerFactory.getLogger(EnableDisableMinecraft.class);
		private WebClient webClient;
		private HtmlPage page;

		private final String USER;
		private final String PASS;
		
		public EnableDisableMinecraft(PropertyManager prop) throws Exception {
			USER = prop.getProperty("mm.fritz.box.user", "user");
			PASS = prop.getProperty("mm.fritz.box.pass", "pass");
	        LOG.debug("0-Startseite");
		    webClient = new WebClient();
		    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
	       	page = webClient.getPage("http://fritz.box");
	        LOG.debug("Title {}",page.getTitleText());
	        step1Login();
		}

		private void step1Login() throws IOException {
	        LOG.debug("1-Login");
			final HtmlForm form = page.getFormByName("");

			final HtmlSelect userField = form.getSelectByName("uiUser");
			final HtmlInput textField = form.getInputByName("uiPass");
			final HtmlButton hbutton = form.getButtonByName("");

			userField.setSelectedAttribute(USER, true);
			// Change the value of the text field
			textField.setValueAttribute(PASS);

			// Now submit the form by clicking the button and get back the second page.
			page = hbutton.click();
		}

		public void enableMinecraft() throws Exception {
	        LOG.debug("enable");
		        step2Filter();
		        step3Zugangsprofile();
		        step4StandardProfile();
		        step5aEnableMinecraft();
		        step6Ok();
		}

		public void disableMinecraft() throws Exception {
	        LOG.debug("disable");
		        step2Filter();
		        step3Zugangsprofile();
		        step4StandardProfile();
		        step5bDisableMinecraft();
		        step6Ok();
		}


		public void close() {
	        LOG.debug("close");
			webClient.close();
		}

		public void step6Ok() throws IOException {
	        LOG.debug("6-Ok");
			// suche nach
			//  <div id="btn_form_foot">
			//  <button type="submit" name="apply" id="uiApply">
			List<Object> applyButtons = page.getByXPath("//div[@id='btn_form_foot']/button[@name='apply']");
			if (applyButtons.size()!=1) {
				LOG.error("applyButtons.size(): {}",applyButtons.size());
				System.exit(1);
			}
			HtmlButton applyButton = (HtmlButton) applyButtons.get(0);
			LOG.debug("applyButton {}"+applyButton.asXml());
			page = applyButton.click();
		}

		public void step5bDisableMinecraft() throws Exception {
	        LOG.debug("5a-DisableMinecraft");
			// <select id="uiNetappsSelect" name="choosenetapps">
			// <option value="9">                 Minecraft              </option>
			List<Object> options = page.getByXPath("//select[@id='uiNetappsSelect']/option[normalize-space()='Minecraft']");
			if (options.size()!=1) {
				LOG.error("no Minecraft option");
				throw new Exception("Already disabled");
			}
			HtmlOption applyButton = (HtmlOption) options.get(0);
			page = applyButton.click();
		}

		public void step5aEnableMinecraft() throws Exception {
	        LOG.debug("5a-EnableMinecraft");
			// suche nach delete Minecraft
			List<Object> deleteButtons = page.getByXPath("//tr[normalize-space(td)='Minecraft']/td/button");
			if (deleteButtons.size()!=1) {
				LOG.error("deleteButtons.size(): {}",deleteButtons.size());
				throw new Exception("Already enabled");
			}
			HtmlButton deleteMinecraft = (HtmlButton) deleteButtons.get(0);
			LOG.debug("deleteMinecraft {}"+deleteMinecraft.asXml());
			page = deleteMinecraft.click();
		}

		public void step4StandardProfile() throws IOException {
	        LOG.debug("4-StandardProfil");
			// suche nach <button type="submit" name="edit" value="filtprof1" class="icon edit" title="Bearbeiten">
			HtmlButton btn = (HtmlButton) page.getByXPath("//tr[td/@datalabel='Standard']/td/button[@name='edit']").get(0);
			LOG.debug("btn {}"+btn.asXml());
			page = btn.click();
		}

		public void step3Zugangsprofile() throws IOException {
	        LOG.debug("3-Zugangsprofile");
			// <a href="/?sid=da36d0b0922d6af6&amp;lp=kidPro" id="kidPro" class=" tab show" role="menuitem" aria-hidden="false" style="order: 20;">
			// Zugangsprofile
			HtmlAnchor zugangsprofile = (HtmlAnchor) page.getElementById("kidPro");
			page = zugangsprofile.click();
		}

		public void step2Filter() throws IOException {
	        LOG.debug("2-Filter");
			// <a href="/?sid=da36d0b0922d6af6&amp;lp=filter" id="filter" style="order: 40;" class="menu_item show" role="link" aria-hidden="false">
			// Filter
			HtmlAnchor link = (HtmlAnchor) page.getElementById("filter");
			if (link == null) {
				LOG.debug("filter button not found on page: {}",page.asXml());
				throw new Error("Filter button not found on page");
			}
			page = link.click();
		}



}
