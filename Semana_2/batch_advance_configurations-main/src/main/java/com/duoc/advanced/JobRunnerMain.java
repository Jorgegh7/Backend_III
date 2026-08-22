package com.duoc.advanced;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class JobRunnerMain {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("com.duoc.advanced");
        JobRunner jobRunner = context.getBean(JobRunner.class);
        jobRunner.runVentasJob();
        SpringApplication.exit(context);
    }
}