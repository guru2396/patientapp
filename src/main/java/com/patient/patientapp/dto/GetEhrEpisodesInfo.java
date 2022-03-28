package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetEhrEpisodesInfo {

    private String episodeId;

    private String episodeName;

    private List<GetEhrEncounterInfo> encounters;
}
