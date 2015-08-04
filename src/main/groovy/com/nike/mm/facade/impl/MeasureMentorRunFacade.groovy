package com.nike.mm.facade.impl

import static com.nike.mm.core.CollectionsTools.enforceAsCollection;

import com.google.common.collect.Lists
import com.nike.mm.business.internal.IJobHistoryBusiness
import com.nike.mm.business.internal.IMeasureMentorJobsConfigBusiness
import com.nike.mm.business.internal.IMeasureMentorRunBusiness
import com.nike.mm.business.plugins.IMeasureMentorBusiness
import com.nike.mm.dto.JobRunRequestDto
import com.nike.mm.dto.JobRunResponseDto
import com.nike.mm.entity.internal.JobHistory
import com.nike.mm.facade.IMeasureMentorRunFacade
import com.nike.mm.service.IDateService
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import org.jasypt.util.text.TextEncryptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.text.SimpleDateFormat

@Service
@Slf4j
class MeasureMentorRunFacade implements IMeasureMentorRunFacade {

    @Autowired
    Set<IMeasureMentorBusiness> measureMentorBusinesses

    @Autowired
    IMeasureMentorJobsConfigBusiness measureMentorConfigBusiness

    @Autowired
    IMeasureMentorRunBusiness measureMentorRunBusiness

    @Autowired
    IJobHistoryBusiness jobHistoryBusiness

    @Autowired
    IDateService dateService

    @Autowired
    TextEncryptor textEncryptor

    @Value('${mm.plugin.defaultStartDate}')
    String defaultFromDateString

    Date defaultFromDate

    @PostConstruct
    void setupFromConfig() {
        this.defaultFromDate = new SimpleDateFormat("dd/MM/yyyy").parse(this.defaultFromDateString)
    }

    @Override
    void runJobId(final String jobid) {
        log.debug("Running job ID $jobid")

        final def startDate = this.dateService.currentDateTime

        try {
            this.measureMentorRunBusiness.startJob(jobid)
            final def entity = this.measureMentorConfigBusiness.findById(jobid)
            final
            def config = new JsonSlurper().parseText(this.textEncryptor.decrypt(new String(Base64.getDecoder().decode
                    (entity.encryptedConfig))))

            // create job history entry
            final JobHistory jh = this.jobHistoryBusiness.createJobHistory(jobid, startDate)

            final List<JobRunRequestDto> requests = createRequestsFromConfig(jobid, jh.id, config)

            final List<JobRunResponseDto> responses = Lists.newArrayListWithCapacity(requests.size())

            // execute all plugins in parallel
            GParsPool.withPool {
                requests.eachParallel { final request ->
                    this.invokePlugin(request, responses)
                }
            }

            this.jobHistoryBusiness.saveJobRunResults(jh.id, this.dateService.currentDateTime, responses)

        } finally {
            this.measureMentorRunBusiness.stopJob(jobid);
        }
    }

    @Override
    String validateConfig(final Object config) {
        final IMeasureMentorBusiness plugin = this.findByType(config.type);
		
        return plugin ? plugin.validateConfig(config) : "No measure mentor configured for: $config.type";
    }

    private List<JobRunRequestDto> createRequestsFromConfig(
            final String jobid, final String jobHistoryId, final def configs) {

		return enforceAsCollection( configs ).collect { final config -> createRequestfromConfig(jobid, jobHistoryId, config) }
    }

    /**
     * Create a request object for a given plugin
     * @param jobid - ID of the configuration that will be run
     * @param jobHistoryId - Job run ID
     * @param config - Configuration that will be executed
     * @return Request object
     */
    private JobRunRequestDto createRequestfromConfig(
            final String jobid, final String jobHistoryId, final def config) {
        return new JobRunRequestDto(
                jobid: jobid,
                jobHistoryId: jobHistoryId,
                config: config,
                pluginType: config.type)
    }

    private void invokePlugin(final JobRunRequestDto request, final List<JobRunResponseDto> responses) {

        log.debug("Running plugin $request.pluginType")

        final IMeasureMentorBusiness plugin = this.findByType(request.pluginType)
        if (null == plugin) {
            final String errorMessage = "No plugin found for config type: $request.pluginType"
            responses.add(createFailureResponse(request.pluginType, errorMessage))
        } else {
            final String errorMessage = plugin.validateConfig(request.config)
            if (errorMessage) {
                responses.add(createFailureResponse(request.pluginType, errorMessage))
            } else {
                // run plugin
                try {
                    responses.add(plugin.updateDataWithResponse(defaultFromDate, request.config))
                } catch (final RuntimeException rte) {
                    responses.add(createExceptionResponse(request.pluginType, rte))
                }
            }
        }
    }

    private JobRunResponseDto createFailureResponse(final String pluginType, final String errorMessage) {
        log.warn("Plugin $pluginType failed: $errorMessage")
        new JobRunResponseDto(type: pluginType, recordsCount: 0, status: JobHistory.Status.error, errorMessage:
                errorMessage)
    }

    private JobRunResponseDto createExceptionResponse(final String pluginType, final Throwable e) {
        log.error("Plugin $pluginType failed: $e.message", e)
        new JobRunResponseDto(type: pluginType, recordsCount: 0, status: JobHistory.Status.error, errorMessage:
                e.message)
    }

    private IMeasureMentorBusiness findByType(final String type) {
		
        return this.measureMentorBusinesses.find({business -> business.type() == type});
    }
}
