package learning.temporal.demo.lighthouse;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface LighthouseActivities {
    @ActivityMethod
    void prepareRoom(int roomNumber);

    @ActivityMethod
    void restoreBeacon();
}
