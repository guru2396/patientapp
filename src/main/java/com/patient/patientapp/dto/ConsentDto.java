package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class ConsentDto {

    private String consent_id;

    private String doctor_id;

    private String dataCustodianId;

    private String access_purpose;

    private String delegate_access;

    private String creation_date;

    private String validity;
}