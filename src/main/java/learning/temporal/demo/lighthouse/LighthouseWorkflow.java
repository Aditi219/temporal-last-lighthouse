package learning.temporal.demo.lighthouse;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface LighthouseWorkflow {
    @WorkflowMethod
    String play(String playerName);

    @SignalMethod
    void submitAnswer(String answer);

    @SignalMethod
    void requestHint();

    @QueryMethod
    String status();

    @QueryMethod
    long roomDeadlineEpochMillis();
}
