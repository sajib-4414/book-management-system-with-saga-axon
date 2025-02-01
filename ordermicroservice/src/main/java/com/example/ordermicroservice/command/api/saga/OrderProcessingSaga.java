package com.example.ordermicroservice.command.api.saga;


import com.example.commonservice.commands.*;
import com.example.commonservice.events.*;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
//order processing is the START of saga, so after this, there is start saga, and end saga
//all the events that u handle for saga, has to be annotated with @saga
public class OrderProcessingSaga {

    @Autowired
    private  transient CommandGateway commandGateway;
    @Autowired
    private  transient QueryGateway queryGateway;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handleOrderCreatedEvent(OrderCreatedEvent event){
        log.info("Order created event in saga for order id :{}",event.getOrderId());
        log.info("printing the event for debug");
        System.out.println(event);

        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery = new GetUserPaymentDetailsQuery(event.getUserId());

        User user = null;
        try {
            log.info("Sending query through queryGateway...");
            log.info("printing the query that is being sent"+getUserPaymentDetailsQuery);
            user = queryGateway.query(getUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class))
                    .get(10, TimeUnit.SECONDS);
            log.info("Query response received: {}", user);
            log.info("here...............");
            log.info("Creating ValidatePaymentCommand for orderId: {}", event.getOrderId());
            ValidatePaymentCommand validatePaymentCommand = ValidatePaymentCommand
                    .builder()
                    .cardDetails(user.getCardDetails())
                    .orderId(event.getOrderId())
                    .paymentId(UUID.randomUUID().toString())
                    .build();

            log.info("Sending ValidatePaymentCommand with paymentId: {}", validatePaymentCommand.getPaymentId());
            commandGateway.sendAndWait(validatePaymentCommand);
            log.info("ValidatePaymentCommand sent successfully");

        } catch (Exception e) {
            log.error("Error querying user details or validating command: ", e);
            log.info("cancelling the order from saga");
            //if error, start the compensating transaction here.
            cancelOrderCommand(event.getOrderId());
        }



    }

    private void cancelOrderCommand(String orderId) {
        CancelOrderCommand command = new CancelOrderCommand(orderId);
        commandGateway.send(command);

    }
    @SagaEventHandler(associationProperty = "orderId")
    public void handleOrderCancelledEvent(OrderCancelledEvent event){

    }

    // this event was already consumed in payment service, 2times, eventsource handler, event handler
    // event source handler stored in the aggregate that will go to event store(for replay, storing sequence purpose)
    // event handler stored the payment inthe payment service db
    // Now, same event is consumed in order processing microservice, as saga handler, because ordermicroservice
    //is the orchestrator, it will listen, then send command to do the next stage in distributed transaction
    @SagaEventHandler(associationProperty = "orderId")
    private void handlePaymentProcessedEvent(PaymentProcessedEvent event){
        log.info("PaymentProcessedEvent in Saga for Order Id: {}", event.getOrderId());

        try{
//            if(true)
//                throw new Exception("just throwing some expection to try");
            ShipOrderCommand shipOrderCommand = ShipOrderCommand.builder()
                    .shipmentId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .build();
            commandGateway.send(shipOrderCommand);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            //start the compensating transaction
            cancelPaymentCommand(event);
        }

    }
    private void cancelPaymentCommand(PaymentProcessedEvent event) {
        CancelPaymentCommand cancelPaymentCommand
                = new CancelPaymentCommand(
                event.getPaymentId(), event.getOrderId()
        );
        log.info("cancel payment command issued from order processing saga.,. for order id "+event.getOrderId());
        commandGateway.send(cancelPaymentCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handleOrderShippedEvent(OrderShippedEvent event){
        log.info("handling order shipped event in saga for order id "+event.getOrderId());


        try{
            log.info("sending command to shipment.....");
            CompleteOrderCommand command = CompleteOrderCommand.builder()
                    .orderId(event.getOrderId())
                    .orderStatus("APPROVED")
                    .build();
            commandGateway.send(command);
            log.info("command to shipment sent");
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            //start the compensating transaction
        }

    }


    @EndSaga
    public void handleOrderCompleted(OrderCompletedEvent event){
        log.info("handling OrderCompletedEvent event in saga for order id "+event.getOrderId());

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentCancelledEvent event) {
        log.info("PaymentCancelledEvent in Saga for Order Id : {}",
                event.getOrderId());
        cancelOrderCommand(event.getOrderId());
    }
}
