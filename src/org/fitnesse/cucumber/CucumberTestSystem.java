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
import gherkin.formatter.*;
import gherkin.formatter.Formatter;
import gherkin.formatter.model.*;

import static fitnesse.html.HtmlUtil.escapeHTML;
import static java.lang.String.format;

/**
 * <p>Cucumber test system.</p>
 */
public class CucumberTestSystem implements TestSystem {

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
        final FitNesseFormatter formatter = new FitNesseFormatter(testSummary);

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

            System.out.println(runtime.getSnippets());
        } catch (CucumberException e) {
            formatter.testSummary.add(ExecutionResult.ERROR);
            testSystemListener.testOutputChunk("<span class='error'>Test execution failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()) + "</span>");
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            testSystemListener.testComplete(testPage, formatter.testSummary);
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


    private class FitNesseFormatter implements Formatter, Reporter {

        private Queue<Step> currentSteps = new ArrayDeque<>();
        private Queue<ExamplesTableRow> examples = new ArrayDeque<>();
        private List<String> exampleHeaders;

        private final TestSummary testSummary;
        private String examplesKeyword;


        public FitNesseFormatter() {
            this(null);
        }

        public FitNesseFormatter(final TestSummary testSummary) {
            this.testSummary = testSummary;
        }

        @Override
        public void uri(final String uri) {
        }

        @Override
        public void background(final Background background) {
            write("h4", background);
        }

        @Override
        public void syntaxError(final String state, final String event, final List<String> legalEvents, final String uri, final Integer line) {
            write("syntaxError " + state + " " + event + "<br/>");
        }

        @Override
        public void feature(final Feature feature) {
            write("h3", feature);
        }

        @Override
        public void scenarioOutline(final ScenarioOutline scenarioOutline) {
            write("h4", scenarioOutline);
        }

        @Override
        public void scenario(final Scenario scenario) {
            if (examples.isEmpty()) {
                write("h4", scenario);
            } else {
                final ExamplesTableRow values = examples.poll();
                write("<h5>" + examplesKeyword + ": ");
                for (int i = 0; i < exampleHeaders.size(); i++) {
                    if (i > 0) write(", ");
                    write(exampleHeaders.get(i) + " = " + values.getCells().get(i));
                }
                write("</h5>");
            }
        }

        private void write(String tag, DescribedStatement statement) {
            write("<" + tag + ">" + statement.getKeyword()+ ": " + statement.getName() + "</" + tag + ">");
            if (statement.getDescription() != null) {
                write("<p style='white-space: pre-line'>" + statement.getDescription() + "</p>");
            }
        }

        @Override
        public void examples(final Examples examples) {
            examplesKeyword = examples.getKeyword();
            this.examples.addAll(examples.getRows());
            this.exampleHeaders = this.examples.poll().getCells();
        }

        @Override
        public void startOfScenarioLifeCycle(final Scenario scenario) {
            currentSteps.clear();
        }

        @Override
        public void step(final Step step) {
            currentSteps.add(step);
        }

        @Override
        public void endOfScenarioLifeCycle(final Scenario scenario) {
        }

        @Override
        public void done() {
        }

        @Override
        public void close() {
        }

        @Override
        public void eof() {
        }

        @Override
        public void before(final Match match, final Result result) {
            if (result.getError() != null) {
                write("<span class='error'>Error before scenario: " + result.getError().getMessage() + "; see Execution Log for details</span>");
                executionLogListener.stdErr(result.getErrorMessage());
            }
        }

        @Override
        public void result(final Result result) {
            Step currentStep = currentSteps.poll();
            String status = result.getStatus();
            if (Result.PASSED.equals(status)) {
                processStep(currentStep, ExecutionResult.PASS);
            } else if (Result.FAILED.equals(status)) {
                processStep(currentStep, ExecutionResult.FAIL);
            } else if (Result.SKIPPED.getStatus().equals(status)) {
                processStep(currentStep, ExecutionResult.IGNORE);
            } else if (Result.UNDEFINED.getStatus().equals(status)) {
                processUndefinedStep(currentStep);
            } else {
                processStep(currentStep, ExecutionResult.ERROR);
            }
        }

        @Override
        public void after(final Match match, final Result result) {
            if (result.getError() != null) {
                write("<span class='error'>Error after scenario: " + result.getError().getMessage() + "; see Execution Log for details</span>");
                executionLogListener.stdErr(result.getErrorMessage());
            }
        }

        @Override
        public void match(final Match match) {
        }

        @Override
        public void embedding(final String mimeType, final byte[] data) {
        }

        @Override
        public void write(final String text) {
            try {
                testSystemListener.testOutputChunk(text);
            } catch (IOException e) {
                throw new RuntimeException("Unable to send output", e);
            }
        }

        private void processStep(Step step, ExecutionResult result) {
            if (testSummary != null) testSummary.add(result);
            write(format("<span class='%s'>%s%s</span><br/>", result.name().toLowerCase(), step.getKeyword(), escapeHTML(step.getName())));
        }

        private void processUndefinedStep(final Step step) {
            if (testSummary != null) testSummary.add(ExecutionResult.ERROR);
            write(format("<span class='error'>Undefined step: %s%s</span><br/>", step.getKeyword(), escapeHTML(step.getName())));
        }

    }
}
