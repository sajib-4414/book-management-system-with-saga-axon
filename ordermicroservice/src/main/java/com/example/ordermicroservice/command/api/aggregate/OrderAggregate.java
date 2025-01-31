package com.example.ordermicroservice.command.api.aggregate;

import com.example.commonservice.commands.CancelOrderCommand;
import com.example.commonservice.commands.CompleteOrderCommand;
import com.example.commonservice.events.OrderCancelledEvent;
import com.example.commonservice.events.OrderCompletedEvent;
import com.example.ordermicroservice.command.api.command.CreateOrderCommand;
import com.example.ordermicroservice.command.api.events.OrderCreatedEvent;

import org.aspectj.weaver.ast.Or;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId ;

    private String productId;
    private String userId;
    private String addressId;
    private Integer quantity;

    private String orderStatus;

    public OrderAggregate(){

    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand){
        //validate the command

        System.out.println("received create order command "+createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);
        AggregateLifecycle.apply(orderCreatedEvent);
    }

    @EventSourcingHandler
    public void onOrderCreated(OrderCreatedEvent event){
        this.orderStatus = event.getOrderStatus();
        this.userId = event.getUserId();
        this.orderId = event.getOrderId();
        this.quantity = event.getQuantity();
        this.productId = event.getProductId();
        this.addressId = event.getAddressId();
    }

    @CommandHandler
    public void handleCompleteOrderCommand(CompleteOrderCommand completeOrderCommand){
        //validate the command
        //publish order completed event
        OrderCompletedEvent orderCompletedEvent = OrderCompletedEvent.builder()
                .orderStatus(completeOrderCommand.getOrderStatus())
                .orderId(completeOrderCommand.getOrderId())
                .build();
        AggregateLifecycle.apply(orderCompletedEvent);
    }

    @EventSourcingHandler
    public void onCompleteOrder(OrderCompletedEvent event){
        this.orderStatus = event.getOrderStatus();
    }

    @CommandHandler
    public void handleOrderCancelled(CancelOrderCommand cancelOrderCommand){
        OrderCancelledEvent event = new OrderCancelledEvent();
        BeanUtils.copyProperties(cancelOrderCommand, event);
        AggregateLifecycle.apply(event);
        //for every such event u dispatch u need to handle even sourcing, so the event is written to the eventstore
        //then u need to handle it in the orderservice event handler to write update the status of the order in order serivce DB(regular db)
        //then u need to handle it in the saga, to do the saga orchestration
    }

    @EventSourcingHandler
    public void onOrderCancelledEvent(OrderCancelledEvent event){
        this.orderStatus = event.getOrderStatus();
    }
}
