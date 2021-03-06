package simulation;

import automail.AbstractMailItem;
import automail.MailItemWithCharge;
import exceptions.ExcessiveDeliveryException;
import exceptions.ItemTooHeavyException;
import exceptions.MailAlreadyDeliveredException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.unimelb.swen30006.wifimodem.WifiModem;

import automail.Automail;
import automail.MailPool;

/**
 * This class simulates the behaviour of AutoMail
 */
public class Simulation {
	private static int NUM_ROBOTS;
	private static double CHARGE_THRESHOLD;
	private static boolean CHARGE_DISPLAY;
	
    /** Constant for the mail generator */
    private static int MAIL_TO_CREATE;
    private static int MAIL_MAX_WEIGHT;
    
    private static ArrayList<AbstractMailItem> MAIL_DELIVERED;
    private static double total_delay = 0;
    private static WifiModem wModem = null;
    private static double total_items = 0;
    private static double total_activities_unit= 0;
    private static double total_activities_cost = 0;
    private static double total_service_cost = 0;
    private static int total_API_calls = 0;
    private static int total_failed_calls = 0;
    private static int total_successful_calls = 0;


    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
    	
    	/** Load properties for simulation based on either default or a properties file.**/
    	Properties automailProperties = setUpProperties();
    	
    	//An array list to record mails that have been delivered
        MAIL_DELIVERED = new ArrayList<automail.AbstractMailItem>();
                
        /** This code section below is to save a random seed for generating mails.
         * If a program argument is entered, the first argument will be a random seed.
         * If not a random seed will be from a properties file. 
         * Otherwise, no a random seed. */
        
        /** Used to see whether a seed is initialized or not */
        HashMap<Boolean, Integer> seedMap = new HashMap<>();
        if (args.length == 0 ) { // No arg
        	String seedProp = automailProperties.getProperty("Seed");
        	if (seedProp == null) { // and no property
        		seedMap.put(false, 0); // so randomise
        	} else { // Use property seed
        		seedMap.put(true, Integer.parseInt(seedProp));
        	}
        } else { // Use arg seed - overrides property
        	seedMap.put(true, Integer.parseInt(args[0]));
        }
        Integer seed = seedMap.get(true);
        System.out.println("#A Random Seed: " + (seed == null ? "null" : seed.toString()));
        
        // Install the modem & turn on the modem
        try {
        	System.out.println("Setting up Wifi Modem");
        	wModem = WifiModem.getInstance(Building.MAILROOM_LOCATION);
			System.out.println(wModem.Turnon());
		} catch (Exception mException) {
			mException.printStackTrace();
		}
        
        /**
         * This code section is for running a simulation
         */
        /* Instantiate MailPool and Automail */
     	MailPool mailPool = new MailPool(NUM_ROBOTS);
     	IMailDelivery delivery = null;
        delivery = (CHARGE_DISPLAY) ? new ReportDeliveryWithCharge() : new ReportDelivery();
        Automail automail = new Automail(mailPool, delivery, NUM_ROBOTS);
        MailGenerator mailGenerator = new MailGenerator(MAIL_TO_CREATE, MAIL_MAX_WEIGHT, mailPool, seedMap);
        
        /** Generate all the mails */
        mailGenerator.generateAllMail();
        while(MAIL_DELIVERED.size() != mailGenerator.MAIL_TO_CREATE) {
        	// System.out.printf("Delivered: %4d; Created: %4d%n", MAIL_DELIVERED.size(), mailGenerator.MAIL_TO_CREATE);
            mailGenerator.addToMailPool();
            try {
                automail.mailPool.loadItemsToRobot();
				for (int i=0; i < NUM_ROBOTS; i++) {
					automail.robots[i].operate();
				}
			} catch (ExcessiveDeliveryException|ItemTooHeavyException e) {
				e.printStackTrace();
				System.out.println("Simulation unable to complete.");
				System.exit(0);
			}
            Clock.Tick();
        }

