package com.nike.mm.repository.ws.impl

import com.nike.mm.dto.HttpRequestDto
import com.nike.mm.entity.plugins.Github
import com.nike.mm.repository.es.plugins.IGithubEsRepository
import com.nike.mm.repository.ws.IGithubWsRepository
import com.nike.mm.service.IHttpRequestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class GithubWsRepository implements IGithubWsRepository {
	
	@Autowired IHttpRequestService httpRequestService;
	
	@Autowired IGithubEsRepository githubEsRepository;
	
	List<String> findAllRepositories(final HttpRequestDto dto) {
		def json = this.httpRequestService.callRestfulUrl(dto);
		//TODO need to figure out paging if(!json.isLastPage){ logger.info("PAGING PROJECTS..."); getAll(path, [start: start+limit, limit: limit]);}
		def projectList = []
		for (def i : json) {
			projectList.add(i.name)
		}
		return projectList;
	}
	
	List<Github> findAllCommitsForRepository(final HttpRequestDto dto) {
		String originalPath = dto.getPath();
		def json 			= this.httpRequestService.callRestfulUrl(dto);
		
		return json.collect {c ->  dto.path =      "$originalPath/$c.sha";
			retrieveCommit(dto, c.sha)
		};
	}

	private Github retrieveCommit(HttpRequestDto dto, sha) {
		def commit 		= this.httpRequestService.callRestfulUrl(dto)
		def githubData 	= this.githubEsRepository.findBySha(sha)
		
		if(!githubData) {
			githubData = [sha: sha]
		}
		 
		githubData.created 		= commit.commit.committer.date
		githubData.linesAdded 	= commit.stats.additions
		githubData.linesRemoved = commit.stats.deletions
		githubData.author 		= commit.commit.committer.name
		
		return githubData
	}
	
	List<Github> findAllPullRequests(final HttpRequestDto dto) {
		//TODO Complete this.
		List<Github> rlist = [];
//		https://api.github.com/repos/cwhd/measurementor/pulls?access_token=????
		return rlist;
	}
}
