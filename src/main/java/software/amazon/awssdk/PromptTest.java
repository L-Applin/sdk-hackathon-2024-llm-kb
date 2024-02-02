package software.amazon.awssdk;

public class PromptTest {
    public static void main(String[] args) throws Exception {
        StaticConversation conversation = new SocketExceptionQuestion();
        conversation.run();
    }
}
