package com.example.ordermicroservice.command.api.saga;

import com.example.ordermicroservice.command.api.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

@Saga
@Slf4j
//order processing is the START of saga, so after this, there is start saga, and end saga
//all the events that u handle for saga, has to be annotated with @saga
public class OrderProcessingSaga {

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handleOrderCreatedEvent(OrderCreatedEvent event){
        log.info("Order created event in saga for order id :{}",event.getOrderId());
    }
}
