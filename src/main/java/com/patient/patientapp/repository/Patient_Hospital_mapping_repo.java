package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Patient_Hospital_mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Patient_Hospital_mapping_repo extends JpaRepository<Patient_Hospital_mapping,String> {
}
