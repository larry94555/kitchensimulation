package challenge.engineering.kitchen.exception;

/**
 * An exception used to indicate either a problem with a configuration parameter or the running of the simulation.
 *
 */
public class KitchenSimulationException extends Exception {
	private static final long serialVersionUID = 1L;

    public KitchenSimulationException(String errorMessage) {
        super(errorMessage);
    }

    public KitchenSimulationException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
}
