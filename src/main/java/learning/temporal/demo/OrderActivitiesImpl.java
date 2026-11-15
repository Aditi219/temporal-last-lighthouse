package learning.temporal.demo;

import io.temporal.activity.Activity;

public class OrderActivitiesImpl implements OrderActivities {
    @Override
    public String reserveItem(String orderId) {
        int attempt = Activity.getExecutionContext().getInfo().getAttempt();
        System.out.println("reserveItem attempt " + attempt + " for " + orderId);
        if (attempt == 1) {
            throw new RuntimeException("simulated temporary inventory outage");
        }
        return "reservation-" + orderId;
    }

    @Override
    public String chargePayment(String orderId) {
        System.out.println("Charging payment for " + orderId);
        return "payment-" + orderId;
    }
}
