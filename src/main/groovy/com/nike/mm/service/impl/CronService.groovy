package com.nike.mm.service.impl

import java.text.MessageFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Service

import com.nike.mm.dto.MeasureMentorJobsConfigDto
import com.nike.mm.facade.IMeasureMentorJobsConfigFacade
import com.nike.mm.facade.IMeasureMentorRunFacade
import com.nike.mm.service.ICronService

@Service
class CronService implements ICronService {

    static final String LOG_JOB_CONFIG_NOT_FOUND = "Unable to find configuration for job ID {0}"
    static final String LOG_JOB_NOT_FOUND = "Unable to find job with ID {0}"
    static final String LOG_UNABLE_TO_CANCEL_JOB = "Found but could not cancel job with ID {0}"

    private static final PageRequest EVERYTHING = new PageRequest(0, Integer.MAX_VALUE)

    private final Map<String, ScheduledFuture> scheduledTasks = new ConcurrentHashMap();

    @Autowired
    TaskScheduler scheduler

    @Autowired
    IMeasureMentorRunFacade measureMentorRunFacade

    @Autowired
    IMeasureMentorJobsConfigFacade measureMentorJobsConfigFacade

    @PostConstruct
    void loadJobs() {
        def jobs = measureMentorJobsConfigFacade.findListOfJobs(EVERYTHING).getContent();
        jobs.each( this.&processJob )
    }
    
    @Override
    void processJob(String jobId) {
        processJob(this.retrieveJobFromId(jobId))
    }

    void processJob(MeasureMentorJobsConfigDto mmJConfigDto) {
        removeCronJob(mmJConfigDto)
        addCronJob(mmJConfigDto)
    }

    
    
    /**
     * Retrieve the job configuration from the database.
     * @param jobId
     * @return MeasureMentorJobsConfigDto instance
     * @throws CronJobRuntimeException if no record found
     */
    private MeasureMentorJobsConfigDto retrieveJobFromId(String jobId) {
        MeasureMentorJobsConfigDto mmJConfigDto = this.measureMentorJobsConfigFacade.findById(jobId)
        if (null == mmJConfigDto) {
            throw new CronJobRuntimeException(MessageFormat.format(LOG_JOB_CONFIG_NOT_FOUND, jobId))
        }
        return mmJConfigDto
    }

    /**
     * Cancel a cron job and remove it from the list of registered cron jobs
     * @param jobId - unique identifier for cron job
     */
    private void removeCronJob(MeasureMentorJobsConfigDto mmJConfigDto) {
        String jobId = mmJConfigDto.id;
        if (!scheduledTasks.containsKey(jobId)) {
            return;
        }
        final ScheduledFuture future = scheduledTasks.get(jobId)

        if (!future.cancel(false)) {
            throw new CronJobRuntimeException(MessageFormat.format(LOG_UNABLE_TO_CANCEL_JOB, jobId))
        }

        scheduledTasks.remove(jobId)
    }

    private void addCronJob(MeasureMentorJobsConfigDto mmJConfigDto) {
        String cron = mmJConfigDto.cron;
        String jobId = mmJConfigDto.id;
        
        if ( !(cron && mmJConfigDto.jobOn) ) {
            return;
        }
        
        Trigger trigger = new CronTrigger(cron);
        scheduledTasks.put(jobId, this.scheduler.schedule({
            this.measureMentorRunFacade.runJobId(jobId)
        }, trigger))
    }

}