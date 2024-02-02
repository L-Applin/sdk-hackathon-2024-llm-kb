package software.amazon.awssdk;

import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;

import java.time.Duration;

public class Constant {
    public static final String KB_ID = "PGQ6MCSNZS";
    public static final String CLAUDE_V2_ID = "anthropic.claude-v2:1";
    public static final SdkAsyncHttpClient NETTY = NettyNioAsyncHttpClient.builder()
            .readTimeout(Duration.ofMinutes(10))
            .build();
    public static final BedrockAgentRuntimeAsyncClient BEDROCK_AGENT_RUNTIME_CLIENT =
            BedrockAgentRuntimeAsyncClient.builder()
                    .region(Region.US_WEST_2)
                    .httpClient(NETTY)
                    .build();
    public static final BedrockRuntimeAsyncClient BEDROCK_RUNTIME_CLIENT = BedrockRuntimeAsyncClient.builder()
            .region(Region.US_EAST_1)
            .httpClient(NETTY)
            .build();



}
