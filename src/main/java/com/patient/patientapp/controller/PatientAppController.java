package com.patient.patientapp.controller;

import com.patient.patientapp.dto.ConsentNotificationResponse;
import com.patient.patientapp.dto.CreateConsentRequest;
import com.patient.patientapp.dto.GetEhrResponse;
import com.patient.patientapp.entity.Consent_request;
import com.patient.patientapp.service.JwtService;
import com.patient.patientapp.service.PatientAppService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> register( ){
        return null;
    }


    @GetMapping(value="/get-consent-notifications")
    public ResponseEntity<?> getConsentRequests(@RequestHeader("Authorization") String token){
        //String patientId=jwtService.extractID(token);
        System.out.print(token);
        List<ConsentNotificationResponse> consentrequests= patientAppService.getConsentRequests(token);
        return ResponseEntity.ok(consentrequests);
    }

    @PostMapping(value = "/create-consent")
    public ResponseEntity<?> createConsent(@RequestBody CreateConsentRequest createConsentRequest,@RequestHeader("Authorization") String token){
        String patientId=token ;//jwtService.extractID(token);
        String consentId=patientAppService.createConsent(createConsentRequest,patientId);
        return ResponseEntity.ok(consentId);
    }

    @GetMapping(value = "/get-ehr")
    public ResponseEntity<?> getEhr(@RequestHeader("Authorization") String token){
        String patientId=token ;//jwtService.extractID(token);
        GetEhrResponse getEhrResponse= patientAppService.fetchEhrOfPatient(patientId);
        return ResponseEntity.ok(getEhrResponse);
    }







}
