package org.fitnesse.cucumber;

import java.io.File;
import org.junit.Test;

import fitnesse.wiki.WikiPage;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class CucumberFeaturePageTest {

    @Test
    public void scenarioNamesShouldBeRenderedAsHeaders() {
        WikiPage storyPage = new CucumberFeaturePage(new File("features/simplefeature.feature"), "simplefeature", null);
        String html = storyPage.getHtml();
        assertThat(html, containsString("<h4>Scenario: 2 squared</h4>"));
    }
}