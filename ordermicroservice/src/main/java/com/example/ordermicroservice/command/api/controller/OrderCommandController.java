package com.example.ordermicroservice.command.api.controller;

import com.example.ordermicroservice.command.api.command.CreateOrderCommand;
import com.example.ordermicroservice.command.api.data.Order;
import com.example.ordermicroservice.command.api.data.OrderRepository;
import com.example.ordermicroservice.command.api.model.OrderRestModel;
import lombok.AllArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderCommandController {

    private CommandGateway commandGateway;
    private OrderRepository orderRepository;

    @PostMapping
    public String createOrder(@RequestBody OrderRestModel orderRestModel){
        System.out.println("got order request"+orderRestModel);
        String orderId = UUID.randomUUID().toString();
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .userId(orderRestModel.getUserId())
                .orderId(orderId)
                .addressId(orderRestModel.getAddressId())
                .productId(orderRestModel.getProductId())
                .quantity(orderRestModel.getQuantity())
                .orderStatus("CREATED")
                        .build();
        commandGateway.sendAndWait(createOrderCommand);
        return "order created";
    }
    @GetMapping
    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }
}
