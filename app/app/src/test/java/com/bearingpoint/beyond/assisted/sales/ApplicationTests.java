package com.bearingpoint.beyond.test-bpintegration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

//@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {

//        String ttt = "TimerEntity[repeat=null, id=54414, revision=4, duedate=Wed Nov 02 17:39:53 GMT 2022, repeatOffset=0, lockOwner=null, lockExpirationTime=null, executionId=54412, processInstanceId=53591, isExclusive=true, retries=3, jobHandlerType=timer-intermediate-transition, jobHandlerConfiguration=dafWarningTimer, exceptionByteArray=null, exceptionByteArrayId=null, exceptionMessage=null, deploymentId=53501]";
//
//        String jobconfig = ttt.substring(ttt.indexOf("jobHandlerConfiguration="));
//
//        System.out.println("JobConfig: " + jobconfig);
//
//        jobconfig = jobconfig.substring(0, jobconfig.indexOf(","));
//        jobconfig = jobconfig.substring("jobHandlerConfiguration=".length());
//
//        System.out.println("JobConfig: " + jobconfig);
//
//        String jc2 = ttt.substring(ttt.indexOf("[")+1, ttt.indexOf("]"));
//        String[] split = jc2.split(", ");
//
//        Map<String, String> headerMap = Arrays.stream(jc2.split(", "))
//                .map(s -> s.split("="))
//                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
//
//        System.out.println("JobConfig: " + headerMap);


    }

}
