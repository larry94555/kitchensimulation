package challenge.engineering.kitchen.config;

import java.io.File;
import java.io.IOException;

import java.util.List;

import org.apache.commons.lang3.Range;

import com.fasterxml.jackson.databind.ObjectMapper;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.model.Strategy;

/**
 * 
 * KitchenSimulationConfig is a object-to-configuration mapping.
 * <p>
 * The configuration file lists the strategies that will be evaluated by the simulation,
 * the parameters of each strategy, and the challenge parameters
 * 
 */
public class KitchenSimulationConfig {
	
	private final static ObjectMapper mapper = new ObjectMapper();
	
	public static final int MIN_NUM_TRIALS = 1;
    public static final int MAX_NUM_TRIALS = 700;
    public static final int DEFAULT_NUM_TRIALS = 1;
    
    public static final int MIN_ORDERS_PER_TRIAL = 1;
	
	private String orderJsonFile;
	private int numTrials;
	private boolean usingFullStatistics;
	private boolean blocking;
	private int ordersPerTrial;
	private List<Strategy> strategies;
	
	public KitchenSimulationConfig() {
	    // set defaults
	    numTrials = DEFAULT_NUM_TRIALS;
	    blocking = true;
	    ordersPerTrial = 0;
	    usingFullStatistics = false;
	}
	
	public boolean isUsingFullStatistics() {
        return usingFullStatistics;
    }

    public void setUsingFullStatistics(boolean usingFullStatistics) {
        this.usingFullStatistics = usingFullStatistics;
    }

    public int getOrdersPerTrial() {
        return ordersPerTrial;
    }

    public void setOrdersPerTrial(int ordersPerTrial) throws KitchenSimulationException {
        if (ordersPerTrial < 0) {
            throw new KitchenSimulationException("ordersPerTrial must be greater or equal to 1");
        }
        this.ordersPerTrial = ordersPerTrial;
    }
    
	public String getOrderJsonFile() {
		return orderJsonFile;
	}
	public void setOrderJsonFile(String orderJsonFile) {
		this.orderJsonFile = orderJsonFile;
	}
	
	public List<Strategy> getStrategies() {
		return strategies;
	}
	
	public void setStrategies(List<Strategy> strategies) {
		this.strategies = strategies;
	}
	
	public int getNumTrials() {
        return numTrials;
    }
	
    public void setNumTrials(int numTrials) throws KitchenSimulationException {
        Range<Integer> allowedRange = Range.between(MIN_NUM_TRIALS, MAX_NUM_TRIALS);
        if (!allowedRange.contains(numTrials)) {
            throw new KitchenSimulationException("numTrials must be in the range: " + allowedRange);
        }
        this.numTrials = numTrials;
    }
    
    public boolean isBlocking() {
        return blocking;
    }
    
    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    /**
     * Load the configuration from a json file
     * <p>
     * @param pathToFile  the path including the file name
     * @return  an a KitchenSimulationConfig with the values specified in the json file
     * @throws KitchenSimulationException  if the json file specified is invalid
     */
    public static KitchenSimulationConfig load(String pathToFile) throws KitchenSimulationException {
		try {
			return mapper.readValue(new File(pathToFile), KitchenSimulationConfig.class);
		} catch(IOException e) {
			throw new KitchenSimulationException("Exception reading jsonFile: " + pathToFile, e);
		}
	}
}
