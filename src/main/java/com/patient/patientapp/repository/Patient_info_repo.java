package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Patient_info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Patient_info_repo extends JpaRepository<Patient_info,String> {
}
