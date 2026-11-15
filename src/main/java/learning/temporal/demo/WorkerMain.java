package learning.temporal.demo;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.util.concurrent.TimeUnit;

public class WorkerMain {
    public static final String TASK_QUEUE = "orders-java";

    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(OrderWorkflowImpl.class);
        worker.registerActivitiesImplementations(new OrderActivitiesImpl());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            factory.shutdown();
            service.shutdown();
        }));

        System.out.println("Worker polling task queue '" + TASK_QUEUE + "'");
        factory.start();
        factory.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
}
