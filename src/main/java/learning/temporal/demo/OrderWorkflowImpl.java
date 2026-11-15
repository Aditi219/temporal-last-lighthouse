package learning.temporal.demo;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class OrderWorkflowImpl implements OrderWorkflow {
    private boolean approved;

    private final OrderActivities activities = Workflow.newActivityStub(
            OrderActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumInterval(Duration.ofSeconds(2))
                            .setMaximumAttempts(3)
                            .build())
                    .build());

    @Override
    public String run(String orderId) {
        String reservation = activities.reserveItem(orderId);

        Workflow.await(() -> approved);

        String payment = activities.chargePayment(orderId);
        return "Order complete: " + reservation + ", " + payment;
    }

    @Override
    public void approve() {
        approved = true;
    }
}
