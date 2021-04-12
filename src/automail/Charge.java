package automail;

import com.unimelb.swen30006.wifimodem.WifiModem;
import simulation.Building;

/**
 * @program: Automail
 * @description: work as a field of MailItemWithCharge class, to increase cohesion of MailItemWithCharge class and
 * reduce coupling since future changes on charge functions could be changed without affecting MailItemWithCharge class
 * @author: Pinzhuo Zhao, StudentID:1043915
 * @create: 2021-04-12 15:25
 **/
public class Charge {
    private double expectedServiceFee;
    private double serviceFee;
    private double activityCost;
    private double totalCharge;
    private double activityUnit;

    public Charge(int destination_floor, double chargePerUnit) {
        try {
            double preCalculatedServiceFee;
            do {
                preCalculatedServiceFee = WifiModem.getInstance(Building.MAILROOM_LOCATION).forwardCallToAPI_LookupPrice(destination_floor);
            } while (preCalculatedServiceFee <= 0);
            this.expectedServiceFee = preCalculatedServiceFee;
            this.activityUnit = ((destination_floor - 1) * 2) * 5;
            this.activityCost = chargePerUnit * activityUnit;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getExpectedServiceFee() {
        return expectedServiceFee;
    }

    public void setExpectedServiceFee(double expectedServiceFee) {
        this.expectedServiceFee = expectedServiceFee;
    }

    public double getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(double serviceFee) {
        this.serviceFee = serviceFee;
    }

    public double getActivityCost() {
        return activityCost;
    }

    public void setActivityCost(double activityCost) {
        this.activityCost = activityCost;
    }

    public double getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(double totalCharge) {
        this.totalCharge = totalCharge;
    }

    public double getActivityUnit() {
        return activityUnit;
    }

    public void setActivityUnit(double activityUnit) {
        this.activityUnit = activityUnit;
    }
}
