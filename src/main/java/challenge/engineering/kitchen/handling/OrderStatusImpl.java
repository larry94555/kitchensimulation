package challenge.engineering.kitchen.handling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.model.KitchenOrder;

/**
 * 
 * OrderStatus holds states for a given order.  THere are 5 states:
 * <p><ul>
 * <li>NO_ORDER_YET:            No order has yet been associated with this object.
 * <li>ORDER_RECEIVED:          An order has been placed.
 * <li>COURIER_ARRIVES_FIRST:   The courier arrives before the food is ready.
 * <li>FOOD_ARRIVES_FIRST:      The food is ready before the courier arrives.
 * <li>ORDER_COMPLETED:         The food is ready and has been given to a courier for delivery.
 *</ul>
 */
public class OrderStatusImpl implements OrderStatus {
    
    static Logger log = LoggerFactory.getLogger(OrderStatusImpl.class);
    
    private State state;
    private long foodReadyTimeInMills;
    private long courierArrivalTimeInMillis;
    private long pickupTimeInMillis;
    
    private enum State {
        NO_ORDER_YET,
        ORDER_RECEIVED,
        COURIER_ARRIVES_FIRST,
        FOOD_ARRIVES_FIRST,
        ORDER_COMPLETED
    };
   
    /**
     * 
     */
    public OrderStatusImpl() {
        foodReadyTimeInMills = 0;
        courierArrivalTimeInMillis = 0;
        pickupTimeInMillis = 0;
        state = State.NO_ORDER_YET;
    }
    
    @Override
    public long getFoodReadyTimeInMillis() {
        return foodReadyTimeInMills;
    }
    
    @Override
    public long getCourierArrivalTimeInMillis() {
        return courierArrivalTimeInMillis;
    }
    
    @Override
    public long getPickupTimeInMillis() {
        return pickupTimeInMillis;
    }
    
    private void makeOrderComplete() {
        state = State.ORDER_COMPLETED;
        pickupTimeInMillis = System.currentTimeMillis();
    }
    
    @Override
    public void handleEvent(KitchenOrder.Event event) throws KitchenSimulationException {
        switch(event) {
            case NEW_ORDER_ARRIVES: {
                if (state == State.NO_ORDER_YET) {
                    state = State.ORDER_RECEIVED;
                } else {
                    throw new KitchenSimulationException("Unexpected State for NEW_ORDER_ARRIVES: " + state);
                }
                break;
            }
            case COURIER_ARRIVES: {
                if (state == State.ORDER_RECEIVED) {
                    state = State.COURIER_ARRIVES_FIRST;
                } else if (state == State.FOOD_ARRIVES_FIRST) {
                    makeOrderComplete();
                } else {
                    throw new KitchenSimulationException("Unexpected State for COURIER_ARRIVES: " + state);
                }
                if (courierArrivalTimeInMillis == 0) {
                    courierArrivalTimeInMillis = System.currentTimeMillis();
                }
                break;
            }
            case FOOD_IS_READY: {
                if (state == State.ORDER_RECEIVED) {
                    state = State.FOOD_ARRIVES_FIRST;
                } else if (state == State.COURIER_ARRIVES_FIRST) {
                    makeOrderComplete();
                } else {
                    throw new KitchenSimulationException("Unexpected State for FOOD_IS_READY: " + state);
                }
                if (foodReadyTimeInMills == 0) {
                    foodReadyTimeInMills = System.currentTimeMillis();
                }
                break;
            }
            case ORDER_COMPLETED: {
                if (state != State.COURIER_ARRIVES_FIRST && state != State.FOOD_ARRIVES_FIRST) {
                    throw new KitchenSimulationException("Unexpected State for ORDER_COMPLETED: " + state);
                }
                makeOrderComplete();
                if (foodReadyTimeInMills == 0) {
                    foodReadyTimeInMills = pickupTimeInMillis;
                }
                if (courierArrivalTimeInMillis == 0) {
                    courierArrivalTimeInMillis = pickupTimeInMillis;
                }
                break;
            }
            default:
                throw new KitchenSimulationException("Unexpected event: " + event);
        };
    }
        
    @Override
    public boolean isOrderComplete() {
        return state == State.ORDER_COMPLETED;
    }
    
    /**
     * 
     * @return
     */
    @Override
    public boolean isWaitingOnCourier() {
        return state == State.FOOD_ARRIVES_FIRST;
    }
    
    @Override
    public boolean isWaitingOnFood() {
        return state == State.COURIER_ARRIVES_FIRST;
    }
    
    @Override
    public void assignToWaitingCourier(OrderStatus other) throws KitchenSimulationException {
        if (isWaitingOnCourier() && other.isWaitingOnFood()) {
            courierArrivalTimeInMillis = other.getCourierArrivalTimeInMillis();
            makeOrderComplete();
        } else {
            throw new KitchenSimulationException("Unexpected state: found isWaitingOnCourier: " + isWaitingOnCourier() + ", assigned has isWaitingOnFood: " + other.isWaitingOnFood() + ", expected COURIER_ARRIVES_FIRST);");
        }
    }
    
    @Override
    public String toString() {
        return "OrderStatus [foodReadyTime=" + foodReadyTimeInMills + ", courierReadyTime=" + courierArrivalTimeInMillis
                + ", pickupTime=" + pickupTimeInMillis + "]";
    }
}
