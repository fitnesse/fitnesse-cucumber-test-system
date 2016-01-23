package org.fitnesse.cucumber;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import util.FileUtil;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CucumberTestSystemTest {

    @Test
    public void shouldHaveAName() {
        TestSystem testSystem = new CucumberTestSystem("name", null, getClassLoader());

        assertThat(testSystem.getName(), is("name"));
    }

    @Test
    public void canPerformAPassingTest() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("FitNesseRoot/CucumberTestSystem/PassingCucumberTest/content.txt");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='pass'>Given a variable x with value 2</span>"));
    }

    @Test
    public void canPerformAFailingTest() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("FitNesseRoot/CucumberTestSystem/FailingCucumberTest/content.txt");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='fail'>Then x should equal 10</span>"));
    }

    @Test
    public void canHandlePendingSteps() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("FitNesseRoot/CucumberTestSystem/FeatureWithoutCandidateSteps/content.txt");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='error'>Undefined step: Given a situation</span>"));
    }

    @Test
    public void canHandleBeforeStep() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("features/substory/withBefore.feature");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='pass'>Given a variable x with value 2</span>"));
    }

    @Test
    public void canHandleAfterStep() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("features/substory/withAfter.feature");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='pass'>Given a variable x with value 2</span>"));
    }

    @Test
    public void canHandleFailingBeforeStep() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("features/substory/withFailingBefore.feature");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='error'>Error before scenario: Something went wrong at runtime; see Execution Log for details</span>"));
    }

    @Test
    public void canHandleFailingAfterStep() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("features/substory/withFailingAfter.feature");
        String output = concatOutput(listener);
        assertThat(output, containsString("<span class='error'>Error after scenario: Something went wrong at runtime; see Execution Log for details</span>"));
    }


    @Test
    public void canHandleAcenariooutLineWithExamples() throws IOException, InterruptedException {
        TestSystemListener listener = testWithPage("features/substory/scenarioOutline.feature");
        String output = concatOutput(listener);
        assertThat(output, containsString("foo"));
    }

    // Perform test execution, assume no errors happen.
    private TestSystemListener testWithPage(final String path) throws IOException, InterruptedException {
        ExecutionLogListener executionLogListener = mock(ExecutionLogListener.class);
        WikiTestPage pageToTest = getWikiTestPage(path);
        TestSystemListener listener = mock(TestSystemListener.class);

        CucumberTestSystem testSystem = new CucumberTestSystem("", executionLogListener, getClassLoader());
        testSystem.addTestSystemListener(listener);

        testSystem.start();
        testSystem.runTests(pageToTest);
        testSystem.bye();

        verify(listener).testSystemStarted(testSystem);
        verify(listener).testSystemStopped(eq(testSystem), eq((Throwable) null));
        verify(listener).testStarted(pageToTest);
        verify(listener).testComplete(eq(pageToTest), any(TestSummary.class));
        verify(listener, never()).testExceptionOccurred(eq((Assertion) null), any(ExceptionResult.class));

        return listener;
    }


    protected ClassLoader getClassLoader() {
        return new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader());
    }

    private WikiTestPage getWikiTestPage(String path) throws IOException {
        WikiTestPage pageToTest = mock(WikiTestPage.class);
        when(pageToTest.getContent()).thenReturn(FileUtil.getFileContent(new File(path)));
        when(pageToTest.getVariable(eq("cucumber.glue"))).thenReturn("org.fitnesse.cucumber");
        return pageToTest;
    }

    private String concatOutput(final TestSystemListener listener) throws IOException {
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(listener, atLeastOnce()).testOutputChunk(captor.capture());

        StringBuilder b = new StringBuilder();
        for (String s : captor.getAllValues()) {
            b.append(s);
        }
        return b.toString();
    }

}