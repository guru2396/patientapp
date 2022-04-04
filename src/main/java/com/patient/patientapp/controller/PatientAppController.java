package com.patient.patientapp.controller;

import com.patient.patientapp.dto.*;
import com.patient.patientapp.entity.Consent_request;
import com.patient.patientapp.service.JwtService;
import com.patient.patientapp.service.PatientAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"})
public class PatientAppController {

    @Autowired
    private PatientAppService patientAppService;

    @Autowired
    private JwtService jwtService;

    @PostMapping(value="/register-patient")
    public ResponseEntity<?> register(@RequestBody PatientRegistrationDto patientRegistrationDto){
        String id=patientAppService.registerPatient(patientRegistrationDto);

        return ResponseEntity.ok(id);
    }

    @PostMapping(value = "/login-patient")
    public ResponseEntity<?> loginPatient(@RequestBody AuthRequest authRequest){
        String msg= patientAppService.loginPatient(authRequest);
        if(msg==null){
            ResponseEntity<String> response=new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            return response;
        }
        return ResponseEntity.ok(msg);
    }


    @GetMapping(value="/get-consent-notifications")
    public ResponseEntity<?> getConsentRequests(@RequestHeader("Authorization") String token){
        String patientId=jwtService.extractID(token);
        System.out.print(token);
        List<ConsentNotificationResponse> consentrequests= patientAppService.getConsentRequests(patientId);
        return ResponseEntity.ok(consentrequests);
    }

    @PostMapping(value = "/create-consent")
    public ResponseEntity<?> createConsent(@RequestBody CreateConsentRequest createConsentRequest,@RequestHeader("Authorization") String token){
        String patientId=jwtService.extractID(token);//token
        String consentId=patientAppService.createConsent(createConsentRequest,patientId);
        return ResponseEntity.ok(consentId);
    }

    @GetMapping(value = "/get-ehr")
    public ResponseEntity<?> getEhr(@RequestHeader("Authorization") String token){
        String patientId=jwtService.extractID(token);
        GetEhrResponse getEhrResponse= patientAppService.fetchEhrOfPatient(patientId);
        System.out.println(getEhrResponse.getEhrId());
        System.out.println(getEhrResponse.getHospitalRecords().get(0).getEpisodes().get(0).getEpisodeName());
        return ResponseEntity.ok(getEhrResponse);
    }







}
