package challenge.engineering.kitchen.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import challenge.engineering.kitchen.exception.KitchenSimulationException;

class KitchenOrderTest {

    private KitchenOrder ko = new KitchenOrder();
    private static final String testId = "123";
    private static final Integer testPrepTime = 200;
    private static final String testName = "food";
   
    @Test
    void validateKitchenOrderTest() {
        ko.setId(testId);
        ko.setPrepTime(testPrepTime);
        ko.setName(testName);
        assertEquals(testId,ko.getId());
        assertEquals(testPrepTime.intValue(),ko.getPrepTime());
        assertEquals(testName,ko.getName());
        assertNotNull(ko.toString());
        assertTrue(ko.toString().contains(testId));
        assertTrue(ko.toString().contains(testName));
        assertTrue(ko.toString().contains(testPrepTime.toString()));
    }
    
    @Test
    void loadListTest() throws KitchenSimulationException {
        List<KitchenOrder> orders = KitchenOrder.load("src/main/resources/dispatch_orders.json");
        assertEquals(132,orders.size());
        
        for (KitchenOrder ko : orders) {
            assertNotNull(ko.getId());
            assertTrue(ko.getId().trim().length() > 0);
            assertNotNull(ko.getName());
            assertTrue(ko.getName().trim().length() > 0);
            assertTrue(ko.getPrepTime() > 0);
        }
    }
    
    @Test
    void validateEnumsTest() {
        assertNotNull(KitchenOrder.Event.COURIER_ARRIVES);
        assertNotNull(KitchenOrder.Event.FOOD_IS_READY);
        assertNotNull(KitchenOrder.Event.NEW_ORDER_ARRIVES);
        assertNotNull(KitchenOrder.Event.ORDER_COMPLETED);
        assertNotNull(KitchenOrder.Event.UNEXPECTED);
    }
    
    @Test
    void missingFileTest() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenOrder.load("file_do_not_exist");
          });
    }
    
    @Test
    void wrongFormatTest() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenOrder.load("src/main/resources/kitchen_simulation_config.json");
          });
    }
}
