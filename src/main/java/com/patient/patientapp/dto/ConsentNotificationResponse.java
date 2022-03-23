package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class ConsentNotificationResponse {

    private String consent_id;

    private String doctor_name;

    private String hospital_name;

    private String request_info;

    private String access_purpose;

}
