package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.application.book.SyncAllGutendexBooksUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SyncGutendexJobService {

    private static final Logger log = LoggerFactory.getLogger(SyncGutendexJobService.class);

    private final SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase;

    public SyncGutendexJobService(SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase) {
        this.syncAllGutendexBooksUseCase = syncAllGutendexBooksUseCase;
    }

    @Async("gutendexSyncExecutor")
    public void triggerSync() {
        triggerSync(1);
    }

    @Async("gutendexSyncExecutor")
    public void triggerSync(int startPage) {
        log.info("Job de sincronização iniciado em thread separada, começando da página {}.", startPage);
        syncAllGutendexBooksUseCase.execute(startPage);
        log.info("Job de sincronização finalizado.");
    }
}
