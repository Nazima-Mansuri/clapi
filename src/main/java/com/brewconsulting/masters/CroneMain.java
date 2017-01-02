package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CroneJobDemo;
import com.brewconsulting.login.Secured;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by lcom53 on 23/12/16.
 */
@Path("crone")
@Secured
public class CroneMain {

    @POST
    @Produces("application/json")
    @Secured
    public Response cron() throws SchedulerException {

        Response resp = null;
        JobDetail job = JobBuilder.newJob(CroneJobDemo.class)
                .withIdentity("dummyJobName", "group1").build();

        //Quartz 1.6.3
        //CronTrigger trigger = new CronTrigger();
        //trigger.setName("dummyTriggerName");
        //trigger.setCronExpression("0/5 * * * * ?");

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("dummyTriggerName", "group1")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 * * * * ?"))
                .build();

        //schedule it
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);

        return Response.ok("Done Job").type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
