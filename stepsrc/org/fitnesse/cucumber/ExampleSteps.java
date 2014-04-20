package org.fitnesse.cucumber;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ExampleSteps {
    int x;

    @Given("a variable x with value $value")
    public void givenXValue(int value) {
        x = value;
    }

    @When("I multiply x by $value")
    public void whenImultiplyXBy(int value) {
        x = x * value;
    }

    @Then("x should equal $outcome")
    public void thenXshouldBe(int value) {
        if (value != x)
            throw new AssertionError("x is " + x + ", but should be " + value);
    }

}