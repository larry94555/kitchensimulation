package challenge.engineering.kitchen.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import challenge.engineering.kitchen.exception.KitchenSimulationException;

class StrategyTest {
    
    private static final String TEST_NAME = "test";

    @Test
    void validateStategyTest() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        strategy.setName(TEST_NAME);
        strategy.setCourierMaxDelayInMilliseconds(Strategy.UPPER_BOUND_COURIER_MAX_DELAY_IN_MILLIS);
        strategy.setCourierMinDelayInMilliseconds(Strategy.LOWER_BOUND_COURIER_MIN_DELAY_IN_MILLIS);
        strategy.setOrdersPerPeriod(1);
        strategy.setOrderPeriodInMilliseconds(Strategy.MIN_ORDER_PERIOD_IN_MILLISECONDS);
        strategy.setCourierMatchedToOrder(false);
        assertFalse(strategy.isCourierMatchedToOrder());
        assertEquals(Strategy.UPPER_BOUND_COURIER_MAX_DELAY_IN_MILLIS, strategy.getCourierMaxDelayInMilliseconds());
        assertEquals(Strategy.LOWER_BOUND_COURIER_MIN_DELAY_IN_MILLIS, strategy.getCourierMinDelayInMilliseconds());
        assertEquals(Strategy.MIN_ORDER_PERIOD_IN_MILLISECONDS, strategy.getOrderPeriodInMilliseconds());
    }
    
    @Test
    void setOrderPeriodInMillisecondsOutOfRange() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            strategy.setOrderPeriodInMilliseconds(Strategy.MIN_ORDER_PERIOD_IN_MILLISECONDS-1);
          });
    }
    
    @Test
    void setCourierMinDelayInMillisecondsOutOfRange() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            strategy.setCourierMinDelayInMilliseconds(Strategy.UPPER_BOUND_COURIER_MIN_DELAY_IN_MILLIS + 1);
          });
    }
    
    @Test
    void setCourierMaxDelayInMillisecondsOutOfRange() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            strategy.setCourierMaxDelayInMilliseconds(Strategy.UPPER_BOUND_COURIER_MAX_DELAY_IN_MILLIS + 1);
          });
    }
    
    @Test
    void setOrdersPerPeriodOutOfRange() throws KitchenSimulationException {
        Strategy strategy = new Strategy();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            strategy.setOrdersPerPeriod(0);
          });
    }
}
