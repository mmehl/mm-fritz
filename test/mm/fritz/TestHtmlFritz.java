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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

public class TestHtmlFritz {
    public static Logger LOG=LoggerFactory.getLogger(TestHtmlFritz.class);

	@Test
	public void homePage() throws Exception {
	    try (final WebClient webClient = new WebClient()) {
	        final HtmlPage page = webClient.getPage("http://fritz.box");
	        LOG.debug("Title {}",page.getTitleText());
	        LOG.debug("Page: {}",page.asXml());

	        final HtmlForm form = page.getFormByName("");

	        final HtmlSelect userField = form.getSelectByName("uiUser");
	        final HtmlInput textField = form.getInputByName("uiPass");
	        final HtmlButton button = form.getButtonByName("");

	        userField.setSelectedAttribute("admin", true);
	        // Change the value of the text field
	        textField.setValueAttribute("pass");

	        // Now submit the form by clicking the button and get back the second page.
	        final HtmlPage page2 = button.click();
	        final String pageAsXml = page2.asXml();
	        LOG.debug("body: {}",pageAsXml);
	        // <a href="/?sid=da36d0b0922d6af6&amp;lp=filter" id="filter" style="order: 40;" class="menu_item show" role="link" aria-hidden="false">
            // Filter
	        HtmlAnchor link = (HtmlAnchor) page2.getElementById("filter");
	        HtmlPage page3 = link.click();
	        LOG.debug("body: {}",page3.asXml());
	        // <a href="/?sid=da36d0b0922d6af6&amp;lp=kidPro" id="kidPro" class=" tab show" role="menuitem" aria-hidden="false" style="order: 20;">
            // Zugangsprofile
	        HtmlAnchor zugangsprofile = (HtmlAnchor) page2.getElementById("kidPro");
	        HtmlPage page4 = zugangsprofile.click();
	        LOG.debug("body4: {}",page4.asXml());
	        // <button class="icon edit" type="submit" name="edit" value="filtprof1" title="Bearbeiten"></button>
	    }
	}
}