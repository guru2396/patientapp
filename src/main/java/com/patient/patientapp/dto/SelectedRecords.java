package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class SelectedRecords {

    private String hospitalId;

    private String episodeId;

    private String encounterId;
}
