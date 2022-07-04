package com.github.search.test;

import io.qameta.allure.Step;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public class BaseTest {
    @Step
    @BeforeMethod
    public void beforeEachTestCase(Method m){
        System.out.println("Starting test: " + m.getName());
        System.out.println("Thread ID: " + Thread.currentThread().getId());
    }
}
