package simulation;

/**
 * @program: Automail
 * @description:Abstract class for statistical information
 * @author: Pinzhuo Zhao, StudentID:1043915
 * @create: 2021-04-07 20:42
 **/
public abstract class AbstractStats {
    protected double total_delay = 0;


    public abstract void printResults();

    public AbstractStats() {
    }

    public AbstractStats(double total_delay) {
        this.total_delay = total_delay;
    }
}
