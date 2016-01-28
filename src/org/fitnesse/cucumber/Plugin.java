package org.fitnesse.cucumber;

import fitnesse.plugins.PluginFeatureFactoryBase;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.wiki.WikiPageFactoryRegistry;

import java.util.logging.Logger;

public class Plugin extends PluginFeatureFactoryBase {
    private static final Logger LOG = Logger.getLogger(Plugin.class.getName());

    @Override
    public void registerWikiPageFactories(WikiPageFactoryRegistry wikiPageFactoryRegistry) {
        wikiPageFactoryRegistry.registerWikiPageFactory(new CucumberPageFactory());
    }

    @Override
    public void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) {
        testSystemFactoryRegistry.registerTestSystemFactory(CucumberTestSystem.TEST_SYSTEM_NAME, new CucumberTestSystemFactory());
        LOG.info("Registered Cucumber test system");
    }
}
