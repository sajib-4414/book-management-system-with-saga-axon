package com.example.ordermicroservice.command.api.aggregate;

import com.example.commonservice.commands.CompleteOrderCommand;
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
}
