package challenge.engineering.kitchen.handling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import challenge.engineering.kitchen.model.KitchenOrder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import challenge.engineering.kitchen.exception.KitchenSimulationException;

class OrderStatusImplTest {
    
    private static final long DELAY_DURATION = 1000;
    
    // helper methods
    private OrderStatus getOrderReceived() throws KitchenSimulationException {
        OrderStatus orderReceived = new OrderStatusImpl();
        orderReceived.handleEvent(KitchenOrder.Event.NEW_ORDER_ARRIVES);
        return orderReceived;
    }
    
    private OrderStatus getCourierArrivesFirst() throws KitchenSimulationException {
        OrderStatus courierArrivesFirst = getOrderReceived();
        courierArrivesFirst.handleEvent(KitchenOrder.Event.COURIER_ARRIVES); 
        return courierArrivesFirst;
    }
    
    private OrderStatus getFoodReadyFirst() throws KitchenSimulationException {
        OrderStatus foodReadyFirst = getOrderReceived();
        foodReadyFirst.handleEvent(KitchenOrder.Event.FOOD_IS_READY);
        return foodReadyFirst;
    }
    
    private void delay(long duration) {
        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
        }
    }
    
    private long getFoodWaitTime(OrderStatus status) {
        return status.getPickupTimeInMillis() - status.getFoodReadyTimeInMillis();
    }
    
    private long getCourierWaitTime(OrderStatus status) {
        return status.getPickupTimeInMillis() - status.getCourierArrivalTimeInMillis();
    }

    @Test
    void noOrderYetTest() {
        OrderStatus noOrderYet = new OrderStatusImpl();
        assertFalse(noOrderYet.isOrderComplete());
        assertFalse(noOrderYet.isWaitingOnCourier());
        assertFalse(noOrderYet.isWaitingOnFood());
        assertEquals(0, getCourierWaitTime(noOrderYet));
        assertEquals(0, getFoodWaitTime(noOrderYet));
    }
    
    @Test
    void orderReceivedTest() throws KitchenSimulationException {
        OrderStatus orderReceived = getOrderReceived();
        assertFalse(orderReceived.isOrderComplete());
        assertFalse(orderReceived.isWaitingOnCourier());
        assertFalse(orderReceived.isWaitingOnFood());
    }
    
    @Test
    void courierArrivesFirstTest() throws KitchenSimulationException {
        OrderStatus courierArrivesFirst = getCourierArrivesFirst();
        assertFalse(courierArrivesFirst.isOrderComplete());
        assertFalse(courierArrivesFirst.isWaitingOnCourier());
        assertTrue(courierArrivesFirst.isWaitingOnFood());  
    }
    
    @Test
    void foodArrivesFirstTest() throws KitchenSimulationException {
        OrderStatus foodArrivesFirst = getFoodReadyFirst();
        assertFalse(foodArrivesFirst.isOrderComplete());
        assertTrue(foodArrivesFirst.isWaitingOnCourier());
        assertFalse(foodArrivesFirst.isWaitingOnFood());  
    }
    
    @Test
    void orderCompleteCourierArrivesFirstTest() throws KitchenSimulationException {
        OrderStatus courierArrivesFirst = getCourierArrivesFirst();
        delay(DELAY_DURATION);
        courierArrivesFirst.handleEvent(KitchenOrder.Event.FOOD_IS_READY);
        assertTrue(courierArrivesFirst.isOrderComplete());
        assertFalse(courierArrivesFirst.isWaitingOnCourier());
        assertFalse(courierArrivesFirst.isWaitingOnFood());
        assertTrue(getCourierWaitTime(courierArrivesFirst) >= DELAY_DURATION);
        assertEquals(0, getFoodWaitTime(courierArrivesFirst));
    }
    
    @Test
    void orderCompleteFoodReadyFirstTest() throws KitchenSimulationException {
        OrderStatus foodReadyFirst = getFoodReadyFirst();
        delay(DELAY_DURATION);
        foodReadyFirst.handleEvent(KitchenOrder.Event.COURIER_ARRIVES);
        assertTrue(foodReadyFirst.isOrderComplete());
        assertFalse(foodReadyFirst.isWaitingOnCourier());
        assertFalse(foodReadyFirst.isWaitingOnFood());
        assertTrue(getFoodWaitTime(foodReadyFirst) >= DELAY_DURATION);
        assertEquals(0, getCourierWaitTime(foodReadyFirst));
    }
    
    @Test
    void orderCompleteEventTest() throws KitchenSimulationException {
        // Case 1: foodArrivesFirst
        OrderStatus foodArrivesFirst = getFoodReadyFirst();
        delay(DELAY_DURATION);
        foodArrivesFirst.handleEvent(KitchenOrder.Event.ORDER_COMPLETED);
        assertTrue(foodArrivesFirst.isOrderComplete());
        assertFalse(foodArrivesFirst.isWaitingOnCourier());
        assertFalse(foodArrivesFirst.isWaitingOnFood());
        assertTrue(getFoodWaitTime(foodArrivesFirst) >= DELAY_DURATION);
        assertEquals(0, getCourierWaitTime(foodArrivesFirst));
        // Case 2: Courier Arrives First
        OrderStatus courierArrivesFirst = getCourierArrivesFirst();
        delay(DELAY_DURATION);
        courierArrivesFirst.handleEvent(KitchenOrder.Event.ORDER_COMPLETED);
        assertTrue(courierArrivesFirst.isOrderComplete());
        assertFalse(courierArrivesFirst.isWaitingOnCourier());
        assertFalse(courierArrivesFirst.isWaitingOnFood());
        assertTrue(getCourierWaitTime(courierArrivesFirst) >= DELAY_DURATION);
        assertEquals(0, getFoodWaitTime(courierArrivesFirst));
    }
    
    @Test
    void assignToWaitingCourierTest() throws KitchenSimulationException {
        OrderStatus courierArrivesFirst = getCourierArrivesFirst();
        delay(DELAY_DURATION);
        OrderStatus foodReadyFirst = getFoodReadyFirst();
        foodReadyFirst.assignToWaitingCourier(courierArrivesFirst);
        assertTrue(foodReadyFirst.isOrderComplete());
        assertFalse(foodReadyFirst.isWaitingOnCourier());
        assertFalse(foodReadyFirst.isWaitingOnFood());
        assertTrue(getCourierWaitTime(foodReadyFirst) >= DELAY_DURATION);
        assertEquals(0, getFoodWaitTime(foodReadyFirst));
    }
    
    @Test
    void isStringTest() throws KitchenSimulationException {
        OrderStatus courierArrivesFirst = getCourierArrivesFirst();
        assertNotNull(courierArrivesFirst.toString());
        assertTrue(courierArrivesFirst.toString().contains("foodReadyTime"));
    }
    
    @Test
    void newOrderArrivesBadStateTest() throws KitchenSimulationException {
        OrderStatus orderReceived = getOrderReceived();
        for (KitchenOrder.Event event : Arrays.<KitchenOrder.Event>asList(KitchenOrder.Event.NEW_ORDER_ARRIVES, KitchenOrder.Event.ORDER_COMPLETED )) {
            Assertions.assertThrows(KitchenSimulationException.class, () -> {
                orderReceived.handleEvent(event);
              });
        }
    }
    
    @Test
    void courierArrivesFirstBadStateTest() throws KitchenSimulationException {
        OrderStatus courierArrivesFirst = getCourierArrivesFirst();
        for (KitchenOrder.Event event : Arrays.<KitchenOrder.Event>asList(KitchenOrder.Event.NEW_ORDER_ARRIVES, KitchenOrder.Event.COURIER_ARRIVES)) {
            Assertions.assertThrows(KitchenSimulationException.class, () -> {
                courierArrivesFirst.handleEvent(event);
              });
        }
    }
    
    @Test
    void foodArrivesFirstBadStateTest() throws KitchenSimulationException {
        OrderStatus foodArrivesFirst = getFoodReadyFirst();
        for (KitchenOrder.Event event : Arrays.<KitchenOrder.Event>asList(KitchenOrder.Event.NEW_ORDER_ARRIVES, KitchenOrder.Event.FOOD_IS_READY)) {
            Assertions.assertThrows(KitchenSimulationException.class, () -> {
                foodArrivesFirst.handleEvent(event);
              });
        }
    }
    
    @Test
    void orderCompleteBadStateTest() throws KitchenSimulationException {
        OrderStatus orderComplete = getFoodReadyFirst();
        orderComplete.handleEvent(KitchenOrder.Event.ORDER_COMPLETED);
        for (KitchenOrder.Event event : Arrays.<KitchenOrder.Event>asList(KitchenOrder.Event.NEW_ORDER_ARRIVES, KitchenOrder.Event.FOOD_IS_READY,
                KitchenOrder.Event.COURIER_ARRIVES, KitchenOrder.Event.ORDER_COMPLETED)) {
            Assertions.assertThrows(KitchenSimulationException.class, () -> {
                orderComplete.handleEvent(event);
              });
        }
    }
    
    @Test
    void handleEventBadEventTest() throws KitchenSimulationException {
        OrderStatus orderStatus = new OrderStatusImpl();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            orderStatus.handleEvent(KitchenOrder.Event.UNEXPECTED);
          });
    }
    
    @Test
    void assignCourierBadStateTest() throws KitchenSimulationException {
        OrderStatus orderStatus = getCourierArrivesFirst();
        OrderStatus other = getFoodReadyFirst();
        Assertions.assertThrows(KitchenSimulationException.class, () -> {
            orderStatus.assignToWaitingCourier(other);
          });
    }
}
