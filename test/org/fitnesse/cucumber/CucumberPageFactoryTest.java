package org.fitnesse.cucumber;

import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPage;
import fitnesse.wiki.fs.FileSystemPageFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class CucumberPageFactoryTest {
    private FileSystemPageFactory factory;
    private FileSystemPage root;

    @Before
    public void setUp() {
        factory = new FileSystemPageFactory();
        factory.registerWikiPageFactory(new CucumberPageFactory());
        root = factory.makePage(new File("./FitNesseRoot"), "FitNesseRoot", null, new SystemVariableSource());
    }

    @Test
    public void shouldLoadTocPage() {
        WikiPage page = root.getChildPage("FeatureFiles");
        assertThat(page, not(nullValue()));
        assertThat(page.getName(), is("FeatureFiles"));
        assertThat(page.getData().getContent(), is("!contents"));
    }

    @Test
    public void tocPageShouldHaveChildren() {
        WikiPage page = root.getChildPage("FeatureFiles");
        List<WikiPage> children = page.getChildren();
        assertThat(children, not(nullValue()));
        assertThat(children.size(), is(1));
        assertThat(children.get(0).getName(), is("SimpleFeature"));
    }

    @Test
    public void tocPageCanRender() {
        WikiPage page = root.getChildPage("FeatureFiles");
        assertThat(page.getHtml(), not(nullValue()));
    }

    @Test
    public void canResolveVariablesDefinedInAParentPage() {
        WikiPage features = root.getChildPage("FeatureFiles");
        WikiPage simpleStory = features.getChildPage("SimpleFeature");
        assertThat(features.getVariable("TEST_VAR"), is("my value"));
        assertThat(simpleStory.getVariable("TEST_VAR"), is("my value"));
    }

    @Test
    public void testPageWillAlwaysResolveTestSystemToCucumber() {
        WikiPage features = root.getChildPage("FeatureFiles");
        WikiPage simpleStory = features.getChildPage("SimpleFeature");
        assertNull("TEST_SYSTEM should not be defined", features.getVariable("TEST_SYSTEM"));
        assertThat(simpleStory.getVariable("TEST_SYSTEM"), is("cucumber"));
    }

    @Test
    public void shouldLoadSymlinkedFeaturesFolder() {
        WikiPage symlinked = root.getChildPage("SymLinked");
        WikiPage page = symlinked.getChildPage("FeatureFiles");
        List<WikiPage> children = page.getChildren();

        assertThat(page, instanceOf(SymbolicPage.class));
        assertThat(((SymbolicPage) page).getRealPage(), instanceOf(CucumberTocPage.class));

        assertThat(children, not(nullValue()));
        assertThat(children.size(), is(2));
        assertThat(children.get(0).getName(), is("simplefeature"));
    }
}
