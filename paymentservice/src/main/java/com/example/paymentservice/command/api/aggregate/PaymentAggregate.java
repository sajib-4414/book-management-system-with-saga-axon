package com.example.paymentservice.command.api.aggregate;

import com.example.commonservice.commands.ValidatePaymentCommand;
import com.example.commonservice.events.PaymentProcessedEvent;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

//is the command handler here
@Aggregate
@NoArgsConstructor
@Slf4j
public class PaymentAggregate {

    @AggregateIdentifier
    private String paymentId;
    private String orderId;
    private String paymentStatus;

    @CommandHandler
    public PaymentAggregate(ValidatePaymentCommand validatePaymentCommand){
        //validae the payment details
        //if everything goes well, publish the payment processed event
        log.info("executing the validate payment command for order id :"+validatePaymentCommand.getOrderId()+", payment id="+validatePaymentCommand.getPaymentId());

        PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(validatePaymentCommand.getPaymentId(),validatePaymentCommand.getOrderId());
        AggregateLifecycle.apply(paymentProcessedEvent);
        log.info("payment processed event applied");
        //now we need to do event sourcing
        //we need to handle event sourcing aslo

    }

    @EventSourcingHandler
    public void onPaymentProcessed(PaymentProcessedEvent event){
        this.paymentId = event.getPaymentId();
        this.orderId = event.getOrderId();
    }
}
