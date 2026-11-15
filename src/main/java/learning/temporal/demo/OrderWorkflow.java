package learning.temporal.demo;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import io.temporal.workflow.SignalMethod;

@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String run(String orderId);

    @SignalMethod
    void approve();
}
