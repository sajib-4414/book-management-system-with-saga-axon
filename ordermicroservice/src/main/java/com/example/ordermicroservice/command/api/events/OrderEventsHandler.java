package com.example.ordermicroservice.command.api.events;


import com.example.commonservice.events.OrderCompletedEvent;
import com.example.ordermicroservice.command.api.data.Order;
import com.example.ordermicroservice.command.api.data.OrderRepository;
import lombok.AllArgsConstructor;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
//this is the repository/readmodel side of cqrs, after we see message that order created
public class OrderEventsHandler {

    private final OrderRepository orderRepository;

    @EventHandler
    public void onOrderCreatedEvent(OrderCreatedEvent event){
        Order order = new Order();
        BeanUtils.copyProperties(event,order);
        orderRepository.save(order);
    }

    @EventHandler
    public void onOrderCompletedEvent(OrderCompletedEvent event){
        Order order = orderRepository.findById(event.getOrderId()).get();
        order.setOrderStatus(event.getOrderStatus());
        orderRepository.save(order);
    }


}
