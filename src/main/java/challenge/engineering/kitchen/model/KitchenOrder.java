package challenge.engineering.kitchen.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import challenge.engineering.kitchen.exception.KitchenSimulationException;

/**
 * 
 * KitchenOrder is simplified model of an order placed to the kitchen  It includes id, name, and prepTime.
 * <p>
 * There are 4 events associated with an order:
 * <p><ul>
 * <li>NEW_ORDER_ARRIVES:  an order is placed.
 * <li>COURIER_ARRIVES:  the courier is ready to pick up an order.
 * <li>FOOD_IS_READY:  the food is ready to be picked up.
 * <li>ORDER_IS_COMPLETED:  the food has been given to the courier for delivery.
 * <li>UNEXPECTED:  an event that is only used for testig purposes
 *</ul>
 */
public class KitchenOrder {
   
    public enum Event {
        NEW_ORDER_ARRIVES,
        COURIER_ARRIVES,
        FOOD_IS_READY,
        ORDER_COMPLETED,
        UNEXPECTED
    };
	
	private String id;
	private String name;
    private int prepTime;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrepTime() {
		return prepTime;
	}
	
	public void setPrepTime(int prepTime) {
		this.prepTime = prepTime;
	}
	
	@Override
	public String toString() {
		return "Order [id=" + id + ", name=" + name + ", prepTime=" + prepTime + "]";
	}
	
	private final static ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Construct a list of kitchen orders from a json array file
	 * <p>
	 * @param pathToJsonFile  the path to the json file that contains the json array of kitchen orders
	 * @return  the list of kitchen orders as a java list.
	 * @throws KitchenSimulationException  if the json file does not contain the expected format
	 */
	public static List<KitchenOrder> load(String pathToJsonFile) throws KitchenSimulationException {
		try {
			
			return mapper.readValue(new File(pathToJsonFile), new TypeReference<List<KitchenOrder>>() {});		
			
		} catch(IOException e) {
			throw new KitchenSimulationException("Exception reading jsonFile: " + pathToJsonFile, e);
		}
	}
}
