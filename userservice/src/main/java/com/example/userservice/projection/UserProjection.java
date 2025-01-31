package com.example.userservice.projection;

import com.example.commonservice.model.CardDetails;
import com.example.commonservice.model.User;
import com.example.commonservice.queries.GetUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {

    @QueryHandler
    public User getUserPaymentDetails(GetUserPaymentDetailsQuery query){
        //ideally get the deails from the DB,
        //these particular details will come from order saga
        CardDetails cardDetails = CardDetails.builder()
                .name("my mastercard")
                .validUntilMonth(1)
                .validUntilYear(2022)
                .cardNumber("4545352565659898")
                .build();
        return User
                .builder()
                .userId(query.getUserId())
                .firstName("shabbir")
                .lastName("karam")
                .cardDetails(cardDetails)
                .build();
    }
}
