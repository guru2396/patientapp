package com.patient.patientapp.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="patient_login_info")
@Data
public class Patient_login_info {

    @Id
    private String patient_id;

    private String patient_name;

    private String patient_email;

    private String patient_password;
}
