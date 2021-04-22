package automail;

import com.unimelb.swen30006.wifimodem.WifiModem;
import simulation.Building;

/**
 * @program: Automail
 * @description:
 * @author: Pinzhuo Zhao, StudentID:1043915
 * @create: 2021-04-05 23:56
 **/
public class MailItemWithCharge extends AbstractMailItem {
    private boolean priority;
    private Charge charge;


    public MailItemWithCharge(int dest_floor, int arrival_time, int weight, double chargePerUnit, double threshold, double markupPercentage) {
        super(dest_floor, arrival_time, weight);
//        try {
//            double preCalculatedServiceFee;
//            do {
//                preCalculatedServiceFee = WifiModem.getInstance(Building.MAILROOM_LOCATION).forwardCallToAPI_LookupPrice(destination_floor);
//            } while (preCalculatedServiceFee <= 0);
//            this.expectedServiceFee = preCalculatedServiceFee;
//            this.activityUnit = ((destination_floor - 1) * 2) * 5;
//            this.activityCost = chargePerUnit * activityUnit;
        this.charge = new Charge(this.destination_floor,chargePerUnit);
        this.priority = (this.charge.getExpectedServiceFee() + this.charge.getActivityCost())*(1 + markupPercentage) >= threshold;

    }

    public boolean isPriority() {
        return priority;
    }

    public double getActivityUnit() {
        return this.charge.getActivityUnit();
    }

    public void setActivityUnit(double activityUnit) {
        this.charge.setActivityUnit(activityUnit);
    }
    public void setActivityCost(double activityCost) {
        this.charge.setActivityCost(activityCost);
    }

    public double getActivityCost() {
        return this.charge.getActivityCost();
    }

    public double getServiceFee() {
        return this.charge.getServiceFee();
    }

    public void setServiceFee(double serviceFee) {
        this.charge.setServiceFee(serviceFee);
    }

    public void setTotalCharge(double totalCharge) {
        this.charge.setTotalCharge(totalCharge);
    }

    @Override
    public String toString() {
        return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d", id, arrival_time, destination_floor, weight);

    }
    public String printCharge() {
        return String.format("Mail Item:: ID: %6s | Arrival: %4d | Destination: %2d | Weight: %4d | Charge: %4f | Cost: %4f | Fee: %4f | Activity: %4f", id, arrival_time, destination_floor, weight, charge.getTotalCharge(), charge.getActivityCost(), charge.getServiceFee(), charge.getActivityUnit());
    }


}
