package software.amazon.awssdk;

import org.json.JSONObject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static software.amazon.awssdk.Constant.CLAUDE_V2_ID;
import static software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateType.KNOWLEDGE_BASE;
import static software.amazon.awssdk.utils.FunctionalUtils.invokeSafely;

public class QuestionEvaluator {
    private final Path path;
    private final BedrockAgentRuntimeAsyncClient bedrockAgentClient;
    private final BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    private final String knowledgeBaseId;

    public QuestionEvaluator() {
        this.knowledgeBaseId = Constant.KB_ID;
        this.bedrockAgentClient = Constant.BEDROCK_AGENT_RUNTIME_CLIENT;
        this.bedrockRuntimeAsyncClient = Constant.BEDROCK_RUNTIME_CLIENT;
        this.path = Path.of("responses", String.format("evaluation-%s.txt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())));
    }

    public void evaluateResponseFor(String prompt) {
        invokeSafely(() -> Files.writeString(path, "========== Starting conversation ==========\n", CREATE));
        out("========== PROMPT ==========");
        out(prompt);

        Question question = new Question();
        question.prompt = prompt;

        // ask the prompt to the KB
        question.kbAnswer = invokeKb(prompt);
        out("========== KB ANSWER (#1) ==========");
        out(question.kbAnswer);

        // ask the same prompt to Claude
        question.claudeAnswer = invokeClaude(prompt);
        out("========== CLAUDE ANSWER (#2) ==========");
        out(question.claudeAnswer);

        // ask models to evaluate questions
        String evaluationPrompt =
                STR."""
                Question:\{question.prompt}
                Answer #1:\{question.kbAnswer}
                Answer #2:\{question.claudeAnswer}

                Given the question about the AWS SDK for Java, state wether Answer #1 or Answer #2 provides
                the most accurate answer.
                Explain why the chosen better answer is the better one.
                """;

        String kbEvaluation = invokeKb(evaluationPrompt);
        out("========== KB evaluation ===========");
        out(kbEvaluation);
        out("\n");

        String claudeEvaluation = invokeClaude(evaluationPrompt);
        out("========== Claude evaluation ===========");
        out(claudeEvaluation);
        out("\n");
    }

    private String invokeKb(String prompt) {

        RetrieveAndGenerateRequest kbRequest= RetrieveAndGenerateRequest.builder()
                .input(input -> input.text(prompt))
                .retrieveAndGenerateConfiguration(conf -> conf.knowledgeBaseConfiguration(kb -> kb.knowledgeBaseId(this.knowledgeBaseId)
                                .modelArn(CLAUDE_V2_ID))
                        .type(KNOWLEDGE_BASE))
                .build();
        RetrieveAndGenerateResponse kbResponse = bedrockAgentClient.retrieveAndGenerate(kbRequest).join();
        return kbResponse.output().text();
    }

    private String invokeClaude(String prompt) {

        // Claude requires you to enclose the prompt as follows:
        String enclosedPrompt = STR."Human: \{prompt}\n\nAssistant:";

        String payload = new JSONObject()
                .put("prompt", enclosedPrompt)
                .put("max_tokens_to_sample", 200)
                .put("temperature", 0.5)
                .put("stop_sequences", List.of("\n\nHuman:"))
                .toString();

        InvokeModelRequest request = InvokeModelRequest.builder()
                .body(SdkBytes.fromUtf8String(payload))
                .modelId(CLAUDE_V2_ID)
                .contentType("application/json")
                .accept("application/json")
                .build();

        InvokeModelResponse response = this.bedrockRuntimeAsyncClient.invokeModel(request).join();

        JSONObject responseBody = new JSONObject(response.body().asUtf8String());
        return responseBody.getString("completion");
    }

    static class Question {
        private String prompt;
        private String kbAnswer;
        private String claudeAnswer;
    }

    private void out(String msg) {
        invokeSafely(() -> Files.writeString(path, STR."\{msg}\n", APPEND));
        System.out.println(msg);
    }

    public static void main(String[] args) {
        String socketQuestion = "How to map setSocketBufferSizeHints in ClientConfiguration (sdk v1) to sdk v2?";
        String fullPrompt = STR."""
               Keep your answer brief.
               Answer the following question related to the AWS SDK for Java:
               \{socketQuestion}
               """;
        QuestionEvaluator evaluator = new QuestionEvaluator();
        evaluator.evaluateResponseFor(fullPrompt);
    }
}
