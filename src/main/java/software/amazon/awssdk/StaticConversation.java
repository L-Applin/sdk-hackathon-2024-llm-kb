package software.amazon.awssdk;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static software.amazon.awssdk.Constant.CLAUDE_V2_ID;
import static software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateType.KNOWLEDGE_BASE;
import static software.amazon.awssdk.utils.FunctionalUtils.invokeSafely;

public abstract class StaticConversation {
    private final Path path;
    private BedrockAgentRuntimeAsyncClient bedrockAgentClient;
    private String sessionId;
    private String knowledgeBaseId;

    public StaticConversation(String knowledgeBaseId, BedrockAgentRuntimeAsyncClient bedrockAgentClient) {
        this.bedrockAgentClient = bedrockAgentClient;
        this.knowledgeBaseId = knowledgeBaseId;
        this.path = Path.of("responses", String.format("response-%s.txt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())));
    }

    public void run() {
        invokeSafely(() -> Files.writeString(path, "========== Starting conversation ==========", CREATE));
        prompts().forEach(this::runPrompt);
        invokeSafely(() -> Files.writeString(path, "\n========== Conversation ended ==========", APPEND));
    }

    private void runPrompt(String prompt) {
        out(String.format("%nprompt:%n%s%n", prompt));
        RetrieveAndGenerateResponse response = retrieveAndGenerateResponse(prompt).join();
        if (this.sessionId == null) {
            this.sessionId = response.sessionId();
        }
        out(String.format("%nresponse:%n%s%n%n", response.output().text()));
        saveCitations(response);
    }

    private void saveCitations(RetrieveAndGenerateResponse response) {
        if (!response.hasCitations()) return;
        response.citations().forEach(citation ->
                citation.retrievedReferences().forEach(ref -> {
                    String citationLocation = String.format("    reference: %s%n", ref.location().s3Location().uri());
                    out(citationLocation);
                })
        );
    }

    private CompletableFuture<RetrieveAndGenerateResponse> retrieveAndGenerateResponse(String prompt) {
        RetrieveAndGenerateRequest.Builder builder = RetrieveAndGenerateRequest.builder()
                .input(input -> input.text(prompt))
                .retrieveAndGenerateConfiguration(conf -> conf.knowledgeBaseConfiguration(kb -> kb.knowledgeBaseId(this.knowledgeBaseId)
                                .modelArn(CLAUDE_V2_ID))
                        .type(KNOWLEDGE_BASE));
        if (this.sessionId != null) {
            builder.sessionId(this.sessionId);
        }
        return bedrockAgentClient.retrieveAndGenerate(builder.build());
    }

    private void out(String msg) {
        invokeSafely(() -> Files.writeString(path, msg, APPEND));
        System.out.println(msg);
    }

    public abstract List<String> prompts();

}
