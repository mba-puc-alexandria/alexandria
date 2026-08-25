\# Passo a Passo: Configuração do EventBridge Scheduler com Spring Boot

Este guia detalha as alterações necessárias no projeto \*\*Alexandria Backend\*\* para viabilizar a sincronização automática e semanal de livros da API Gutendex, utilizando o Amazon EventBridge Scheduler.

Como o processo de iteração por milhares de páginas pode levar um tempo considerável (devido ao \*delay\* entre requisições), a arquitetura adotada utiliza processamento assíncrono. Isso evita que o EventBridge (ou um eventual API Gateway) encerre a conexão por \*timeout\*.

\---

\#\# 💻 Fase 1: Alterações no Projeto Spring Boot

\#\#\# 1\. Habilitar Processamento Assíncrono  
Para permitir que o Spring Boot execute tarefas em segundo plano (\*background\*), você precisa habilitar o suporte assíncrono na aplicação.

\*\*O que fazer:\*\*  
Vá até a classe principal da sua aplicação (provavelmente \`AlexandriaApplication.java\`) e adicione a anotação \`@EnableAsync\`:

\`\`\`java  
import org.springframework.boot.SpringApplication;  
import org.springframework.boot.autoconfigure.SpringBootApplication;  
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication  
@EnableAsync // Habilita execução de threads em background  
public class AlexandriaApplication {  
    public static void main(String\[\] args) {  
        SpringApplication.run(AlexandriaApplication.class, args);  
    }  
}

### **2\. Criar o Use Case de Sincronização (Loop Assíncrono)**

Na sua camada de Aplicação (`application/book/`), crie um novo *Use Case* responsável por controlar o loop de páginas, aplicar o delay e tratar o encerramento.

**O que fazer:** Crie a classe `SyncAllGutendexBooksUseCase` aproveitando o seu `CreateBookUseCase` já existente.

Java  
import org.springframework.scheduling.annotation.Async;

public class SyncAllGutendexBooksUseCase {

    private final CreateBookUseCase createBookUseCase;

    public SyncAllGutendexBooksUseCase(CreateBookUseCase createBookUseCase) {  
        this.createBookUseCase \= createBookUseCase;  
    }

    @Async // Importante: Faz o método rodar fora da thread principal (requisição web)  
    public void execute() {  
        int currentPage \= 1;

        while (true) {  
            try {  
                // Reaproveita o Use Case existente para salvar a página atual  
                CreateBookInput input \= new CreateBookInput(currentPage);  
                createBookUseCase.execute(input);  
                  
                System.out.println("Página " \+ currentPage \+ " sincronizada com sucesso.");  
                currentPage++;  
                  
                // Delay de 500ms (ou outro valor) para não sobrecarregar a Gutendex  
                Thread.sleep(500); 

            } catch (RuntimeException e) {  
                // Condição de parada: a Gutendex não tem mais páginas  
                if (e.getMessage() \!= null && e.getMessage().contains("Page not found in Gutendex")) {  
                    System.out.println("Fim da sincronização alcançado: " \+ e.getMessage());  
                    break;  
                } else {  
                    System.err.println("Erro inesperado na página " \+ currentPage \+ ": " \+ e.getMessage());  
                    break;  
                }  
            } catch (InterruptedException e) {  
                Thread.currentThread().interrupt();  
                System.err.println("Sincronização interrompida.");  
                break;  
            }  
        }  
    }  
}

*Atenção: Lembre-se de registrar este novo Use Case na sua classe de configuração (`BeanConfiguration.java`).*

### **3\. Criar o Endpoint Gatilho (Webhook)**

O EventBridge fará uma requisição HTTP para a sua aplicação. Precisamos de um endpoint que atue apenas como o "gatilho" do processo assíncrono.

**O que fazer:** No seu `BookController.java` (ou num Controller específico de jobs), adicione o seguinte endpoint POST:

Java  
import org.springframework.http.ResponseEntity;  
import org.springframework.web.bind.annotation.PostMapping;  
import org.springframework.web.bind.annotation.RestController;

@RestController  
public class BookSyncController {

    private final SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase;

    public BookSyncController(SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase) {  
        this.syncAllGutendexBooksUseCase \= syncAllGutendexBooksUseCase;  
    }

    @PostMapping("/api/jobs/sync-gutendex")  
    public ResponseEntity\<Void\> triggerSync() {  
        // Dispara o job em background. O Spring não vai bloquear a thread aqui.  
        syncAllGutendexBooksUseCase.execute();  
          
        // Retorna "202 Accepted" imediatamente para o EventBridge.  
        // Significa: "Recebi a ordem e vou processar, não se preocupe mais com isso".  
        return ResponseEntity.accepted().build();   
    }  
}

---

## **☁️ Fase 2: Configurações na AWS (EventBridge Scheduler)**

Após fazer o deploy das alterações acima no seu EC2 (e garantir que a porta 80/443 está acessível via Cloudflare ou ELB), siga estes passos no painel da AWS.

### **1\. Criar a "API Destination"**

Esta configuração ensina o EventBridge como chegar no seu Spring Boot.

1. No console da AWS, busque por **Amazon EventBridge**.  
2. No menu lateral, acesse **Integration** \> **API destinations** e clique em **Create API destination**.  
3. **Name**: `GutendexSyncEndpoint`  
4. **API destination endpoint**: A URL completa do seu endpoint no EC2/Cloudflare (ex: `https://api.bibliotecaalexandria.com.br/api/jobs/sync-gutendex`).  
5. **HTTP method**: `POST`  
6. **Connection**: Clique em *Create a new connection*.  
   * **Name**: `ConexaoGutendexSync`  
   * **Authorization Type**: Se o seu endpoint for aberto, coloque *API Key* e adicione um valor fictício (ex: Key: `X-Dummy`, Value: `123`). Se for protegido pelo Spring Security, defina as credenciais adequadas (Basic Auth ou API Key configurada na sua aplicação).

### **2\. Criar o "Schedule" (O Despertador)**

Esta configuração define *quando* a chamada será feita.

1. No menu do EventBridge, vá em **Scheduler** \> **Schedules** e clique em **Create schedule**.  
2. **Schedule name**: `SincronizacaoSemanalGutendex`  
3. **Schedule pattern**: Selecione `Recurring schedule` e depois `Cron-based schedule`.  
   * **Cron expression**: Para rodar toda madrugada de domingo às 03:00, use `cron(0 3 ? * SUN *)`.  
   * **Timezone**: Selecione o fuso horário da sua preferência (ex: *America/Sao\_Paulo*).  
4. **Target detail**: Na lista de targets, escolha **API destination** (na seção de *EventBridge* ou *Universal targets*).  
   * Selecione a API Destination criada no passo anterior (`GutendexSyncEndpoint`).  
5. **Payload**: Pode deixar vazio (sua aplicação não espera um corpo no POST).  
6. **Permissions**: Selecione *Create new role for this schedule* para a AWS gerenciar as permissões automaticamente.  
7. Revise e clique em **Create schedule**.

### **3\. Validação Final**

Para garantir que tudo está funcionando:

1. Volte na edição do Schedule e coloque para rodar em 5 minutos a partir de agora (só para testar).  
2. Monitore os logs da sua aplicação Spring Boot (usando `docker logs` ou o arquivo de log no EC2).  
3. Você deverá ver a requisição chegando, o endpoint devolvendo 202 rapidamente, e o console imprimindo "Página 1 sincronizada com sucesso", "Página 2...", etc., a cada 500ms.

