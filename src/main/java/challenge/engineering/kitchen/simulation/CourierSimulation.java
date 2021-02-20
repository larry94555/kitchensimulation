package challenge.engineering.kitchen.simulation;

import challenge.engineering.kitchen.model.KitchenOrder;

public interface CourierSimulation {
    
    /**
     * Dispatch a kitchen order to a courier
     * <p>
     * @param order  the kitchen order dispatched to a courier
     */
    public void dispatch(KitchenOrder ko); 

}
