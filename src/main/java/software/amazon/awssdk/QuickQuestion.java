package software.amazon.awssdk;

import java.util.List;

import static software.amazon.awssdk.Constant.BEDROCK_AGENT_RUNTIME_CLIENT;
import static software.amazon.awssdk.Constant.KB_ID;

public class QuickQuestion extends StaticConversation {
    private String question;
    public QuickQuestion(String question) {
        super(KB_ID, BEDROCK_AGENT_RUNTIME_CLIENT);
        this.question = question;
    }

    @Override
    public List<String> prompts() {
        return List.of(question);
    }

    public static void main(String[] args) {
        new QuickQuestion(
                """
                Hi, I'm currently working on migrating our team's codebase from sdkv1 to sdkv2.
                I am having trouble finding the sdkv2 version for these:
                com.amazonaws.auth.policy.actions.KMSActions;
                com.amazonaws.auth.policy.actions.S3Actions;
                com.amazonaws.auth.policy.conditions.S3ConditionFactory;
                Wondering if anyone was able to find the v2 versions, or if there are any workarounds!
                """
        ).run();
    }
}
