package learning.temporal.demo;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class ApproveOrderMain {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ApproveOrderMain <workflow-id>");
            System.exit(1);
        }

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        OrderWorkflow workflow = client.newWorkflowStub(OrderWorkflow.class, args[0]);
        workflow.approve();
        String result = WorkflowStub.fromTyped(workflow).getResult(String.class);
        System.out.println("Workflow result: " + result);
        service.shutdown();
    }
}
