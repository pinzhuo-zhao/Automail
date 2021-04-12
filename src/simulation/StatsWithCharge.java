package simulation;

/**
 * @program: Automail
 * @description:
 * @author: Pinzhuo Zhao, StudentID:1043915
 * @create: 2021-04-07 20:46
 **/
public class StatsWithCharge extends AbstractStats {
    private double total_items;
    private double total_activities_unit;
    private double total_activities_cost;
    private double total_service_cost;

    public StatsWithCharge(double total_delay, double total_items, double total_activities_unit, double total_activities_cost, double total_service_cost) {
        super(total_delay);
        this.total_items = total_items;
        this.total_activities_unit = total_activities_unit;
        this.total_activities_cost = total_activities_cost;
        this.total_service_cost = total_service_cost;
    }

    @Override
    public void printResults() {
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");
        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Delay: %.2f%n", total_delay);
        System.out.println("Total number of items delivered: " + total_items);
        System.out.println("Total billable activity: " + total_activities_unit);
        System.out.println("Total activity cost: " + total_activities_cost);
        System.out.println("Total service cost: " + total_service_cost);


    }
}
