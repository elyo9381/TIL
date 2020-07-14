package me.elyowon.springinit.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

@RestController
@Component
public class SampleRunner implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(SampleRunner.class);
    @Autowired
    private String hello;

    @Autowired
    private ElyoProperties elyoProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("========================");
        logger.debug(hello);
        logger.debug(elyoProperties.getName());
        logger.debug(elyoProperties.getFullname());
        logger.debug("========================");


        System.out.println("================");
        System.out.println(elyoProperties.getName());
        System.out.println(elyoProperties.getAge());
        System.out.println("================");
    }
}