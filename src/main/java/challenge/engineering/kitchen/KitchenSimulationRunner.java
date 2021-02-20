package challenge.engineering.kitchen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import challenge.engineering.kitchen.config.KitchenSimulationConfig;
import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.exception.KitchenSimulationRuntimeException;
import challenge.engineering.kitchen.handling.StatManager;
import challenge.engineering.kitchen.handling.StatManagerImpl;
import challenge.engineering.kitchen.model.KitchenOrder;
import challenge.engineering.kitchen.simulation.KitchenSimulation;
import challenge.engineering.kitchen.simulation.KitchenSimulationImpl;

/**
 * 
 * KitchenSimulationRunner loads up the kitchen simulation configuration json file,  runs the named simulations listed there,
 * and then reports stats for each strategy. 
 * <p>
 * Usage: java -jar <path-to-configuration-file>
 *
 */
public class KitchenSimulationRunner {
    
    static Logger log = LoggerFactory.getLogger(KitchenSimulationRunner.class);

	public static void main(String[] args) throws KitchenSimulationException {
	   
	    long startTime = System.currentTimeMillis();
		
	    // a configuration file is required with at least one strategy included 
	    // (with the minimum required properties: 'name', 'courierMatchedToOrder') 
		if (ArrayUtils.isEmpty(args) || args.length != 1) {
            throw new KitchenSimulationException(
                    "Incorrect parameters: expected: java -jar KitchenSimulation.jar <config.json>");
        }
		
		String configFilename = args[0];
		KitchenSimulationConfig config = KitchenSimulationConfig.load(configFilename);
		if (CollectionUtils.isEmpty(config.getStrategies())) {
			throw new KitchenSimulationException( "At least one strategy must be included in config file: " + configFilename);
		}
		
		List<KitchenOrder> kitchenOrders = ListUtils.unmodifiableList(KitchenOrder.load(config.getOrderJsonFile()));
		if (config.getOrdersPerTrial() > kitchenOrders.size()) {
		    throw new KitchenSimulationException("In the configuration file, ordersPerTrial must be less than or equal to the number of orders in orderJsonFile");
		}
		
		Set<String> set = new HashSet<>();
		for (KitchenOrder ko : kitchenOrders) {
		    if (set.contains(ko.getId())) {
		        throw new KitchenSimulationException("Each order in orderJsonFile must have a unique id: found 2 orders with this id: " + ko.getId());
		    } else {
		        set.add(ko.getId());
		    }
		}
		
		StatManager statManager = new StatManagerImpl(config.isUsingFullStatistics());
		// if 0, then use all orders found in orderJsonFile
		int ordersPerTrial = (config.getOrdersPerTrial() == 0) ? kitchenOrders.size() : config.getOrdersPerTrial();
		
		List<KitchenSimulation> simulations = new ArrayList<>();
	    IntStream.rangeClosed(1, config.getNumTrials())
        .forEach(i -> {
            List<KitchenOrder> randomlyOrdered = new ArrayList<>(kitchenOrders);
            Collections.shuffle(randomlyOrdered, ThreadLocalRandom.current());
            List<KitchenOrder> ordersUsedForTrial = (ordersPerTrial < randomlyOrdered.size()) ? randomlyOrdered.subList(0, config.getOrdersPerTrial()) : randomlyOrdered;
            config.getStrategies().stream().forEach(strategy-> {
                if (!strategy.valid()) {
                    throw new KitchenSimulationRuntimeException("All strategies require a nonblank 'name' and a 'courierMatchedToOrder' setting");
                }
	            log.info("Starting {} strategy trial #{} out of {}...", strategy.getName(), i, config.getNumTrials());
    		    KitchenSimulation kitchen = new KitchenSimulationImpl(statManager, ordersUsedForTrial, strategy, i);
    		    kitchen.runSimulation();
    		    simulations.add(kitchen);
    		    if (config.isBlocking()) {
    		        kitchen.waitUntilComplete();
    		    }
            });
        });
		
		while (simulations.size() > 0) {
		    KitchenSimulation simulation = simulations.get(0);
		    simulation.waitUntilComplete();
		    log.info("{} has completed...", simulation.getFullName());
		    simulations.remove(0);
		}
		
		statManager.reportResults(startTime);	
	}
}
