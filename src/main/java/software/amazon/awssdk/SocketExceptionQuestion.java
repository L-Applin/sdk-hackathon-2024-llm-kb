package software.amazon.awssdk;

import java.util.List;

import static software.amazon.awssdk.Constant.BEDROCK_AGENT_RUNTIME_CLIENT;
import static software.amazon.awssdk.Constant.KB_ID;

public class SocketExceptionQuestion extends StaticConversation {
    public SocketExceptionQuestion() {
        super(KB_ID, BEDROCK_AGENT_RUNTIME_CLIENT);
    }

    @Override
    public List<String> prompts() {
        String question =
                """
                Hey team, we got many SocketException: connection reset errors
                I'm curious are those SocketException are getting retried by sdk client
                (I know that client configuration could set automatic retry times but are they cover the scenario SocketException: connection reset)?
                How could we know that? Do we have any documentation for that?
                """;
        return List.of(
                STR."""
                    Play the role of a software engineer with expertise on the AWS SDK for Java v2.
                    Answer the following question, giving code example if necessary.
                    If you give a code example, describe briefly what it does.

                    Here is the question:
                    \{question}
                    """
        );
    }

    public static void main(String[] args) {
        new SocketExceptionQuestion().run();
    }
}
