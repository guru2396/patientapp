package com.patient.patientapp.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ConsentRequestDto {

    private String consent_request_id;

    private String patient_id;

    private String doctor_id;

    private String hospital_id;

    private String request_info;

    private String access_purpose;

    private String request_status;

    private Date created_dt;
}
