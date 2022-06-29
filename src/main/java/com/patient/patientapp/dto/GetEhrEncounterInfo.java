package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetEhrEncounterInfo {

    private String encounterId;

    private String doctorName;

    private List<GetEhrOpRecordsInfo> op_records;
}
