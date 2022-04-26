package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsentUIDto {

    private String consent_id;

    private String doctor_name;

    private String hospital_name;

    private String access_purpose;

    private String delegate_access;

    private String creation_date;

    private String validity;

    private List<DataCustodian> dataCustodians;
}
