package com.enuri.brndmkr.modelmatch.service;

import java.util.LinkedHashMap;
import java.util.Set;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.enuri.brndmkr.modelmatch.model.entity.main.TblCategory;
import com.enuri.brndmkr.modelmatch.repository.main.TblCategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

	private final JobLauncher jobLauncher;
	private final TblCategoryRepository tblCategoryRepository;
	private final JobExplorer jobExplorer;

	@Resource
	private Job popularModelJob;
	@Resource
	private Job matchingJob;
	@Resource
	private DataSource mainDataSource;

	private JobParameters createInitJobParam(String cateCode) {
		var map = new LinkedHashMap<String, JobParameter>();
		map.put("time", new JobParameter(System.currentTimeMillis()));
		map.put("cateCode", new JobParameter(cateCode));
		return new JobParameters(map);
	}

	public String runJob() {
		String result = "job empty~!!";

		try {
			Set<JobExecution> jobExecutions = jobExplorer.findRunningJobExecutions("matchingJob");
			//TODO 중복구동 방어로직 방안 찾아야함
			/*if (jobExecutions.isEmpty()) {
				jobLauncher.run(popularModelJob, createInitJobParam("14"));
			}*/

			jobLauncher.run(popularModelJob, createInitJobParam("14"));

			// 카테고리 추출하여 job 실행
			for (TblCategory tblCategory : tblCategoryRepository.findByFirstnameLikeCate("14%")) {
				String jobName = tblCategory.getCaCode();
				jobExecutions = jobExplorer.findRunningJobExecutions(jobName);

				// 카테고리 추출하여 job 실행
				if (jobExecutions.isEmpty()) {
					log.info("{} job start~!!", tblCategory.getCaCode());
					jobLauncher.run(matchingJob, createInitJobParam(tblCategory.getCaCode()));
					result = "success %s~!!";
				} else {
					result = "already %s~!!";
				}

				result = result.formatted(jobName);
			}
		} catch (Exception e) {
			result = "fail %s~!!";
			log.error("", e);
		}

		log.info("runJob result = {}", result);
		return result;
	}
}