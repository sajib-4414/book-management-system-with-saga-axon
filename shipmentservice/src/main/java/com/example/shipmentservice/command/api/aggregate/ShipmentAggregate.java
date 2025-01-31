package com.example.shipmentservice.command.api.aggregate;

import com.example.commonservice.commands.ShipOrderCommand;
import com.example.commonservice.events.OrderShippedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Slf4j
@Aggregate
public class ShipmentAggregate {

    @AggregateIdentifier
    private String shipmentId;
    private String orderId;
    private String shipmentStatus;

    public ShipmentAggregate(){

    }

    @CommandHandler
    public ShipmentAggregate(ShipOrderCommand shipOrderCommand){
        log.info("ship order command received in shipping microservice...............");
        //Validate the Command
        //Publish the Order Shipped event
        OrderShippedEvent orderShippedEvent
                = OrderShippedEvent.builder()
                .shipmentId(shipOrderCommand.getShipmentId())
                .orderId(shipOrderCommand.getOrderId())
                .shipmentStatus("COMPLETED")
                .build();
        AggregateLifecycle.apply(orderShippedEvent);
        log.info("shipment event dispatched...........");
    }

    @EventSourcingHandler
    public void onOrderShippedEvent(OrderShippedEvent event){
        this.orderId = event.getOrderId();
        this.shipmentId = event.getShipmentId();
        this.shipmentStatus = event.getShipmentStatus();

    }
}
