package simulation;

/**
 * @program: Automail
 * @description:
 * @author: Pinzhuo Zhao, StudentID:1043915
 * @create: 2021-04-07 20:56
 **/
public class DefaultStats extends AbstractStats {

    @Override
    public void printResults() {
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");
        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Delay: %.2f%n", total_delay);
    }

    public DefaultStats(double total_delay) {
        super(total_delay);
    }
}
