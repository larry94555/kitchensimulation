package challenge.engineering.kitchen.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import challenge.engineering.kitchen.exception.KitchenSimulationException;
import challenge.engineering.kitchen.model.Strategy;

class KitchenSimulationConfigTest {

    @Test
    void loadConfigTest() throws KitchenSimulationException {
        KitchenSimulationConfig config = KitchenSimulationConfig.load("src/main/resources/kitchen_simulation_config.json");
        assertNotNull(config);
    }

    @Test
    void missingConfigTest() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationConfig.load("file_do_not_exist");
          });
    }
    
    @Test
    void wrongFormatTest() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationConfig.load("src/main/resources/dispatch_orders.json");
          });
    }
    
    @Test
    void validateKitchenSimulationConfigTest() throws KitchenSimulationException {
        String dummyConfigFilename = "dummy.json";
        KitchenSimulationConfig config = new KitchenSimulationConfig();
        List<Strategy> strategies = Arrays.asList(new Strategy());
        config.setBlocking(true);
        config.setStrategies(strategies);
        config.setNumTrials(KitchenSimulationConfig.MAX_NUM_TRIALS);
        config.setOrderJsonFile(dummyConfigFilename);
        assertEquals(dummyConfigFilename, config.getOrderJsonFile());
        assertTrue(config.isBlocking());
        assertEquals(1, config.getStrategies().size());
        assertEquals(KitchenSimulationConfig.MAX_NUM_TRIALS, config.getNumTrials());
    }
    
    @Test
    void setNumTrialsOutOfRange() throws KitchenSimulationException {
        KitchenSimulationConfig config = new KitchenSimulationConfig();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            config.setNumTrials(KitchenSimulationConfig.MAX_NUM_TRIALS + 1);
          });
    }
    
    @Test
    void setOrdersPerTrialOutOfRange() throws KitchenSimulationException {
        KitchenSimulationConfig config = new KitchenSimulationConfig();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            config.setOrdersPerTrial(-1);
          });
    }
}
