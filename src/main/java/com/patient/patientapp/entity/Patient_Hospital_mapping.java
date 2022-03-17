package com.patient.patientapp.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="patient_hospital_mapping")
@Data
public class Patient_Hospital_mapping {

    @Id
    private String mapping_id;

    private String patient_id;

    private String hospital_id;


}
