package org.fitnesse.cucumber;

import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.TestSystem;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CucumberTestSystemFactoryTest {

    @Test
    public void factoryReturnsRunningTestSystemInstance() throws IOException {
        CucumberTestSystemFactory factory = new CucumberTestSystemFactory();
        Descriptor descriptor = mock(Descriptor.class);
        when(descriptor.getClassPath()).thenReturn(new ClassPath(Arrays.asList("classes"), ":"));

        TestSystem testSystem = factory.create(descriptor);

        assertThat(testSystem.isSuccessfullyStarted(), is(false));
    }
}
