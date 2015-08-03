package com.nike.mm.service.impl

import com.google.common.collect.Maps
import com.nike.mm.dto.MeasureMentorJobsConfigDto
import com.nike.mm.facade.IMeasureMentorJobsConfigFacade
import com.nike.mm.facade.IMeasureMentorRunFacade
import com.nike.mm.service.ICronService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Service

import java.text.MessageFormat
import java.util.concurrent.ScheduledFuture

@Service
class CronService implements ICronService {

	static final String LOG_JOB_CONFIG_NOT_FOUND = "Unable to find configuration for job ID {0}"
	static final String LOG_JOB_NOT_FOUND = "Unable to find job with ID {0}"
	static final String LOG_UNABLE_TO_CANCEL_JOB = "Found but could not cancel job with ID {0}"

	static private final Map<String, ScheduledFuture> SCHEDULED_TASKS = Maps.newHashMap();

	@Autowired
	ThreadPoolTaskScheduler threadPoolTaskScheduler

	@Autowired
	IMeasureMentorRunFacade measureMentorRunFacade

	@Autowired
	IMeasureMentorJobsConfigFacade measureMentorJobsConfigFacade

	@Override
	void processJob(String jobId) {

		MeasureMentorJobsConfigDto mmJConfigDto = this.retrieveJobFromId(jobId)
		removeCronJob(jobId)

		if (mmJConfigDto.cron && mmJConfigDto.jobOn) {
			this.addCronJob(jobId, mmJConfigDto.cron)
		}
	}

	/**
	 * Retrieve the job configuration from the database.
	 * @param jobId
	 * @return MeasureMentorJobsConfigDto instance
	 * @throws CronJobRuntimeException�if no record found
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
	private static void removeCronJob(final String jobId) {
		if (!SCHEDULED_TASKS.containsKey(jobId)) {
			return;
		}
		final ScheduledFuture future = SCHEDULED_TASKS.get(jobId)

		if (null == future) {
			throw new CronJobRuntimeException(MessageFormat.format(LOG_JOB_NOT_FOUND, jobId))
		}

		if (!future.cancel(false)) {
			throw new CronJobRuntimeException(MessageFormat.format(LOG_UNABLE_TO_CANCEL_JOB, jobId))
		}

		SCHEDULED_TASKS.remove(jobId)
	}

	private void addCronJob(String jobId, String cron) {

		Trigger trigger = new CronTrigger(cron);
		SCHEDULED_TASKS.put(jobId, this.threadPoolTaskScheduler.schedule({
			this.measureMentorRunFacade.runJobId(jobId)
		}, trigger))
	}

}