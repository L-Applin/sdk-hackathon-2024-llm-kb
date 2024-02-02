package software.amazon.awssdk;

import org.json.JSONObject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;

import static software.amazon.awssdk.Constant.CLAUDE_V2_ID;
import static software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateType.KNOWLEDGE_BASE;

public class QuestionEvaluator {
    private BedrockAgentRuntimeAsyncClient bedrockAgentClient;
    private BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    private String knowledgeBaseId;

    public QuestionEvaluator() {
        this.knowledgeBaseId = Constant.KB_ID;
        this.bedrockAgentClient = Constant.BEDROCK_AGENT_RUNTIME_CLIENT;
        this.bedrockRuntimeAsyncClient = Constant.BEDROCK_RUNTIME_CLIENT;
    }

    public void evaluateResonseFor(String prompt) {
        Question question = new Question();
        question.question = prompt;

        // ask the prompt to the KB
        question.kbAnswer = invokeKb(prompt);

        // ask the same prompt to Claude
        question.claudeAnswer = invokeClaude(prompt);

        // ask models to evaluate questions
        String evaluationPrompt =
                STR."""
                Question: \{question.question}
                Answer #1: \{question.kbAnswer}
                Answer #2: \{question.claudeAnswer}

                Given the question about the AWS SDK for Java, state wether Answer #1 or Answer #2 provides
                the most accurate answer.
                Explain why the chosen better answer is the better one.
                """;

        System.out.println(STR."evaluation prompt: \{evaluationPrompt}");

        String claudeEvaluation = invokeClaude(evaluationPrompt);
        System.out.println("========== Claude evaluation ===========");
        System.out.println(claudeEvaluation);
        System.out.println();

        String kbEvaluation = invokeKb(evaluationPrompt);
        System.out.println("========== KB evaluation ===========");
        System.out.println(kbEvaluation);
        System.out.println();
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
        private String question;
        private String kbAnswer;
        private String claudeAnswer;
    }

    public static void main(String[] args) {
        String question = "Hi. Can you point me to a java example where one does client side encryption with the DDB Enhanced Client with a KMS key from a different account than the one hosting the table?";
        String requestHandlerQ = """
               There are two versions of the AWS SDK for Java: v1 (1.x) and v2 (2.x).
               Version 1 will reach end-of-life on December 31, 2025 so customers need help migrating to v2.
               Using the context of the current conversation, answer the question:
               What's the v2 equivalent of RequestHandler2? Is it ExecutionInterceptor?
               """;
        QuestionEvaluator evaluator = new QuestionEvaluator();
        evaluator.evaluateResonseFor(requestHandlerQ);
    }
}
