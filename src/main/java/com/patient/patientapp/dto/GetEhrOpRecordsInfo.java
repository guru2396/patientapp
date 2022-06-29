package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class GetEhrOpRecordsInfo {

    private String op_record_id;

    private String diagnosis;

    private String recordDetails;

    private String timestamp;
}
