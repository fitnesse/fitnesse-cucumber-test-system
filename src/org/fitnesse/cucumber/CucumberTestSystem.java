package org.fitnesse.cucumber;

import java.io.*;
import java.util.*;

import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.FileSystemPage;

import static java.lang.String.format;

/**
 * <p>Cucumber test system.</p>
 */
public class CucumberTestSystem implements TestSystem {

    public static final String TEST_SYSTEM_NAME = "cucumber";
    private final String name;
    private final ExecutionLogListener executionLogListener;
    private final ClassLoader classLoader;
    private final CompositeTestSystemListener testSystemListener;

    private boolean started = false;
    private TestSummary testSummary;

    public CucumberTestSystem(String name, final ExecutionLogListener executionLogListener, ClassLoader classLoader) {
        super();
        this.name = name;
        this.executionLogListener = executionLogListener;
        this.classLoader = classLoader;
        this.testSystemListener = new CompositeTestSystemListener();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws IOException {

        started = true;

        testSystemListener.testSystemStarted(this);
    }

    @Override
    public void bye() throws IOException, InterruptedException {
        kill();
    }

    @Override
    public void kill() throws IOException {
        testSystemListener.testSystemStopped(this, null);

        if (classLoader instanceof Closeable) {
            ((Closeable) classLoader).close();
        }
    }

    @Override
    public void runTests(TestPage testPage) throws IOException, InterruptedException {
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        String gluePath = testPage.getVariable("cucumber.glue");
        final TestSummary testSummary = new TestSummary();

        final FitNesseResultFormatter formatter = new FitNesseResultFormatter(testSummary,
                new Printer() {
                    @Override
                    public void write(final String text) {
                        testOutputChunk(text);
                    }
                }, new Printer() {
                    @Override
                    public void write(final String text) {
                        executionLogListener.stdErr(text);
                    }
                });

        testSystemListener.testStarted(testPage);

        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            RuntimeOptions runtimeOptions = new RuntimeOptions(Arrays.asList("--glue", gluePath));

            final List<CucumberFeature> cucumberFeatures = new ArrayList<>();
            final List<Object> filters = new ArrayList<>();
            final FeatureBuilder builder = new FeatureBuilder(cucumberFeatures);

            builder.parse(new PageResource(testPage), filters);
            ResourceLoader resourceLoader = new MultiLoader(classLoader);
            ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
            Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);

            for (CucumberFeature cucumberFeature : cucumberFeatures) {
                cucumberFeature.run(formatter, formatter, runtime);
            }

            formatter.missing(runtime.getSnippets());
        } catch (CucumberException e) {
            testSummary.add(ExecutionResult.ERROR);
            testSystemListener.testOutputChunk("<span class='error'>Test execution failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()) + "</span>");
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            testSystemListener.testComplete(testPage, testSummary);
        }
    }

    public String getPath(TestPage testPage) {
        WikiPage sourcePage = ((WikiTestPage) testPage).getSourcePage();
        if (sourcePage instanceof FileSystemPage) {
            return ((FileSystemPage) sourcePage).getFileSystemPath().getPath();
        } else if (sourcePage instanceof CucumberFeaturePage) {
            return ((CucumberFeaturePage) sourcePage).getFileSystemPath().getPath();
        }
        throw new RuntimeException("Can not parse file as Cucumber feature file: " + sourcePage);
    }

    @Override
    public boolean isSuccessfullyStarted() {
        return started;
    }

    @Override
    public void addTestSystemListener(TestSystemListener listener) {
        testSystemListener.addTestSystemListener(listener);
    }

    private void testOutputChunk(final String text) {
        try {
            testSystemListener.testOutputChunk(text);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write output", e);
        }
    }

    private static class PageResource implements Resource {
        private final TestPage testPage;

        public PageResource(final TestPage testPage) {
            this.testPage = testPage;
        }

        @Override
        public String getPath() {
            return "fitnesse";
        }

        @Override
        public String getAbsolutePath() {
            return "fitnesse";
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(testPage.getContent().getBytes());
        }

        @Override
        public String getClassName(final String extension) {
            return "fitnesse";
        }
    }


}