        AbstractStats stat = null;
        stat = (CHARGE_DISPLAY) ? new StatsWithCharge(total_delay,total_items,total_activities_unit,total_activities_cost,total_service_cost,total_API_calls,total_failed_calls,total_successful_calls) : new DefaultStats(total_delay);
        stat.printResults();
        System.out.println(wModem.Turnoff());
    }
    
    static private Properties setUpProperties() throws IOException {
    	Properties automailProperties = new Properties();
		// Default properties
    	automailProperties.setProperty("Robots", "Standard");
    	automailProperties.setProperty("Floors", "10");
    	automailProperties.setProperty("Mail_to_Create", "80");
    	automailProperties.setProperty("ChargeThreshold", "0");
    	automailProperties.setProperty("ChargeDisplay", "false");
    	
    	// Read properties
		FileReader inStream = null;
		try {
			inStream = new FileReader("automail.properties");
			automailProperties.load(inStream);
		} finally {
			 if (inStream != null) {
	                inStream.close();
	            }
		}
		
		// Floors
		Building.FLOORS = Integer.parseInt(automailProperties.getProperty("Floors"));
        System.out.println("#Floors: " + Building.FLOORS);
		// Mail_to_Create
		MAIL_TO_CREATE = Integer.parseInt(automailProperties.getProperty("Mail_to_Create"));
        System.out.println("#Created mails: " + MAIL_TO_CREATE);
        // Mail_to_Create
     	MAIL_MAX_WEIGHT = Integer.parseInt(automailProperties.getProperty("Mail_Max_Weight"));
        System.out.println("#Maximum weight: " + MAIL_MAX_WEIGHT);
		// Last_Delivery_Time
		Clock.MAIL_RECEVING_LENGTH = Integer.parseInt(automailProperties.getProperty("Mail_Receving_Length"));
        System.out.println("#Mail receiving length: " + Clock.MAIL_RECEVING_LENGTH);
		// Robots
		NUM_ROBOTS = Integer.parseInt(automailProperties.getProperty("Robots"));
		System.out.print("#Robots: "); System.out.println(NUM_ROBOTS);
		assert(NUM_ROBOTS > 0);
		// Charge Threshold 
		CHARGE_THRESHOLD = Double.parseDouble(automailProperties.getProperty("ChargeThreshold"));
		System.out.println("#Charge Threshold: " + CHARGE_THRESHOLD);
		// Charge Display
		CHARGE_DISPLAY = Boolean.parseBoolean(automailProperties.getProperty("ChargeDisplay"));
		System.out.println("#Charge Display: " + CHARGE_DISPLAY);
		
		return automailProperties;
    }
    
    static class ReportDelivery implements IMailDelivery {
    	
    	/** Confirm the delivery and calculate the total score */
    	public void deliver(AbstractMailItem deliveryItem){
    		if(!MAIL_DELIVERED.contains(deliveryItem)){
    			MAIL_DELIVERED.add(deliveryItem);
                System.out.printf("T: %3d > Delivered(%4d) [%s]%n", Clock.Time(), MAIL_DELIVERED.size(), deliveryItem.toString());
    			// Calculate delivery score
    			total_delay += calculateDeliveryDelay(deliveryItem);
    			total_items += 1;

    		}
    		else{
    			try {
    				throw new MailAlreadyDeliveredException();
    			} catch (MailAlreadyDeliveredException e) {
    				e.printStackTrace();
    			}
    		}
    	}

    }
    static class ReportDeliveryWithCharge implements IMailDelivery{
        @Override
        public void deliver(AbstractMailItem deliveryItem) {
            MailItemWithCharge deliveryItemWithCharge = null;
            if (deliveryItem instanceof MailItemWithCharge){
                 deliveryItemWithCharge = (MailItemWithCharge) deliveryItem;
            }
            Properties automailProperties = ResourcesUtil.readProperties("automail.properties");

            double markupPercentage = 0;
            double chargePerUnit = 0;
            try {
                Properties chargeProperties = ResourcesUtil.readProperties("charge.properties");
                markupPercentage = Double.parseDouble(chargeProperties.getProperty("MarkupPercentage"));
                chargePerUnit = Double.parseDouble(chargeProperties.getProperty("ChargePerUnit"));
            }
                catch (NullPointerException e){
                    chargePerUnit = 0.224;
                    markupPercentage = 0.059;
                }
            try {
                double finalServiceFee;
                do {
                finalServiceFee = WifiModem.getInstance(Building.MAILROOM_LOCATION).forwardCallToAPI_LookupPrice(deliveryItem.getDestFloor());
                //increment the MailItem's accumulated activity unit by 0.1 everytime it performs a lookup
                //however, as the lookup fee will only be charged for once, the increment won't affect the final charge amount
                deliveryItemWithCharge.setActivityUnit(deliveryItemWithCharge.getActivityUnit()+0.1);
                deliveryItemWithCharge.setActivityCost(deliveryItemWithCharge.getActivityUnit()*chargePerUnit);
                total_API_calls += 1;
                if (finalServiceFee >= 0){
                    total_successful_calls += 1;
                }
                else {
                    total_failed_calls += 1;
                }
                } while (finalServiceFee <= 0);
                deliveryItemWithCharge.setServiceFee(finalServiceFee);



                double totalCharge = (deliveryItemWithCharge.getServiceFee() + deliveryItemWithCharge.getActivityCost()) * (1 + markupPercentage);
                deliveryItemWithCharge.setTotalCharge(totalCharge);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!MAIL_DELIVERED.contains(deliveryItemWithCharge)){
                MAIL_DELIVERED.add(deliveryItemWithCharge);
                System.out.printf("T: %3d > Delivered(%4d) [%s]%n", Clock.Time(), MAIL_DELIVERED.size(), deliveryItemWithCharge.printCharge());
                // Calculate delivery score
                total_delay += calculateDeliveryDelay(deliveryItemWithCharge);
                total_items += 1;
                total_activities_unit += deliveryItemWithCharge.getActivityUnit();
                total_activities_cost += deliveryItemWithCharge.getActivityCost();
                total_service_cost += deliveryItemWithCharge.getServiceFee();
            }
            else{
                try {
                    throw new MailAlreadyDeliveredException();
                } catch (MailAlreadyDeliveredException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static double calculateDeliveryDelay(AbstractMailItem deliveryItem) {
    	// Penalty for longer delivery times
    	final double penalty = 1.2;
    	double priority_weight = 0;
        // Take (delivery time - arrivalTime)**penalty * (1+sqrt(priority_weight))
        return Math.pow(Clock.Time() - deliveryItem.getArrivalTime(),penalty)*(1+Math.sqrt(priority_weight));
    }

   /* public static void printResults(){
        System.out.println("T: "+Clock.Time()+" | Simulation complete!");
        System.out.println("Final Delivery time: "+Clock.Time());
        System.out.printf("Delay: %.2f%n", total_delay);
    }*/
}
