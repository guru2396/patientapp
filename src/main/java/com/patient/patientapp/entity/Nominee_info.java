package com.patient.patientapp.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="nominee_info")
@Data
public class Nominee_info {

    @Id
    private String nominee_id;

    private String nominee_name;

    private String nominee_email;

    private String nominee_contact;

    private String nominee_code;

    private String patient_id;

    private String is_deleted;
}
