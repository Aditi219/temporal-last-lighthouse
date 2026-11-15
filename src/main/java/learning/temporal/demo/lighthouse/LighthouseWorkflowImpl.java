package learning.temporal.demo.lighthouse;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class LighthouseWorkflowImpl implements LighthouseWorkflow {
    private static final List<String> RIDDLES = List.of(
            "Room 1 — The keeper left a note: I bend the sun into a bright path. What am I?",
            "Room 2 — Three teeth, one turn, and the sea gate opens. What do you need?",
            "Room 3 — The storm is pushing the ship east. Which way must the beacon face?");
    private static final List<String> ANSWERS = List.of("lens", "key", "west");
    private static final List<String> HINTS = List.of(
            "The lighthouse beam passes through it.",
            "It is small, made of brass, and opens a lock.",
            "Point the light against the direction the ship is drifting.");

    private final LighthouseActivities activities = Workflow.newActivityStub(
            LighthouseActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setMaximumInterval(Duration.ofSeconds(3))
                            .setMaximumAttempts(3)
                            .build())
                    .build());

    private String latestAnswer;
    private boolean hintRequested;
    private String currentStatus = "The lighthouse is waiting for an expedition.";
    private int hintsUsed;
    private int wrongAnswers;
    private long roomDeadlineEpochMillis;

    @Override
    public String play(String playerName) {
        currentStatus = "Welcome, " + playerName + ". The storm has cut power to the lighthouse.";

        for (int index = 0; index < RIDDLES.size(); index++) {
            int roomNumber = index + 1;
            activities.prepareRoom(roomNumber);
            currentStatus = RIDDLES.get(index);
            roomDeadlineEpochMillis = Workflow.currentTimeMillis() + Duration.ofMinutes(10).toMillis();

            while (true) {
                boolean receivedAction = Workflow.await(
                        Duration.ofMinutes(10),
                        () -> latestAnswer != null || hintRequested);

                if (!receivedAction) {
                    currentStatus = "The storm won. The beacon is still dark. Start a new expedition to try again.";
                    roomDeadlineEpochMillis = 0;
                    return currentStatus;
                }

                if (hintRequested) {
                    hintRequested = false;
                    hintsUsed++;
                    currentStatus = "Hint: " + HINTS.get(index) + "\n\n" + RIDDLES.get(index);
                    continue;
                }

                String answer = latestAnswer;
                latestAnswer = null;
                if (normalize(answer).equals(ANSWERS.get(index))) {
                    currentStatus = "Correct! Room " + roomNumber + " is secure.";
                    roomDeadlineEpochMillis = 0;
                    break;
                }

                wrongAnswers++;
                currentStatus = "That answer did not unlock the room. Try again, or ask for a hint.\n\n"
                        + RIDDLES.get(index);
            }
        }

        currentStatus = "All rooms solved. Bringing the generator online...";
        activities.restoreBeacon();
        currentStatus = "You did it, " + playerName + "! The Last Lighthouse is shining again. "
                + "Hints used: " + hintsUsed + ", wrong answers: " + wrongAnswers + ".";
        roomDeadlineEpochMillis = 0;
        return currentStatus;
    }

    @Override
    public void submitAnswer(String answer) {
        latestAnswer = answer;
    }

    @Override
    public void requestHint() {
        hintRequested = true;
    }

    @Override
    public String status() {
        return currentStatus;
    }

    @Override
    public long roomDeadlineEpochMillis() {
        return roomDeadlineEpochMillis;
    }

    private static String normalize(String answer) {
        return answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT);
    }
}
