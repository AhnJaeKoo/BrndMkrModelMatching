package com.enuri.brndmkr.modelmatch.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enuri.brndmkr.modelmatch.service.JobService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobController {

	private final JobService jobService;

	@GetMapping("/job/matchingJobRun")
	public ResponseEntity<String> mainSyncRun() {
		log.info("matchingJobRun~!!");
		return new ResponseEntity<>(jobService.runJob(), HttpStatus.OK);
	}

	@GetMapping("/job/test")
	public ResponseEntity<String> maintest() {
		log.info("test ~~!!");
		return new ResponseEntity<>("test!!!!", HttpStatus.OK);
	}
}