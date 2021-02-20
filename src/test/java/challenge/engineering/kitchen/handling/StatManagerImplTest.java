package challenge.engineering.kitchen.handling;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.model.KitchenOrder;
import challenge.engineering.kitchen.model.Strategy;

class StatManagerImplTest {
    
    private static final long TEST_COURIER_WAIT_TIME = 200;
    private static final long TEST_FOOD_WAIT_TIME = 0;
    private static final long ACCEPTABLE_VARIATION_IN_MILLIS = 10;

    @Test
    void  reportResultsEmptyTest() {
        StatManager statManager = new StatManagerImpl(false);
        assertFalse(statManager.reportResults(System.currentTimeMillis()));
    }
    
    @Test
    void reportResultsWithStrategy() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        strategy.setName("test strategy");
        StatManagerImpl statManager = new StatManagerImpl(false);
        OrderStatus status = new OrderStatusImpl();
        status.handleEvent(KitchenOrder.Event.NEW_ORDER_ARRIVES);
        status.handleEvent(KitchenOrder.Event.COURIER_ARRIVES);
        try {
            Thread.sleep(TEST_COURIER_WAIT_TIME);
        } catch(InterruptedException e) {
        }
        status.handleEvent(KitchenOrder.Event.FOOD_IS_READY);
        statManager.updateStats(strategy, status);
        assertTrue(statManager.reportResults(System.currentTimeMillis()));
        assertEquals(1, statManager.getNumOrdersCompleted(strategy.getName()));
        Double avgCourierWaitTime = statManager.getAverageCourierWaitTime(strategy.getName());
        Double avgFoodWaitTime = statManager.getAverageFoodWaitTime(strategy.getName());
        assertTrue(avgCourierWaitTime.longValue() >= TEST_COURIER_WAIT_TIME);
        assertTrue(avgCourierWaitTime.longValue() <= TEST_COURIER_WAIT_TIME + ACCEPTABLE_VARIATION_IN_MILLIS);
        assertEquals(TEST_FOOD_WAIT_TIME, avgFoodWaitTime.longValue());
    }
    
    @Test
    void reportResultsWithAllZeros() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        strategy.setName("test strategy");
        StatManagerImpl statManager = new StatManagerImpl(false);
        OrderStatus zeroStatus = new OrderStatusImpl();
        zeroStatus.handleEvent(KitchenOrder.Event.NEW_ORDER_ARRIVES);
        zeroStatus.handleEvent(KitchenOrder.Event.FOOD_IS_READY);
        zeroStatus.handleEvent(KitchenOrder.Event.COURIER_ARRIVES);
        statManager.updateStats(strategy, zeroStatus);
        assertTrue(statManager.reportResults(System.currentTimeMillis()));
        assertEquals(1, statManager.getNumOrdersCompleted(strategy.getName()));
        Double avgCourierWaitTime = statManager.getAverageCourierWaitTime(strategy.getName());
        Double avgFoodWaitTime = statManager.getAverageFoodWaitTime(strategy.getName());
        assertEquals(0, avgCourierWaitTime.longValue());
        assertEquals(0, avgFoodWaitTime.longValue());
    }

}
