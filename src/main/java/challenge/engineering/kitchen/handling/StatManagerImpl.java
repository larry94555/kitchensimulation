package challenge.engineering.kitchen.handling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import challenge.engineering.kitchen.model.Strategy;

/**
 * Resulting wait times are added to the StatManager when the order completes.  The StatManager
 * keep 3 subtotals per strategy name:
 * <p><ul>
 * <li>Total Orders Completed
 * <li>Total Food Wait Time:  Delay between when the food is ready and the courier arrives
 * <li>Total Courier Wait Time:  Delay between when the courier arrives and the food is ready.   
 * </ul>
 * 
 */
public class StatManagerImpl implements StatManager {
    
    static Logger log = LoggerFactory.getLogger(StatManagerImpl.class);
	
	private Map<String, CumulativeTotals> statsByStrategyName = new TreeMap<>();
	private Map<String, Map<Long, Integer>> numLogsByStrategyNameAndColumn = new HashMap<>();
	private boolean usingFullStatistics = false;
	
	class CumulativeTotals {
	      long totalOrderCount=0;
	      long totalCourierWaitTime=0;
	      long totalFoodWaitTime=0;
	};
	
	/**
	 * Constructor for StatManager which logs each completed order and is used to generate statistics
	 * <p>
	 * @param usingFullStatistics  true if median and standard deviation should be included in the report at the end
	 */
	public StatManagerImpl(boolean usingFullStatistics) {
	    this.usingFullStatistics = usingFullStatistics;
	}

	@Override
	public double getAverageFoodWaitTime(String strategyName) {
	    
		CumulativeTotals totals = statsByStrategyName.get(strategyName);
		double totalCount = totals.totalOrderCount;
		double totalFoodWaitTime = totals.totalFoodWaitTime;
		return totalFoodWaitTime/totalCount;
	}
	
	@Override
	public long getNumOrdersCompleted(String strategyName) {
	    CumulativeTotals totals = statsByStrategyName.get(strategyName);
	    return totals.totalOrderCount;
	}
	
	@Override
	public double getSampleStandardDeviation(String strategyName, String column, double mean) {
	    String key = buildKey(strategyName,column);
	    double sum = 0;
	    long n = getNumOrdersCompleted(strategyName); 
	            
	    if (numLogsByStrategyNameAndColumn.containsKey(key)) {
	        for (Entry<Long, Integer> entry : numLogsByStrategyNameAndColumn.get(key).entrySet()) {
	            for (int i=0; i < entry.getValue(); i++) {
	                sum+=Math.pow((entry.getKey()-mean),2);
	            }
	        }
	    }
	    
	    return n != 1 ? Math.sqrt(sum/(n-1)) : 0;
	}
	
	@Override
	public double getMedian(String strategyName, String column) {
	    Long n = getNumOrdersCompleted(strategyName);
	    String key = buildKey(strategyName, column);
	    Map<Long, Integer> numLogs = numLogsByStrategyNameAndColumn.get(key);
	    if (n == 0 || numLogs == null) {
            return 0;
        }
	    List<Long> orderedList = new ArrayList<>();
	    for (Entry<Long,Integer> entry : numLogs.entrySet()) {
	        for (int i=0; i < entry.getValue(); i++) {
	            orderedList.add(entry.getKey());
	        }
	    }
	    return  (n % 2 == 1) ? orderedList.get((n.intValue()+1)/2-1) : (orderedList.get(n.intValue()/2-1) + orderedList.get(n.intValue()/2))/2; 
	}
	
	@Override
	public double getAverageCourierWaitTime(String strategyName) {
	    CumulativeTotals totals = statsByStrategyName.get(strategyName);
		double totalCount = totals.totalOrderCount;
		double totalCourierWaitTime = totals.totalCourierWaitTime;
		return totalCourierWaitTime/totalCount;
	}
	
	// Used for median and standard deviation which are only used when usingFullStatistics is true
	private void addToNumLogs(String key, long num) {
	    Map<Long, Integer> numLogs = numLogsByStrategyNameAndColumn.get(key);
	    if (numLogs == null) {
	        numLogs = new TreeMap<>();
	    }
	    if (numLogs.containsKey(num)) {
	        int count = numLogs.get(num);
	        count += 1;
	        numLogs.put(num, count);
	    } else {
	        numLogs.put(num,  1);
	    }
	    numLogsByStrategyNameAndColumn.put(key, numLogs);
	}
	
	private String buildKey(String strategyName, String columnName) {
	    return (new StringBuilder(strategyName))
	            .append("_").append(columnName).toString();
	}
	
	@Override
	public void updateStats(Strategy strategy, OrderStatus status) {
		synchronized(statsByStrategyName) {
			CumulativeTotals totals = statsByStrategyName.get(strategy.getName());
			if (totals == null) {
				totals = new CumulativeTotals();
			}
			long courierWaitTime = status.getPickupTimeInMillis() - status.getCourierArrivalTimeInMillis();
			long foodWaitTime = status.getPickupTimeInMillis() - status.getFoodReadyTimeInMillis();
			totals.totalOrderCount += 1;
			totals.totalCourierWaitTime += courierWaitTime;
			totals.totalFoodWaitTime += foodWaitTime;
			statsByStrategyName.put(strategy.getName(), totals);
			
			if (usingFullStatistics) {
			    String foodWaitTimeKey = buildKey(strategy.getName(), Metrics.FOOD_WAIT_TIME.getValue());
			    String courierWaitTimeKey = buildKey(strategy.getName(),  Metrics.COURIER_WAIT_TIME.getValue());
			    addToNumLogs(foodWaitTimeKey, foodWaitTime);
			    addToNumLogs(courierWaitTimeKey, courierWaitTime);
			}
		}
	}
	
	@Override
	public boolean reportResults(long startTime) {
		if (CollectionUtils.isEmpty(statsByStrategyName.keySet())) {
			log.info("No strategies found.");
			return false;
		} else {
		    // Add a blank line
		    log.info("\n");
			for (String strategyName : statsByStrategyName.keySet()) {
			    double avgFoodWaitTime = getAverageFoodWaitTime(strategyName);
			    double avgCourierWaitTime =  getAverageCourierWaitTime(strategyName);
				log.info("Strategy: {}", strategyName);
			    log.info("{} average food wait time (milliseconds) between order ready and pickup: {} ms", strategyName, avgFoodWaitTime);
				log.info("{} average courier wait time (milliseconds) between arrival and order pickup: {} ms", strategyName, avgCourierWaitTime);
				if (usingFullStatistics) {
                    log.info("{} sample std dev food wait time (milliseconds) between order ready and pickup: {} ms", strategyName, getSampleStandardDeviation(strategyName, Metrics.FOOD_WAIT_TIME.getValue(), avgFoodWaitTime));
                    log.info("{} median food wait time (milliseconds) between order ready and pickup: {} ms", strategyName, getMedian(strategyName, Metrics.FOOD_WAIT_TIME.getValue()));
                    log.info("{} sample std dev courier wait time (milliseconds) between arrival and order pickup: {} ms", strategyName, getSampleStandardDeviation(strategyName, Metrics.COURIER_WAIT_TIME.getValue(), avgCourierWaitTime));
                    log.info("{} median courier wait time (milliseconds) between order ready and pickup: {} ms", strategyName, getMedian(strategyName, Metrics.COURIER_WAIT_TIME.getValue()));
				}
				log.info("{} total orders Completed: {}", strategyName, getNumOrdersCompleted(strategyName));
				log.info("Total Time For Trials: {} sec", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
				log.info("\n");
			}
			return true;
		}
	}
}
