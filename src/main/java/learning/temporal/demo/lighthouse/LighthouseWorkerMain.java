package learning.temporal.demo.lighthouse;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.util.concurrent.TimeUnit;

public class LighthouseWorkerMain {
    public static final String TASK_QUEUE = "lighthouse-quest";

    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(LighthouseWorkflowImpl.class);
        worker.registerActivitiesImplementations(new LighthouseActivitiesImpl());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            factory.shutdown();
            service.shutdown();
        }));

        System.out.println("Lighthouse worker polling task queue '" + TASK_QUEUE + "'");
        factory.start();
        factory.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
}
