package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class AddNomineeDto {
    private String nominee_name;

    private String nominee_email;

    private String nominee_contact;
}
