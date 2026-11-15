package learning.temporal.demo.lighthouse;

import io.temporal.activity.Activity;

public class LighthouseActivitiesImpl implements LighthouseActivities {
    @Override
    public void prepareRoom(int roomNumber) {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        System.out.println("Preparing room " + roomNumber + " (attempt " + attempt + ")");
        if (attempt == 1) {
            throw new RuntimeException("simulated storm damage while powering the room");
        }
        System.out.println("Room " + roomNumber + " is ready.");
    }

    @Override
    public void restoreBeacon() {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        System.out.println("Restoring the beacon (attempt " + attempt + ")");
        if (attempt == 1) {
            throw new RuntimeException("simulated generator failure");
        }
        System.out.println("The beacon is shining again!");
    }
}
