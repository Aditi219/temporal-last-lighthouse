package learning.temporal.demo.lighthouse;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class LighthouseActionMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            usage();
            System.exit(1);
        }

        String action = args[0].toLowerCase();
        String workflowId = args[1];
        if (action.equals("answer") && args.length < 3) {
            usage();
            System.exit(1);
        }

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        LighthouseWorkflow game = client.newWorkflowStub(LighthouseWorkflow.class, workflowId);

        switch (action) {
            case "status" -> System.out.println(game.status());
            case "hint" -> {
                game.requestHint();
                System.out.println("Hint requested. Check status in a moment.");
            }
            case "answer" -> {
                game.submitAnswer(String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)));
                System.out.println("Answer submitted. Check status in a moment.");
            }
            default -> {
                usage();
                System.exit(1);
            }
        }
        service.shutdown();
    }

    private static void usage() {
        System.err.println("Usage: LighthouseActionMain status <workflow-id>");
        System.err.println("   or: LighthouseActionMain hint <workflow-id>");
        System.err.println("   or: LighthouseActionMain answer <workflow-id> <answer>");
    }
}
