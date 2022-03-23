package com.patient.patientapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class EpisodeDetails {

    private String episodeId;

    private String time_limit_records;

    private List<EncounterDetails> encounterDetails;

}
