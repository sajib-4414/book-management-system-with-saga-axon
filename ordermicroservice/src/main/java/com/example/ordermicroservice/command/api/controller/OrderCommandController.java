package com.example.ordermicroservice.command.api.controller;

import com.example.ordermicroservice.command.api.command.CreateOrderCommand;
import com.example.ordermicroservice.command.api.model.OrderRestModel;
import lombok.AllArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderCommandController {

    private CommandGateway commandGateway;

    @PostMapping
    public String createOrder(@RequestBody OrderRestModel orderRestModel){
        String orderId = UUID.randomUUID().toString();
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .orderId(orderId)
                .addressId(orderRestModel.getAddressId())
                .productId(orderRestModel.getProductId())
                .quantity(orderRestModel.getQuantity())
                .orderStatus("CREATED")
                        .build();
        commandGateway.sendAndWait(createOrderCommand);
        return "order created";
    }
}
