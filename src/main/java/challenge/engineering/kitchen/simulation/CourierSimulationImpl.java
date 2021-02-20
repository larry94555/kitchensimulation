package challenge.engineering.kitchen.simulation;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.exception.KitchenSimulationRuntimeException;
import challenge.engineering.kitchen.model.KitchenOrder;

/**
 * The CourierSimulation simulates a courier which receives dispatch requests and then simulates an 
 * a courier arrival after a randomized delay which falls between the minDelayInMilliseconds and 
 * maxDelayInMillseconds
 *
 */
public class CourierSimulationImpl implements CourierSimulation {
	
    static Logger log = LoggerFactory.getLogger(CourierSimulationImpl.class);
    
	private final int minDelayInMilliseconds;
	private final int maxDelayInMilliseconds;
	private final KitchenSimulation kitchen;
	
	/**
	 * Constructor for CourierSimulation
	 * <p>
	 * @param kitchen  reference to Kitchen Simulation used
	 * @param minDelayInMilliseconds  the lower bound of the randomized delay time before arriving
	 * @param maxDelayInMilliseconds  the upper bound of the randomized delay time before arriving
	 */
	public CourierSimulationImpl(KitchenSimulation kitchen, int minDelayInMilliseconds, int maxDelayInMilliseconds) {
		this.kitchen = kitchen;
		this.minDelayInMilliseconds = minDelayInMilliseconds;
		this.maxDelayInMilliseconds = maxDelayInMilliseconds;
	}
	
	@Override
	public void dispatch(KitchenOrder ko) {
		
		// call kitchen.pickup after uniform distribution between minDelay and maxDelay 
		int delay = ThreadLocalRandom.current().nextInt(minDelayInMilliseconds, maxDelayInMilliseconds);
		log.info("courier dispatched, for order type: {}, id: {}, will arrive in {} ms", ko.getName(), ko.getId(), delay);
		
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.schedule(new Runnable() {
		    @Override
		    public void run() {
		        try {
		            kitchen.courierArrives(ko);
		        } catch(KitchenSimulationException e) {
		            throw new KitchenSimulationRuntimeException(e.getMessage(), e);
		        }
		        executor.shutdown();
	        }
		}, delay, TimeUnit.MILLISECONDS);
	}

}
