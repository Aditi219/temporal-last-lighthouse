package learning.temporal.demo;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderActivities {
    @ActivityMethod
    String reserveItem(String orderId);

    @ActivityMethod
    String chargePayment(String orderId);
}
