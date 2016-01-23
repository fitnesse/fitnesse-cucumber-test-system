package org.fitnesse.cucumber;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ExampleSteps {
    int x;

    @Before("@withBefore")
    public void beforeScenario() {
        System.out.println("Before scenario");
    }

    @After("@withAfter")
    public void afterScenario() {
        System.out.println("After scenario");
    }

    @Before("@withFailingBefore")
    public void beforeScenarioFails() {
        System.out.println("Before scenario");
        throw new RuntimeException("Something went wrong at runtime");
    }

    @After("@withFailingAfter")
    public void afterScenarioFails() {
        System.out.println("After scenario");
        throw new RuntimeException("Something went wrong at runtime");
    }

    @Given("^a variable x with value (\\d+)$")
    public void givenXValue(int value) {
        x = value;
    }

    @When("^I multiply x by (\\d+)$")
    public void whenImultiplyXBy(int value) {
        x = x * value;
    }

    @Then("^x should equal (\\d+)$")
    public void thenXshouldBe(int value) {
        if (value != x)
            throw new AssertionError("x is " + x + ", but should be " + value);
    }

}