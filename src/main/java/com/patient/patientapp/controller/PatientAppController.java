package com.patient.patientapp.controller;

import com.patient.patientapp.service.PatientAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatientAppController {

    @Autowired
    private PatientAppService patientAppService;

    @PostMapping(value="/register-patient")
    public ResponseEntity<?> register( ){
        return null;
    }

}
