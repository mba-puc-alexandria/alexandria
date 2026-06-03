package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncAllGutendexBooksUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncAllGutendexBooksUseCase.class);
    private static final long PAGE_DELAY_MS = 500L;

    private final CreateBookUseCase createBookUseCase;

    public SyncAllGutendexBooksUseCase(CreateBookUseCase createBookUseCase) {
        this.createBookUseCase = createBookUseCase;
    }

    public void execute() {
        int currentPage = 1;

        while (true) {
            try {
                CreateBookInput input = new CreateBookInput(currentPage);
                var output = createBookUseCase.execute(input);
                log.info("Página {} sincronizada. {} livros criados.",
                        currentPage, output.ids().size());
                currentPage++;
                Thread.sleep(PAGE_DELAY_MS);

            } catch (RuntimeException e) {
                String msg = e.getMessage();

                if (msg != null && msg.contains("Page not found in Gutendex")) {
                    log.info("Fim da sincronização: {}", msg);
                    break;
                }

                log.error("Erro na página {}, pulando para a próxima: {}", currentPage, msg);
                currentPage++;
                continue;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sincronização interrompida externamente na página {}.", currentPage);
                break;
            }
        }

        log.info("Sincronização completa. Última página processada: {}.", currentPage - 1);
    }
}
