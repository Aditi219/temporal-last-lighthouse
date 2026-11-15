package learning.temporal.demo.lighthouse;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

import java.util.UUID;

public class StartLighthouseMain {
    public static void main(String[] args) {
        String workflowId = args.length > 0 ? args[0] : "lighthouse-" + UUID.randomUUID();
        String playerName = args.length > 1 ? args[1] : "Keeper";

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        LighthouseWorkflow game = client.newWorkflowStub(
                LighthouseWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(LighthouseWorkerMain.TASK_QUEUE)
                        .build());

        WorkflowClient.start(game::play, playerName);
        System.out.println("Expedition started: " + workflowId);
        System.out.println("Check the workflow status, then send an answer or ask for a hint.");
        service.shutdown();
    }
}
