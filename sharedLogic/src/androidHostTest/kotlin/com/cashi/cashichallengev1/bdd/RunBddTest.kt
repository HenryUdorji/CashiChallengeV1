package com.cashi.cashichallengev1.bdd

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/androidHostTest/resources/features"],
    glue = ["com.cashi.cashichallengev1.bdd"],
    plugin = ["pretty", "html:build/reports/cucumber.html"]
)
class RunBddTest
