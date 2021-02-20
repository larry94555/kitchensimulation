# Kitchen Simulation Runner

The purpose of the KitchenSimulationRunner is to evaluate the performance of the two different strategies for handling incoming orders.

1. *Matched Strategy*: a courier is dispatched for a specific order and may only pick up that order.
2. *First-in-first-out*: a courier picks up the next available order upon arrival. If no orders are available, then the courier waits until an
order is available.  If an order is available and multiple couriers are waiting, the order goes to the courier which first arrived.  

To evaluate these two strategies, a [real-time simulation](https://en.wikipedia.org/wiki/Real-time_simulation) has been created that will simulate orders in real time and have couriers arrive
randomly following a [uniform distribution](https://en.wikipedia.org/wiki/Continuous_uniform_distribution).  

Each simulation is run against the following default parameters:

1. *orderPeriodInMilliseconds*: **1000 ms**:  Orders are placed every second.
2. *ordersPerPeriod*: **2**: Two orders are placed every period
3. *courierMinDelayInMilliseconds*: **3000**:
4. *courierMaxDelayInMilliseconds*: **15000**:  Couriers arrive to pick up food randomly between 3 and 15 seconds after being dispatched.
 
After running the simulations, each strategy can then be evaluated in terms of the following metrics:

1. *average food wait time*: the time between order ready and pick up in milliseconds
2. *average courier wait time*: the time between courier arrival and pick up in milliseconds

## Running the kitchen simulation from the command line

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

To run the default application on the command line, do the following:

1. ```mvn clean install```
2. ``` java -jar target/kitchen-1.0-SNAPSHOT-jar-with-dependencies.jar src/main/resources/kitchen_simulation_config.json```

The simulation will start, show progress in the console, and show the results at the end. 

## Running the kitchen simulation from eclipse

For building and running the application, I used Spring Tool Suite 4 with the following:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)

I used the following eclipse extensions:

- [EclEmma Java Code Coverage](https://www.eclemma.org/) 3.1.3
- [Markdown Text Editor](https://marketplace.eclipse.org/content/markdown-text-editor) for Eclipse 

If eclipse is not yet installed, instructions can be found [here](https://spring.io/guides/gs/sts/)

To load the application to eclipse, do the following:

1. Start Eclipse. From Eclipse: File -> Import -> Maven -> Existing Maven Projects
2. Browse to the top of the directory in step 1 and select the folder: kitchen-simulation-runner and Open.
3. Click the Finish button.

To build the application in eclipse, do the following:

1. Open Package Explorer and click on the arrow to open files
2. Right click on ```kitchen``` -> Run As -> Maven build... 
3. In Goals, enter ```clean install``` and then click the ```Run``` button.

To run the application within eclipse, do the following:

1. Open Package Explorer and click on the arrow to open files
2. Right click on ```kitchen``` -> Run As -> Run Configurations...
3. If no configuration exists, right click ```Java Application``` and select New Configuration
4. Click the Main Tab and enter for the main class: ```challenge.engineering.kitchen.KitchenSimulationRunner```
5. Click the Arguments Tab
6. For Program Arguments: ```src/main/resources/kitchen_simulation_config.json```
7. For VM Arguments: ```-Dlog4j.configurationFile=src/main/resources/log4j2.xml```

## Design Decisions

I decided to use Java 8, Eclipse, and Maven 3 as the tech stack.  Since this is a real-time simulation, I wanted the performance 
quality offered by the Java Virtual Machine.  While garbage collection can hurt true real-time performance and there exists a [java
standard for real time systems](https://en.wikipedia.org/wiki/Real_time_Java), I decided to start with standard Java to see how well it did. I confirmed through testing that [ScheduledThreadPoolExecutor](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html) was consistently within 10-20 ms of real time which is reasonable in the 
context of the simulation where most durations are measured in thousands of milliseconds.

Based on the requirements of the simulation, I created the following components: 

* **Kitchen Simulation Runner**:  loads the json configuration file and runs the simulations listed there.
* **Kitchen Simulation**:  simulations an instance of a kitchen based on the strategy parameters and interacting with incoming orders and a courier service.
* **Order Simulation**:  generate n orders per m milliseconds which are placed with the kitchen simulation.
* **Courier Simulation**:  receive dispatches and send out courier for pick up who arrive randomly within a range in the strategy.
* **Strategy**: the parameters of a given simulation including whether couriers are matched to a given order or whether they can pick up any order.
* **Kitchen Simulation Configuration**: A json to object mapping for the configuration parameters for the kitchen simulation runner
* **Kitchen Order**: the details of an order including id, prepTime, and name.
* **StatManager**:  collect wait time for each order and report back averages for courier wait time and food wait time.

The first question I hit was how to represent order status.  I liked the idea of keeping the Kitchen Order non-mutable and simple since the list of orders is reused by all simulations.
I decided on an OrderStatus object that contains the metrics for a specific order and handles order state transitions based on kitchen events.

* **Order Status**: holds order state, collects metrics, and handles state transitions based on events.  

To keep each of the public interfaces clean, I divided up the following components into interfaces and concrete implementations:

* ```OrderStatus``` and ```OrderStatusImpl``` 
* ```StatManager``` and ```StatManagerImpl```
* ```CourierSimulation``` and ```CourierSimulationImpl```
* ```KitchenSimulation``` and ```KitchenSimulationImpl```
* ```OrderSimulation``` and ```OrderSimulationImpl```

I added two custom exceptions 

* ```KitchenSimulationException```: exception occurring outside of a lambda, typically because of invalid parameters in the configuration file.
* ```KitchenSimulationRuntimeException```: exceptions occurring within a lambda that invalidate the running of the simulation.

To complete, the application, I needed to address the following requirements:

1. Real time simulation: ensure the simulation runs in real time 
2. Ensuring that randomization follows a uniform distribution
3. Logging events to console so that details can be tracked
4. Reporting averages on demand
5. Mapping the json configuration file to the ```KitchenSimulationConfig``` instance
6. Thread synchronization: preventing deadlocks and ensuring threads are cleaned up after they complete

### Real time simulation

There are 3 areas where real time is needed:

1. *Order Generation*:  2 orders every second
2. *Courier Arrival*:  couriers arrive between 3 and 15 seconds after being dispatched.
3. *Food Preparation*:  food is ready after an amount of time equal to the prepTime of the kitchen order has passed.

I ended up using ```ScheduledThreadPoolExecutor``` for all 3 purposes.  For *Order Generation*, I used ```scheduleAtFixedRate``` to generate 2 orders every second.
For *Courier Arrival* and *Food Preparation*, I used ```schedule``` with a delay. 

I verified that these methods worked according to requirements by reviewing the logs that included a time stamp accurate to milliseconds.

### Ensuring that randomization follows a uniform distribution

Couriers need to arrive randomly between 3 and 15 seconds.  Java provides [```ThreadLocalRandom```](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadLocalRandom.html) which is thread safe and which follows a uniform distribution.

### Logging events to console so that details can be tracked

Java provides an excellent logging framework [sfl4j](http://www.slf4j.org/) which wraps [log4j](https://logging.apache.org/log4j/2.x/).  

This provides a millisecond time stamp (which I used to verify real time accuracy), an easy way to write to console or log files as shown in ```log4j2.xml```, and a convenient way to add Strings to the log:

```log.info("order prepared, order type: {}, id: {}", ko.getName(), ko.getId());```

### Reporting average on demand

For average wait time, cumulative long values are sufficient.  I used a [```TreeMap```](https://docs.oracle.com/javase/8/docs/api/java/util/TreeMap.html) to organize strategy names in alphabetical order with a private pojo for cumulative totals.

As I was reviewing the results of the simulations, I became curious about the median and standard deviation.  I added a simple HashMap to map strategy name/field name to a TreeMap that collects all values in numerical order which I can then use
to generate the median and standard deviation.  Since these metrics are not in the original requirements, they are only displayed when configured.

### Mapping a json configuration file to an object

The [Jackson Api](https://github.com/FasterXML/jackson) provides a straight forward way to map a json configuration file to an object.

### Thread synchronization

When thinking about thread synchronization, I relied on standard java mechanisms including ```ScheduledThreadPoolExecutor``` and [Java Streams](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html).

OrderSimulationImpl synchronizes on ```counter``` to ensure that each OrderSimluation only shuts down after all orders have been placed.

KitchenSimulationImpl synchronized the method ```updateOrderStatus``` to ensure that each order update is logically consistent and prevents any race conditions between orders.

KitchenSimulation provides a ```waitUntilComplete``` method so that the ```KitchenSimulationRunner``` can wait for all simulations to complete before reporting the statistics for all simulations.

## Configuration Options

The KitchenSimulation Runner supports the following configuration parameters:

* **orderJsonFile**: required, at least 1 order required and each id must be unique, the full list of orders used in the simulation.  The file should be a json array in the same format as ```dispatch_orders.json```

* **numTrials**: default: **1**, can be between *1* and *700*, the number of trials to run for each strategy.  The order of kitchen orders is randomized for each trial and the same order is used for all strategies in that trial.  Useful for more accurate results.

* **ordersPerTrial**: default: size of orderJsonFile list, must be between **0** and the number of orders in the **orderJsonFile**. If the number is greater than the list, then the random order will include duplicates to match the number. The order of this list is randomized per trial.  Useful for speeding up testing with a smaller amount of orders.  **0** means use all orders included in the orderJsonFile.

* **usingFullStatistics**: default: false, true to report median and standard deviation for a given strategy at the end.

* **blocking**: default: true, wait for each simulation to finish before starting the next, useful for verifying log.  Setting ```blocking``` to false, greatly speeds up the running of simulations to get to the final report at the price of making it more difficult to debug.

* **strategies**: an array of strategy parameters that are used. The details on the strategy parameters are below.

Each strategy supports the following configuration parameters:

* **name**: required, cannot be blank, is the name used to organize the results across a trial.  At the end, statistics are shown for each name.

* **courierMatchedToOrder**: required, true if matched strategy is used and false if matched strategy is not used.

* **courierMinDelayInMilliseconds**:
* **courierMaxDelayInMilliseconds**: default: *3000* and *15000*, specifies the range of random values used for when the courier arrives.

* **orderPeriodInMilliseconds**:
* **ordersPerPeriod**: the frequency at which orders go out.  ```orderPeriodInMilliseconds``` has a default of **1000** and ```ordersPerPeriod``` has a default of **2**.  Changing these values determines the period of the orders placed and the numbers of orders placed at this interval.


