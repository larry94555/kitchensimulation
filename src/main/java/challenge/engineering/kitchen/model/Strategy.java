package challenge.engineering.kitchen.model;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import challenge.engineering.kitchen.exception.KitchenSimulationException;

/**
 * 
 * The Strategy is a set of conditions that are being evaluated by a kitchen simulation.
 * <p>
 * Two required properties:<p><ul> 
 * <li>name: Name used to identify statistics
 * <li>courierMatchedToOrder:  Is a courier dedicates to a specific order or can they accept any order.
 * </ul><p>
 * Strategy also supports the following parameters:<p><ul>
 * <li>orderPeriodInMilliseconds:  slows down evaluation but very useful for reading console output.
 * <li>ordersPerPeriod: number of trials - increasing the trials results in a more accurate evaluation
 * <li>courierMinDelayInMilliseconds: 
 * <li>courierMaxDelayInMilliseconds:
 * </ul><p>
 * Name is used to organize the results.  Results are organized by name in alphabetical order.
 *
 */
public class Strategy {
	
	private String name;
	private Boolean courierMatchedToOrder;
	private int orderPeriodInMilliseconds;
    private int ordersPerPeriod;
    private int courierMinDelayInMilliseconds;
    private int courierMaxDelayInMilliseconds;
    
    public static final int MIN_ORDER_PERIOD_IN_MILLISECONDS = 100;
    public static final int MAX_ORDER_PERIOD_IN_MILLISECONDS = 60000;
    public static final int DEFAULT_ORDER_PERIOD_IN_MILLISECONDS = 1000;
    
    public static final int MIN_ORDERS_PER_PERIOD = 1;
    public static final int DEFAULT_ORDERS_PER_PERIOD = 2;
    
    public static final int LOWER_BOUND_COURIER_MIN_DELAY_IN_MILLIS = 10;
    public static final int UPPER_BOUND_COURIER_MIN_DELAY_IN_MILLIS = 1000000;
    public static final int DEFAULT_COURIER_MIN_DELAY_IN_MILLIS = 3000;
    
    public static final int LOWER_BOUND_COURIER_MAX_DELAY_IN_MILLIS = 10;
    public static final int UPPER_BOUND_COURIER_MAX_DELAY_IN_MILLIS = 10000000;
    public static final int DEFAULT_COURIER_MAX_DELAY_IN_MILLIS = 15000;
    
    public Strategy() {
        // set defaults
        orderPeriodInMilliseconds = DEFAULT_ORDER_PERIOD_IN_MILLISECONDS;
        ordersPerPeriod = DEFAULT_ORDERS_PER_PERIOD;
        courierMinDelayInMilliseconds = DEFAULT_COURIER_MIN_DELAY_IN_MILLIS;
        courierMaxDelayInMilliseconds = DEFAULT_COURIER_MAX_DELAY_IN_MILLIS;
    }
	
	public boolean isCourierMatchedToOrder() {
		return courierMatchedToOrder;
	}
	public void setCourierMatchedToOrder(boolean courierMatchedToOrder) {
		this.courierMatchedToOrder = courierMatchedToOrder;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public int getOrderPeriodInMilliseconds() {
        return orderPeriodInMilliseconds;
    }
    
    public void setOrderPeriodInMilliseconds(int orderPeriodInMilliseconds) throws KitchenSimulationException {
        Range<Integer> allowedRange = Range.between(MIN_ORDER_PERIOD_IN_MILLISECONDS, 
                MAX_ORDER_PERIOD_IN_MILLISECONDS);
        if (!allowedRange.contains(orderPeriodInMilliseconds)) {
            throw new KitchenSimulationException("orderPeriodInMilliseconds must be in the range: " + allowedRange);
        }
        this.orderPeriodInMilliseconds = orderPeriodInMilliseconds;
    }
    public int getOrdersPerPeriod() {
        return ordersPerPeriod;
    }
    public void setOrdersPerPeriod(int ordersPerPeriod) throws KitchenSimulationException {
        if (ordersPerPeriod < 1) {
            throw new KitchenSimulationException("ordersPerPeriod must be greater or equal to 1");
        }
        this.ordersPerPeriod = ordersPerPeriod;
    }
    
    public int getCourierMinDelayInMilliseconds() {
        return courierMinDelayInMilliseconds;
    }
    public void setCourierMinDelayInMilliseconds(int courierMinDelayInMilliseconds) throws KitchenSimulationException {
        Range<Integer> allowedRange = Range.between(LOWER_BOUND_COURIER_MIN_DELAY_IN_MILLIS, 
                UPPER_BOUND_COURIER_MIN_DELAY_IN_MILLIS);
        if (!allowedRange.contains(courierMinDelayInMilliseconds)) {
            throw new KitchenSimulationException("courierMinDelayInMilliseconds must be in the range: " + allowedRange);
        } 
        this.courierMinDelayInMilliseconds = courierMinDelayInMilliseconds;
    }
    public int getCourierMaxDelayInMilliseconds() {
        return courierMaxDelayInMilliseconds;
    }
    
    public void setCourierMaxDelayInMilliseconds(int courierMaxDelayInMilliseconds) throws KitchenSimulationException {
        Range<Integer> allowedRange = Range.between(LOWER_BOUND_COURIER_MAX_DELAY_IN_MILLIS, 
                UPPER_BOUND_COURIER_MAX_DELAY_IN_MILLIS);
        if (!allowedRange.contains(courierMaxDelayInMilliseconds)) {
            throw new KitchenSimulationException("courierMaxDelayInMilliseconds must be in the range: " + allowedRange);
        } 
        this.courierMaxDelayInMilliseconds = courierMaxDelayInMilliseconds;
    }
	
	// a valid strategy must have a nonblank 'name' and setting for 'courierMatchedToOrder'
    /**
     * Validate that the strategy contains the required fields of name and courierMatchedToOrder condition 
     * <p>
     * @return  true if strategy containes the required parameters of name and courierMatchedToOrder
     */
	public boolean valid() {
	    return StringUtils.isNotBlank(name) && courierMatchedToOrder != null;
	}
}
