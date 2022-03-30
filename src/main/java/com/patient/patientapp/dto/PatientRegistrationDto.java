package com.patient.patientapp.dto;

import lombok.Data;

@Data
public class PatientRegistrationDto {

    private String patient_name;
    private String patient_contact;
    private String patient_email;
    private String patient_dob;
    private String patient_address;
    private String patient_gender;
    private String patient_emergency_contact;
    private String patient_emergency_contact_name;
    private String patient_govtid_type;
    private String patient_govtid;
    private String patient_password;


}
