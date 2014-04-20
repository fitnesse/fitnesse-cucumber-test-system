package org.fitnesse.cucumber;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import fitnesse.testsystems.*;

import static fitnesse.html.HtmlUtil.escapeHTML;
import static java.lang.String.format;

/**
 * <p>Cucumber test system.</p>
 */
public class CucumberTestSystem implements TestSystem {

    private final String name;
    private final ClassLoader classLoader;
    private final CompositeTestSystemListener testSystemListener;

    private boolean started = false;
    private TestSummary testSummary;

    public CucumberTestSystem(String name, ClassLoader classLoader) {
        super();
        this.name = name;
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
    public void runTests(TestPage pageToTest) throws IOException, InterruptedException {
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        testSummary = new TestSummary();

        testSystemListener.testStarted(pageToTest);

        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            // TODO: run page content
            pageToTest.getContent();
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            testSystemListener.testComplete(pageToTest, testSummary);
        }
    }

    @Override
    public boolean isSuccessfullyStarted() {
        return started;
    }

    @Override
    public void addTestSystemListener(TestSystemListener listener) {
        testSystemListener.addTestSystemListener(listener);
    }

    private Collection<Object> resolveClassInstances(Collection<String> stepNames) {
        List<Object> steps = new LinkedList<>();
        for (String stepName : stepNames) {
            try {
                steps.add(classLoader.loadClass(stepName).newInstance());
            } catch (Exception e) {
                processStep(format("Unable to load steps from %s: %s", stepName, e.toString()), ExecutionResult.ERROR);
            }
        }
        return steps;
    }


    private void println(String message) {
        output(format("%s<br/>", message));
    }

    private void output(String message) {
        try {
            testSystemListener.testOutputChunk(message);
        } catch (IOException e) {
            throw new RuntimeException("Unable to send ", e);
        }
    }

    private void processStep(String message, ExecutionResult result) {
        testSummary.add(result);
        output(format("<span class='%s'>%s</span><br/>", result.name().toLowerCase(), escapeHTML(message)));
    }

}
