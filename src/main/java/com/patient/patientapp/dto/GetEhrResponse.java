package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetEhrResponse {

    private String ehrId;

    private List<GetEhrHospitalRecords> hospitalRecords;
}
