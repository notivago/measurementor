package com.nike.mm.business.plugins.impl

import com.google.common.collect.Lists
import com.nike.mm.business.plugins.IGithubBusiness
import com.nike.mm.dto.HttpRequestDto
import com.nike.mm.dto.JobRunResponseDto
import com.nike.mm.entity.internal.JobHistory
import com.nike.mm.entity.plugins.Github
import com.nike.mm.repository.es.plugins.IGithubEsRepository
import com.nike.mm.repository.ws.IGithubWsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class GithubBusiness extends AbstractBusiness implements IGithubBusiness {

    public static final String MISSING_ACCESS_TOKEN = "Missing access token"

    public static final String MISSING_REPOSITORY_OWNER = "Missing repository owner"

    @Autowired
    IGithubWsRepository githubWsRepository

    @Autowired
    IGithubEsRepository githubEsRepository

    //TODO better pagination
    def start = 0
    def limit = 300

    @Override
    String type() {
        return "Github";
    }

    @Override
    String validateConfig(final Object config) {
        final List<String> errorMessages = Lists.newArrayList()
        if (!config.url) {
            errorMessages.add(MISSING_URL)
        }
        if (!config.access_token) {
            errorMessages.add(MISSING_ACCESS_TOKEN)
        }
        if (!config.repository_owner) {
            errorMessages.add(MISSING_REPOSITORY_OWNER)
        }
        return buildValidationErrorString(errorMessages)
    }

    @Override
    JobRunResponseDto updateDataWithResponse(Date lastRunDate, Object configInfo) {
        List<String> repositories = this.findAllRepositories(configInfo, lastRunDate);
        for (String repo : repositories) {
            addToEsRepository(this.getAllCommitsForRepo(configInfo, repo));
			addToEsRepository(this.getAllPullRequestsForRepo(configInfo, repo));
        }
        return [type: type(), status: JobHistory.Status.success, reccordsCount: 0] as JobRunResponseDto
    }

	private addToEsRepository(List changesets) {
		if (changesets) {
			this.githubEsRepository.save(changesets);
		}
	}

    private List<String> findAllRepositories(final Object configInfo, final Date fromDate) {
        HttpRequestDto request = createRequest("/users/$configInfo.repository_owner/repos", configInfo);
        return this.githubWsRepository.findAllRepositories(request);
    }

    private List<Github> getAllCommitsForRepo(final Object configInfo, final String repo) {
		HttpRequestDto request = createRequest("/repos/$configInfo.repository_owner/$repo/commits", configInfo);
        return this.githubWsRepository.findAllCommitsForRepository(request);
    }

    private List<Github> getAllPullRequestsForRepo(final Object configInfo, final String repo) {
		HttpRequestDto request = createRequest("/repos/$configInfo.repository_owner/$repo/pulls", configInfo);
        this.githubWsRepository.findAllPullRequests(request);
    }
	
	private HttpRequestDto createRequest(String path, configInfo) {
		return [url: configInfo.url, path: path, query: [access_token: configInfo.access_token, start: start, limit: limit]];
	}
}
