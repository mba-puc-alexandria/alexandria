package com.pucsp.alexandria.adapter.in.rest;

import com.pucsp.alexandria.adapter.in.job.SyncGutendexJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final SyncGutendexJobService syncGutendexJobService;

    public JobController(SyncGutendexJobService syncGutendexJobService) {
        this.syncGutendexJobService = syncGutendexJobService;
    }

    @PostMapping("/sync-gutendex")
    public ResponseEntity<Void> triggerSync(
            @RequestParam(required = false, defaultValue = "1") Integer page) {
        try {
            syncGutendexJobService.triggerSync(page);
            return ResponseEntity.accepted().build();
        } catch (TaskRejectedException e) {
            log.warn("Job de sincronização já está em execução. Requisição rejeitada.");
            return ResponseEntity.status(429).build();
        }
    }
}
