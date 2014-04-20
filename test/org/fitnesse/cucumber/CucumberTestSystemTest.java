package org.fitnesse.cucumber;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.Test;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.*;
import fitnesse.wiki.PageData;
import util.FileUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CucumberTestSystemTest {

    static class TestCucumberTestSystem extends CucumberTestSystem {

        public TestCucumberTestSystem(String name, ClassLoader classLoader) {
            super(name, classLoader);
        }
    }

    @Test
    public void shouldHaveAName() {
        TestSystem testSystem = new CucumberTestSystem("name", getClassLoader());

        assertThat(testSystem.getName(), is("name"));
    }

    @Test
    public void canPerformAPassingTest() throws IOException, InterruptedException {
        CucumberTestSystem testSystem = new TestCucumberTestSystem("", getClassLoader());
        WikiTestPage pageToTest = mock(WikiTestPage.class);
        PageData pageData = mock(PageData.class);
        when(pageToTest.getData()).thenReturn(pageData);
        when(pageData.getContent()).thenReturn(FileUtil.getFileContent(new File("FitNesseRoot/CucumberTestSystem/PassingCucumberTest/content.txt")));
        TestSystemListener listener = mock(TestSystemListener.class);
        testSystem.addTestSystemListener(listener);

        testSystem.start();
        testSystem.runTests(pageToTest);
        testSystem.bye();

        verify(listener).testSystemStarted(testSystem);
        verify(listener).testSystemStopped(eq(testSystem), eq((Throwable) null));
        verify(listener).testStarted(pageToTest);
        verify(listener).testComplete(eq(pageToTest), any(TestSummary.class));

    }

    @Test
    public void canPerformAFailingTest() throws IOException, InterruptedException {
        CucumberTestSystem testSystem = new TestCucumberTestSystem("", getClassLoader());
        WikiTestPage pageToTest = mock(WikiTestPage.class);
        PageData pageData = mock(PageData.class);
        when(pageToTest.getData()).thenReturn(pageData);
        when(pageData.getContent()).thenReturn(FileUtil.getFileContent(new File("FitNesseRoot/CucumberTestSystem/FailingCucumberTest/content.txt")));
        TestSystemListener listener = mock(TestSystemListener.class);
        testSystem.addTestSystemListener(listener);

        testSystem.start();
        testSystem.runTests(pageToTest);
        testSystem.bye();

        verify(listener).testSystemStarted(testSystem);
        verify(listener).testSystemStopped(eq(testSystem), eq((Throwable) null));
        verify(listener).testStarted(pageToTest);
        verify(listener).testComplete(eq(pageToTest), any(TestSummary.class));
        verify(listener, never()).testExceptionOccurred(eq((Assertion) null), any(ExceptionResult.class));
    }

    @Test
    public void canHandlePendingSteps() throws IOException, InterruptedException {
        CucumberTestSystem testSystem = new TestCucumberTestSystem("", getClassLoader());
        WikiTestPage pageToTest = mock(WikiTestPage.class);
        PageData pageData = mock(PageData.class);
        when(pageToTest.getData()).thenReturn(pageData);
        when(pageData.getContent()).thenReturn(FileUtil.getFileContent(new File("FitNesseRoot/CucumberTestSystem/FailingCucumberTest/content.txt")));
        TestSystemListener listener = mock(TestSystemListener.class);
        testSystem.addTestSystemListener(listener);

        testSystem.start();
        testSystem.runTests(pageToTest);
        testSystem.bye();

        verify(listener).testSystemStarted(testSystem);
        verify(listener).testSystemStopped(eq(testSystem), eq((Throwable) null));
        verify(listener).testStarted(pageToTest);
        verify(listener).testComplete(eq(pageToTest), any(TestSummary.class));
        verify(listener, never()).testExceptionOccurred(eq((Assertion) null), any(ExceptionResult.class));
    }

    protected ClassLoader getClassLoader() {
        return new URLClassLoader(new URL[] {}, Thread.currentThread().getContextClassLoader());
    }

}