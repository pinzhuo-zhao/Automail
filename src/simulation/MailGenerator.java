package simulation;

import java.util.*;

import automail.AbstractMailItem;
import automail.MailItem;
import automail.MailItemWithCharge;
import automail.MailPool;

/**
 * This class generates the mail
 */
public class MailGenerator {

    public final int MAIL_TO_CREATE;
    public final int MAIL_MAX_WEIGHT;
    
    private int mailCreated;

    private final Random random;
    /** This seed is used to make the behaviour deterministic */
    
    private boolean complete;
    private MailPool mailPool;

    private Map<Integer, ArrayList<AbstractMailItem>> allMail;

    /**
     * Constructor for mail generation
     * @param mailToCreate roughly how many mail items to create
     * @param mailMaxWeight limits the maximum weight of the mail
     * @param mailPool where mail items go on arrival
     * @param seed random seed for generating mail
     */
    public MailGenerator(int mailToCreate, int mailMaxWeight, MailPool mailPool, HashMap<Boolean,Integer> seed){
        if(seed.containsKey(true)){
        	this.random = new Random((long) seed.get(true));
        }
        else{
        	this.random = new Random();	
        }
        // Vary arriving mail by +/-20%
        MAIL_TO_CREATE = mailToCreate*4/5 + random.nextInt(mailToCreate*2/5);
        MAIL_MAX_WEIGHT = mailMaxWeight;
        // System.out.println("Num Mail Items: "+MAIL_TO_CREATE);
        mailCreated = 0;
        complete = false;
        allMail = new HashMap<Integer, ArrayList<AbstractMailItem>>();
        this.mailPool = mailPool;
    }

    /**
     * @return a new mail item that needs to be delivered
     */
    private AbstractMailItem generateMail(){
    	MailItem newMailItem;
        int destinationFloor = generateDestinationFloor();
        int arrivalTime = generateArrivalTime();
        int weight = generateWeight();
        
        newMailItem = new MailItem(destinationFloor,arrivalTime,weight);
        return newMailItem;
    }
    private AbstractMailItem generateMail(double threshold, double chargePerUnit, double markupPercentage){
        MailItemWithCharge newMailItem = null;
        int destinationFloor = generateDestinationFloor();
        int arrivalTime = generateArrivalTime();
        int weight = generateWeight();
        try {
            newMailItem = new MailItemWithCharge(destinationFloor,arrivalTime,weight,chargePerUnit,threshold,markupPercentage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newMailItem;
    }

    /**
     * @return a destination floor between the ranges of GROUND_FLOOR to FLOOR
     */
    private int generateDestinationFloor(){
        return Building.LOWEST_FLOOR + random.nextInt(Building.FLOORS);
    }

    /**
     * @return a random weight
     */
    private int generateWeight(){
    	final double mean = 200.0; // grams for normal item
    	final double stddev = 1000.0; // grams
    	double base = random.nextGaussian();
    	if (base < 0) base = -base;
    	int weight = (int) (mean + base * stddev);
        return weight > MAIL_MAX_WEIGHT ? MAIL_MAX_WEIGHT : weight;
    }
    
    /**
     * @return a random arrival time before the last delivery time
     */
    private int generateArrivalTime(){
        return 1 + random.nextInt(Clock.MAIL_RECEVING_LENGTH);
    }

    /**
     * This class initializes all mails and sets their corresponding values,
     * All generated mails will be saved in allMail
     */
    public void generateAllMail(){
        Properties automailProperties = ResourcesUtil.readProperties("automail.properties");
        Properties chargeProperties = ResourcesUtil.readProperties("charge.properties");

        double chargeThreshold = Double.parseDouble(automailProperties.getProperty("ChargeThreshold"));
        double chargePerUnit = Double.parseDouble(chargeProperties.getProperty("ChargePerUnit"));
        double markupPercentage = Double.parseDouble(chargeProperties.getProperty("MarkupPercentage"));
        boolean commercialDisplay = Boolean.parseBoolean(automailProperties.getProperty("ChargeDisplay"));
        while(!complete){
            /** *
             * read from properties file to decide which kind of MailItem to produce
             */
            AbstractMailItem newMail = commercialDisplay ? generateMail(chargeThreshold,chargePerUnit,markupPercentage) : generateMail();
            int timeToDeliver = newMail.getArrivalTime();
            /** Check if key exists for this time **/
            if(allMail.containsKey(timeToDeliver)){
                /** Add to existing array */
                allMail.get(timeToDeliver).add(newMail);
            }
            else{
                /** If the key doesn't exist then set a new key along with the array of MailItems to add during
                 * that time step.
                 */
                ArrayList<AbstractMailItem> newMailList = new ArrayList<>();
                newMailList.add(newMail);
                allMail.put(timeToDeliver,newMailList);
            }
            /** Mark the mail as created */
            mailCreated++;

            /** Once we have satisfied the amount of mail to create, we're done!*/
            if(mailCreated == MAIL_TO_CREATE){
                complete = true;
            }
        }

    }
    
    /**
     * Given the clock time, put the generated mails into the mailPool.
     * So that the robot will can pick up the mails from the pool.
     */
    public void addToMailPool(){
    	// Check if there are any mail to create
        if(this.allMail.containsKey(Clock.Time())){
            for(AbstractMailItem mailItem : allMail.get(Clock.Time())){
                System.out.printf("T: %3d > new addToPool [%s]%n", Clock.Time(), mailItem.toString());
                mailPool.addToPool(mailItem);
            }
        }
    }
    
}
