package com.example.commonservice.commands;

import com.example.commonservice.model.CardDetails;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidatePaymentCommand {
    private String paymentId;
    private String orderId;
    private CardDetails cardDetails;
}
