package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateConsent {

    private String patient_id;

    private String doctor_id;

    private String dataCustodianId;

    private String ehr_id;

    private List<EpisodeDetails> episodes;

    private String purpose;

    private String delegateAccess;

    private String signature;
}
