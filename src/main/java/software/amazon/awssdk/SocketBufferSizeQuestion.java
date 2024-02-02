package software.amazon.awssdk;

import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.time.Duration;
import java.util.List;

import static software.amazon.awssdk.Constant.KB_ID;

public class SocketBufferSizeQuestion extends StaticConversation {
    public SocketBufferSizeQuestion(String knowledgeBaseId, BedrockAgentRuntimeAsyncClient bedrockAgentClient) {
        super(knowledgeBaseId, bedrockAgentClient);
    }

    @Override
    public List<String> prompts() {
        String question = "How to map setSocketBufferSizeHints in ClientConfiguration (sdk v1) to sdk v2?";
        return List.of(
                STR."""
               There are two versions of the AWS SDK for Java: v1 (1.x) and v2 (2.x).
               Version 1 will reach end-of-life on December 31, 2025 so customers need help migrating to v2.

                Answer the following customer question:
                \{question}
                """
        );
    }

}
