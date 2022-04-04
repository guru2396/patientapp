package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateConsentRequest {

    private String consent_request_id;

    private String dataCustodianId;

    private String ehr_id;

    private List<SelectedRecords> selectedRecords;

    private String purpose;

    private String delegateAccess;

    private String signature;
}
