package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class AccessLogsDto {
    private String  log_id;
    private String doctor_name;
    private String hospital_name;
    private String consent_id;
    //private String data_custodian_name;
    private String access_details;
    private String access_purpose;
    private String timestamp;
}
