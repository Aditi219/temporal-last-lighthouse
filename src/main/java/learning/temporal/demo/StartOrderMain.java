package learning.temporal.demo;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.UUID;

public class StartOrderMain {
    public static void main(String[] args) {
        String workflowId = args.length > 0 ? args[0] : "order-demo-" + UUID.randomUUID();
        String orderId = args.length > 1 ? args[1] : workflowId;

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        OrderWorkflow workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(WorkerMain.TASK_QUEUE)
                        .build());

        WorkflowClient.start(workflow::run, orderId);
        System.out.println("Started workflow " + workflowId + " for order " + orderId);
        System.out.println("It should retry inventory, then wait for approval.");
        service.shutdown();
    }
}
