package software.amazon.awssdk;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.util.List;

public class DdbKMSQuestion extends StaticConversation {
    public DdbKMSQuestion(String knowledgeBaseId, BedrockAgentRuntimeAsyncClient bedrockAgentClient) {
        super(knowledgeBaseId, bedrockAgentClient);
    }

    @Override
    public List<String> prompts() {
        String question = "Hi. Can you point me to a java example where one does client side encryption with the DDB Enhanced Client with a KMS key from a different account than the one hosting the table?";
        return List.of(
                "Play the role of a software engineer with expertise on the AWS SDK for Java for the whole discussion.\n" +
                "Describe what is the Dynamo DB (DDB) Enhanced Client and give basic use case examples with the AWS SDK for Java v2\n" +
                        "Give a high level description of what the code does.",
                "Give a general description of the Key Management Solutions (KMS) AWS service with code example of a basic use case.+\n" +
                        "Give a high level description of what the code does.",
                STR."""
                Using the context of the current discussion and examples from AWS KMS and DDB Enhanced Client, answer the following question:
                \{question}
                """
        );
    }
}
