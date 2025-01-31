package com.example.ordermicroservice.command.api.saga;


import com.example.commonservice.commands.CompleteOrderCommand;
import com.example.commonservice.commands.ShipOrderCommand;
import com.example.commonservice.commands.ValidatePaymentCommand;
import com.example.commonservice.events.OrderCompletedEvent;
import com.example.commonservice.events.OrderShippedEvent;
import com.example.commonservice.events.PaymentProcessedEvent;
import com.example.commonservice.model.User;
import com.example.commonservice.queries.GetUserPaymentDetailsQuery;
import com.example.ordermicroservice.command.api.events.OrderCreatedEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.spring.stereotype.Saga;

import java.util.UUID;

@Saga
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
//order processing is the START of saga, so after this, there is start saga, and end saga
//all the events that u handle for saga, has to be annotated with @saga
public class OrderProcessingSaga {

    private  CommandGateway commandGateway;
    private  QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handleOrderCreatedEvent(OrderCreatedEvent event){
        log.info("Order created event in saga for order id :{}",event.getOrderId());

        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery = new GetUserPaymentDetailsQuery(event.getUserId());

        User user = null;

        try{
            user = queryGateway.query(getUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();//we are waiting here
            //there is two case, whter we get the query or Not. we have to handle the exception like order cancel

        }catch (Exception e){
            log.error(e.getMessage());
            //if error, start the compensating transaction here.
        }
        ValidatePaymentCommand validatePaymentCommand = ValidatePaymentCommand
                .builder()
                .cardDetails(user.getCardDetails())
                .orderId(event.getOrderId())
                .paymentId(UUID.randomUUID().toString())
                .build();
        commandGateway.sendAndWait(validatePaymentCommand);
    }

    // this event was already consumed in payment service, 2times, eventsource handler, event handler
    // event source handler stored in the aggregate that will go to event store(for replay, storing sequence purpose)
    // event handler stored the payment inthe payment service db
    // Now, same event is consumed in order processing microservice, as saga handler, because ordermicroservice
    //is the orchestrator, it will listen, then send command to do the next stage in distributed transaction
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(PaymentProcessedEvent event){
        log.info("PaymentProcessedEvent in Saga for Order Id: {}", event.getOrderId());

        try{
            ShipOrderCommand shipOrderCommand = ShipOrderCommand.builder()
                    .shipmentId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .build();
            commandGateway.send(shipOrderCommand);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            //start the compensating transaction
        }

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handleOrderShippedEvent(OrderShippedEvent event){
        log.info("handling order shipped event in saga for order id "+event.getOrderId());
        CompleteOrderCommand command = CompleteOrderCommand.builder()
                .orderId(event.getOrderId())
                .orderStatus("APPROVED")
                .build();
        commandGateway.send(command);
    }

    @EndSaga
    public void handleOrderCompleted(OrderCompletedEvent event){
        log.info("handling OrderCompletedEvent event in saga for order id "+event.getOrderId());

    }
}
