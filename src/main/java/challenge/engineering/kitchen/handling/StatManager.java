package challenge.engineering.kitchen.handling;

import challenge.engineering.kitchen.model.Strategy;

public interface StatManager {
    
    /*
     * The type of metrics that are logged and used for statistics
     * <p><ul>
     * <li>TOTAL_ORDER_COUNT  the total orders processed for a given strategy name
     * <li>COURIER_WAIT_TIME  the time that a courier waited before the food was ready.
     * <li>FOOD_WAIT_TIME  the time that a food waited before the courier arrived.
     * </ul>
     */
    public enum Metrics {
      TOTAL_ORDER_COUNT("TOTAL_ORDER_COUNT"),
      COURIER_WAIT_TIME("COURIER_WAIT_TIME"),
      FOOD_WAIT_TIME("FOOD_WAIT_TIME");
        
      private String value;  
      Metrics(String value) {
          this.value = value;
      }
      public String getValue( ) {
          return value;
      }
    };
    
    /**
     * Get the average food wait time in milliseconds for a given strategy by name
     * <p>
     * @param strategyName  the name of the strategy related to the metric
     * @return  the average food wait time in milliseconds
     */
    public double getAverageFoodWaitTime(String strategyName);
    
    /**
     * Get the sample standard deviation for a given strategy by name and column type
     * <p>
     * @param strategyName  the name of the strategy related to the metric
     * @param column  the column type which is either COURIER_WAIT_TIME or FOOD_WAIT_TIME
     * @param mean  the mean for the column type either COURIER_WAIT_TIME or FOOD_WAIT_TIME
     * @return  the sample standard deviation or 0 if there are no values found.
     */
    public double getSampleStandardDeviation(String strategyName, String column, double mean);
    
    /**
     * Get the the median in milliseconds for a given strategy name and column type
     * <p>
     * @param strategyName  the name of the strategy related to the metric
     * @param column  the column type which is either COURIER_WAIT_TIME or FOOD_WAIT_TIME
     * @return  the median or 0 if there are no values found.
     */
    public double getMedian(String strategyName, String column);
    
    /**
     * Get the average courier wait time in milliseconds by strategy name
     * <p>
     * @param strategyName  the name of the strategy related to the metric
     * @return  the average courier wait time in milliseconds
     */
    public double getAverageCourierWaitTime(String strategyName);
    
    /**
     * Get the numbe of orders completed for a given strategy name
     * <p>
     * @param strategyName  the name of the strategy related to the metric
     * @return  the number of orders completed for a given strategy name
     */
    public long getNumOrdersCompleted(String strategyName);
    
    /**
     * Report the statistics related to all trials
     * <p>
     * @param startTime  the time stamp in milliseconds for when the trial started
     * @return  true if not empty
     */
    public boolean reportResults(long startTime);
    
    /**
     * Log the metrics for a given order
     * <p>
     * @param strategy  the strategy associated with the order being logged
     * @param status  the status of the order which includes the time stamps of related to the order.
     */
    public void updateStats(Strategy strategy, OrderStatus status);
}
