package challenge.engineering.kitchen.simulation;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.exception.KitchenSimulationRuntimeException;
import challenge.engineering.kitchen.model.KitchenOrder;

/**
 * 
 * OrderSimulation simulates the placing of orders in a soft real-time system.
 * <p>
 * It uses 2 parameters specified in the KitchenSimulation configuration file:
 * <p><ul>
 * <li>orderPeriodInMilliseconds: specifies the frequency of "ticks" where n orders are placed.
 * <li>ordersPerPeriod:  specifies the number of orders to place at each "tick"
 *</ul>
 */
public class OrderSimulationImpl implements OrderSimulation {
    
    static Logger log = LoggerFactory.getLogger(OrderSimulationImpl.class);
	
	private final List<KitchenOrder> ordersToPlace;
	private final int orderPeriodInMilliseconds;
	private final int ordersPerPeriod;
	private final KitchenSimulationImpl kitchen;
	
	private volatile Integer counter;
    private ScheduledFuture<?> tick;
	
    /**
     * Constructor for the Order Simulation
     * <p>
     * @param kitchen  reference to the kitchen simulation
     * @param ordersToPlace  the list of kitchen orders to use in the order simulation
     * @param orderPeriodInMilliseconds  the frequency at which ordered are placed
     * @param ordersPerPeriod  the number of orders placed at each period
     */
	public OrderSimulationImpl(KitchenSimulationImpl kitchen, List<KitchenOrder> ordersToPlace, int orderPeriodInMilliseconds, int ordersPerPeriod) {
		this.ordersToPlace = ListUtils.unmodifiableList(ordersToPlace);
		this.orderPeriodInMilliseconds = orderPeriodInMilliseconds;
		this.ordersPerPeriod = ordersPerPeriod;
		this.kitchen = kitchen;
	}
	
	@Override
	public void simulateOrders() {
        counter=0;
        log.info("simulateOrders: ordersToPlace size = {}", ordersToPlace.size());

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true); 
        tick = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info(
                        "new order(s) coming: tick ({} msec), counter = {}, size = {}", orderPeriodInMilliseconds, counter, ordersToPlace.size());
                synchronized(counter) {
                    IntStream.range(counter, counter+ordersPerPeriod).parallel().forEach(i -> {
                        if (i < ordersToPlace.size()) {
                            KitchenOrder ko = ordersToPlace.get(i);
                            log.info("Order placed, type: {}, id: {}", ko.getName(), ko.getId());
                            try {
                                kitchen.placeOrder(ko);
                            } catch(KitchenSimulationException e) {
                                throw new KitchenSimulationRuntimeException(e.getMessage(), e);
                            }
                        }
                    });
                    counter += ordersPerPeriod;
                }
                
                if (counter == ordersToPlace.size()) {
                    tick.cancel(false);
                    executor.shutdown();
                } 
            }
            
        }, 0, orderPeriodInMilliseconds, TimeUnit.MILLISECONDS);
	}
}
