package com.example.commonservice.events;

import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
public class OrderCancelledEvent {

    private String orderId;
    private String orderStatus;
}
