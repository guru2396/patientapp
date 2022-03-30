package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetEhrHospitalRecords {

    private String hospitalId;

    private String hospitalName;

    private List<GetEhrEpisodesInfo> episodes;
}
