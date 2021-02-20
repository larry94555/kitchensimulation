package challenge.engineering.kitchen.exception;

/**
 * A runtime exception occurred while running the kitchen simulation.
 *
 */
public class KitchenSimulationRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public KitchenSimulationRuntimeException(String errorMessage) {
        super(errorMessage);
    }

    public KitchenSimulationRuntimeException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
}
