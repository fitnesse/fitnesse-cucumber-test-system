package org.fitnesse.cucumber;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.VariableSource;

import java.io.File;

public class CucumberPageFactory implements WikiPageFactory {
    private static final String FEATURE_EXTENSION = ".feature";

    @Override
    public WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource) {
        return new CucumberTocPage(path, pageName, parent, variableSource);
    }

    @Override
    public boolean supports(File path) {
        if (path.isDirectory()) {
            for (String child : path.list()) {
                if (isFeatureFile(new File(path, child))) return true;
            }
        }
        return false;
    }

    static boolean isFeatureFile(File path) {
        return (path.getName().endsWith(FEATURE_EXTENSION));
    }
}
