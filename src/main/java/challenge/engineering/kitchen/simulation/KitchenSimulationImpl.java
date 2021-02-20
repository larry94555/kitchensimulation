package challenge.engineering.kitchen.simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.exception.KitchenSimulationRuntimeException;
import challenge.engineering.kitchen.handling.OrderStatus;
import challenge.engineering.kitchen.handling.OrderStatusImpl;
import challenge.engineering.kitchen.handling.StatManager;
import challenge.engineering.kitchen.model.KitchenOrder;
import challenge.engineering.kitchen.model.Strategy;

/**
 * The KitchenSimulation class simulates a kitchen which runs according to the strategy being evaluated.
 * <p><ul>
 * <li>It initiates the Order Simulation which sends out kitchen orders in a random order.
 * <li>It can handle when an order is placed, when food is ready, and when a courier arrives.
 * <li>For each completed order, it logs the courierWaitTime and the foodWaitTime to the statManager
 *</ul>
 */
public class KitchenSimulationImpl implements KitchenSimulation {
    
    static Logger log = LoggerFactory.getLogger(KitchenSimulationImpl.class);
    
    private final Strategy strategy;
    private final StatManager statManager;
    private int totalOrdersCompleted=0;
    private int totalOrdersToBePlaced=0;
    private Queue<KitchenOrder> waitingRoom = new PriorityBlockingQueue<KitchenOrder>();
    private Set<KitchenOrder> foodReady = new HashSet<KitchenOrder>();
    private final OrderSimulationImpl orderSimulation;
    private Map<KitchenOrder,OrderStatus> ordersProcessed = new HashMap<>();
    private final CourierSimulation courier;
    private final String fullName;
    
    private volatile Boolean completed = false;
    
    /**
     * 
     * Initialize the simulation with configuration parameters and kitchen orders
     * <p>
     * @param statManager  the shared stats for all simulations
     * @param kitchenOrders  the list of orders shared by all simulations in a given trial
     * @param config  the simulation configuration details
     * @param strategy  the strategy with parameters to be used for the kitchen simulation
     */
    public KitchenSimulationImpl(StatManager statManager, List<KitchenOrder> kitchenOrders, Strategy strategy, int trialNumber) {
        this.statManager = statManager;
        this.strategy = strategy;
        this.fullName = strategy.getName() + " trial #: " + trialNumber;
        totalOrdersToBePlaced = kitchenOrders.size();
        orderSimulation = new OrderSimulationImpl(this, kitchenOrders, strategy.getOrderPeriodInMilliseconds(), strategy.getOrdersPerPeriod());
        courier = new CourierSimulationImpl(this, strategy.getCourierMinDelayInMilliseconds(), strategy.getCourierMaxDelayInMilliseconds());
    }
    
    @Override
    public String getFullName() {
        return fullName;
    }
    
    @Override
    public void waitUntilComplete() {
        synchronized(completed) {
            while (totalOrdersCompleted < totalOrdersToBePlaced) {
                try {
                    completed.wait();
                } catch(InterruptedException e) {
                    log.error("Interrupted Exception: ", e);
                }
            }
        }
    }
    
    /* Update the order status based on an event and an order
     * <p>
     * Return true if the order is now complete
     */
    private synchronized boolean updateOrderStatus(KitchenOrder.Event event, KitchenOrder ko) throws KitchenSimulationException {
        OrderStatus status = (ordersProcessed.containsKey(ko)) ? ordersProcessed.get(ko) : new OrderStatusImpl();
        if (status.isOrderComplete()) {
            return true;
        }
        
        status.handleEvent(event);
        ordersProcessed.put(ko, status);   
        
        if (status.isOrderComplete()) {
            logWaitTimeStats(ko, status);
            return true;
        }  
        
        if (!strategy.isCourierMatchedToOrder()) {
            if (status.isWaitingOnFood() && foodReady.size() > 0) {
                Optional<KitchenOrder> attempt = foodReady.stream().findAny();
                if (attempt.isPresent()) {
                    foodReady.remove(attempt.get());
                    OrderStatus other = ordersProcessed.get(attempt.get());
                    other.assignToWaitingCourier(status);
                    logWaitTimeStats(attempt.get(), other);
                    return true;
                }
            } else if (status.isWaitingOnCourier() && waitingRoom.size() > 0) {
                KitchenOrder courierOrder = waitingRoom.remove();
                status.assignToWaitingCourier(ordersProcessed.get(courierOrder));
                logWaitTimeStats(ko,status);
                return true;
            }
        } 
        
        return false;
    }
    
    private void notifyThatSimulationIsComplete() {
        synchronized(completed) {
            completed.notifyAll();
        }
    }
    
    private void prepareOrder(KitchenOrder order) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    foodIsReady(order);
                } catch(KitchenSimulationException e) {
                    throw new KitchenSimulationRuntimeException(e.getMessage(), e);
                }
                executor.shutdown();
            }
        }, order.getPrepTime(), TimeUnit.SECONDS);
    }
    
    private void logWaitTimeStats(KitchenOrder ko, OrderStatus status) {
        statManager.updateStats(strategy, status);
        long courierWaitTime = status.getPickupTimeInMillis() - status.getCourierArrivalTimeInMillis();
        long foodWaitTime = status.getPickupTimeInMillis() - status.getFoodReadyTimeInMillis();
        log.info("{}, order picked up, type: {}, id: {}, courier wait time: {} ms, food wait time: {} ms, orderStatus: {}", fullName, ko.getName(), ko.getId(), courierWaitTime, foodWaitTime, status);
        log.info("{}, average food wait time (milliseconds) so far: {} ms", fullName, statManager.getAverageFoodWaitTime(strategy.getName()));
        log.info("{}, average courier wait time (milliseconds) so far: {} ms", fullName, statManager.getAverageCourierWaitTime(strategy.getName()));
        log.info("{}: orders completed: {}", fullName, statManager.getNumOrdersCompleted(strategy.getName()));
        totalOrdersCompleted += 1;
        if (totalOrdersCompleted == totalOrdersToBePlaced) {
            notifyThatSimulationIsComplete();
        }
    }
    
    @Override
    public boolean courierArrives(KitchenOrder ko) throws KitchenSimulationException {
        log.info("courier arrived, order type: {}, id: {}", ko.getName(), ko.getId());
        return updateOrderStatus(KitchenOrder.Event.COURIER_ARRIVES, ko);
    }
    
    private boolean foodIsReady(KitchenOrder ko) throws KitchenSimulationException {
        log.info("order prepared, order type: {}, id: {}", ko.getName(), ko.getId());
        return updateOrderStatus(KitchenOrder.Event.FOOD_IS_READY, ko);
    }
    
    @Override
    public void placeOrder(KitchenOrder ko) throws KitchenSimulationException {
        log.info("order received, order type: {}, id: {}, prepTime: {}", ko.getName(), ko.getId(), ko.getPrepTime());
        updateOrderStatus(KitchenOrder.Event.NEW_ORDER_ARRIVES, ko);
        courier.dispatch(ko);
        prepareOrder(ko);
    }
    
    @Override
    public void runSimulation() {
        orderSimulation.simulateOrders();
    }

}
