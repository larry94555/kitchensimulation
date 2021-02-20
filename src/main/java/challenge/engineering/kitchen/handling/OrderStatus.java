package challenge.engineering.kitchen.handling;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.model.KitchenOrder;

/**
 * The state and logged times for a given order.
 *
 */
public interface OrderStatus {
    
    /**
     * Get the order's pick up time stamp with 0 if it is not yet picked up.
     * <p>
     * @return  the pickup time in milliseconds or 0 if the order has not yet been picked up. 
     */
    public long getPickupTimeInMillis();
    
    /**
     * Get the order's courier arrival time stamp with 0 if the courier has not yet arrived.
     * <p>
     * @return  the courier arrival time in milliseconds or 0 if the courier has not yet arrived.
     */
    public long getCourierArrivalTimeInMillis();
    
    /**
     * Get the order's food ready time stamp with 0 if the food is not yet ready.
     * <p>
     * @return  the food ready time in milliseconds or 0 if the food is not yet ready.
     */
    public long getFoodReadyTimeInMillis();
    
    /**
     * Update the order state based on the event which has occurred.
     * <p>
     * @param event  update the current state based on the event received.
     * @throws KitchenSimulationException
     */
    public void handleEvent(KitchenOrder.Event event) throws KitchenSimulationException;
    
    /**
     * Is the Order complete?
     * <p>
     * @return  true if order is complete
     */
    public boolean isOrderComplete();
    
    /**
     * Is the food ready and waiting on pick up?
     * <p>
     * @return  true if food is ready but courier has not yet arrived
     */
    public boolean isWaitingOnCourier();
    
    /**
     * Is the courier ready and waiting on the food to be ready?
     * <p>
     * @return  true if courier has arrived but the food is not yet ready
     */
    public boolean isWaitingOnFood();
    
    /**
     * Assign a food is ready order to courier associated with a different order.
     * <p>
     * OrderStatus takes the courier arrival time from the other order and marks the order complete.
     * <p> 
     * @param other  the status of the order where the courier has arrived but is still waiting for the food.
     * @throws KitchenSimulationException  if the state is incorrect for the order status itself or the state of the other.
     */
    public void assignToWaitingCourier(OrderStatus other) throws KitchenSimulationException;

}
