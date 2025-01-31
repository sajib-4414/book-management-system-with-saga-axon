package com.example.userservice.controller;

import com.example.commonservice.model.User;
import com.example.commonservice.queries.GetUserPaymentDetailsQuery;
import lombok.AllArgsConstructor;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private transient QueryGateway queryGateway;
    @GetMapping("{userId}")
    public User getUserPaymentDetails (@PathVariable String userId){
        GetUserPaymentDetailsQuery query = new GetUserPaymentDetailsQuery();
        User user = queryGateway.query(query, ResponseTypes.instanceOf(User.class)).join();
        return user;
    }
}
