package challenge.engineering.kitchen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import challenge.engineering.kitchen.exception.KitchenSimulationException;

class KitchenSimulationRunnerTest {

    @Test
    void validateMain() throws KitchenSimulationException {
        KitchenSimulationRunner.main(new String[] {"src/test/resources/test_kitchen_simulation_config.json"});
    }
    
    @Test
    void mainWithMissingConfigFile() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationRunner.main(new String[] {"file_not_exist"});
          });
    }
    
    @Test
    void mainWithoutParams() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationRunner.main(new String[] {});
          });
    }
    
    @Test
    void configWithNoStrategies() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationRunner.main(new String[] { "src/test/resources/test_kitchen_simulation_config_without_strategies.json" });
          });
    }
    
    @Test
    void strategyMissingName() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationRunner.main(new String[] { "src/test/resources/test_config_with_strategy_missing_name.json" });
          });
    }
    
    @Test
    void ordersPerTrialTooLarge() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationRunner.main(new String[] { "src/test/resources/test_kitchen_simulation_config_too_many_orders_per_trial.json" });
          });
    }
    
    @Test
    void orderListThatRepeatsIds() throws KitchenSimulationException {
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            KitchenSimulationRunner.main(new String[] { "src/test/resources/test_config_with_repeating_ids.json" });
          });
    }
    
}
