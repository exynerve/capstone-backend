package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.LogoutResponse;
import com.upgrad.FoodOrderingApp.service.exception.*;

import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.businness.CustomerSignupService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/customer")

public class CustomerController {
   @Autowired
    private CustomerSignupService customerSignupService;

    @RequestMapping(method= RequestMethod.POST, path ="/signup", consumes=MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException{

        final CustomerEntity customerEntity = new CustomerEntity();

        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        customerEntity.setPassword(signupCustomerRequest.getPassword());

        final CustomerEntity createdCustomerEntity = customerSignupService.signup(customerEntity);
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(createdCustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED") ;
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }


    @Autowired
    private CustomerService customerService;

    @RequestMapping(method = RequestMethod.POST, path="/login", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException{

        String B= "";
        for(int i=0; i<6; i++){
           B = B+authorization.charAt(i);
        }
        if(!(B.equals("Basic "))){
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }

        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        CustomerAuthEntity customerAuth = customerService.authenticate(decodedArray[0], decodedArray[1]);

        CustomerEntity customer = customerAuth.getCustomer();

        String customerUuid = customer.getUuid();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(customerUuid);
        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");
        loginResponse.setFirstName(customer.getFirstName());
        loginResponse.setLastName(customer.getLastName());
        loginResponse.setEmailAddress(customer.getEmail());
        loginResponse.setContactNumber(customer.getContactNumber());

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuth.getAccessToken());
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);

    }

}
