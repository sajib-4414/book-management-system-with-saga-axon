package com.example.ordermicroservice.command.api.events;


import com.example.commonservice.events.OrderCancelledEvent;
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
        System.out.println("EventHandler received create order command ");
        System.out.println("Event details: " + event); // Log the event object

        Order order = new Order();
        BeanUtils.copyProperties(event,order);
        System.out.println("Created order object: " + order); // Log the order object

        try {
            Order savedOrder = orderRepository.save(order);
            Order dbOrder = orderRepository.findById(savedOrder.getOrderId()).get();
            System.out.println("DB fetched order: " + dbOrder);
            System.out.println("Saved order: " + savedOrder); // Log the saved order
        } catch (Exception e) {
            e.printStackTrace(); // In case there's a swallowed exception
        }
    }

    @EventHandler
    public void onOrderCompletedEvent(OrderCompletedEvent event){
        Order order = orderRepository.findById(event.getOrderId()).get();
        order.setOrderStatus(event.getOrderStatus());
        orderRepository.save(order);
    }

    @EventHandler
    public void onOrderCancelledEvent(OrderCancelledEvent event){
        Order order = orderRepository.findById(event.getOrderId()).get();
        order.setOrderStatus(event.getOrderStatus());
        orderRepository.save(order);
    }


}
