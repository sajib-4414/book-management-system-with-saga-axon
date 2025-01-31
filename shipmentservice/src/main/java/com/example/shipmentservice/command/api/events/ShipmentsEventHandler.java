package com.example.shipmentservice.command.api.events;

import com.example.commonservice.events.OrderShippedEvent;
import com.example.shipmentservice.command.api.data.Shipment;
import com.example.shipmentservice.command.api.data.ShipmentRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ShipmentsEventHandler {

    private final ShipmentRepository shipmentRepository;

    /*
     you are updating the read model as part of a CQRS (Command Query Responsibility Segregation) pattern.
     */
    @EventHandler
    public void onOrderShipped(OrderShippedEvent event){
        Shipment shipment = new Shipment();
        BeanUtils.copyProperties(event,shipment);
        shipmentRepository.save(shipment);
    }


}
