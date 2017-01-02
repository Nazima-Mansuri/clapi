package com.brewconsulting.DB.masters;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by lcom53 on 23/12/16.
 */
public class CroneJobDemo implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("Hello Quartz!");
    }
}
