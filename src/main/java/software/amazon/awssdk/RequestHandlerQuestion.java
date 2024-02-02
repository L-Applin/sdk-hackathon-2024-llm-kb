package software.amazon.awssdk;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.util.List;

public class RequestHandlerQuestion extends StaticConversation {
    public RequestHandlerQuestion(String knowledgeBaseId, BedrockAgentRuntimeAsyncClient bedrockAgentClient) {
        super(knowledgeBaseId, bedrockAgentClient);
    }

    @Override
    public List<String> prompts() {

        List<String> prompts = List.of(
                "Play the role of a software engineer with expertise on the AWS SDK for Java for the whole discussion.\nDescribe what are the use cases for the class RequestHandler2 in the java v1 SDK",
                STR."""
               There are two versions of the AWS SDK for Java: v1 (1.x) and v2 (2.x).
               Version 1 will reach end-of-life on December 31, 2025 so customers need help migrating to v2.

               Using the context of the current conversation, answer the question:
               What's the v2 equivalent of RequestHandler2? Is it ExecutionInterceptor?
               """
        );
        return prompts;
    }
}
