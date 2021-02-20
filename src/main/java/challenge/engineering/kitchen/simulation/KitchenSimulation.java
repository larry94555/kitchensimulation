package challenge.engineering.kitchen.simulation;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.model.KitchenOrder;


public interface KitchenSimulation {
    
    /**
     * Get strategy name and trial # of situation
     */
    public String getFullName();
    
    /**
     * Block until the kitchen simulation completes
     */
    public void waitUntilComplete();
     
    /**
     * Courier arrives to pick up a kitchen order
     * <p>
     * @param ko  the kitchen order
     * @return  true if the order was picked u
     */
    public boolean courierArrives(KitchenOrder ko) throws KitchenSimulationException;
    
    /**
     * Place new kitchen order
     * <p>
     * @param ko  the kitchen order
     */
    public void placeOrder(KitchenOrder ko) throws KitchenSimulationException;
    
    /**
     * Run the simulation
     */
    public void runSimulation();
}
