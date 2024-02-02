package software.amazon.awssdk;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.util.List;

public class TimeoutQuestion extends StaticConversation {
    public TimeoutQuestion(String knowledgeBaseId, BedrockAgentRuntimeAsyncClient bedrockAgentClient) {
        super(knowledgeBaseId, bedrockAgentClient);
    }

    @Override
    public List<String> prompts() {
        String question =
                """
                Hey, quick question about apiCallTimeouts.
                There are two configurations apiCallTimeout and apiCallAttemptTimeout.
                If we’re setting both, should apiCallTimeout be a multiple of apiCallAttempTimeout? 
                For example, we have configured SDK retries to 5. 
                So should apiCallTimeout = 5 * apiCallAttempTimeout? Currently in Hadoop S3A we are setting both these 
                configs to the same value, which I think will mean as soon as an individual request times out, 
                the api call will also timeout and we won’t get any retries?
                """;
        return List.of(question);
    }

}
