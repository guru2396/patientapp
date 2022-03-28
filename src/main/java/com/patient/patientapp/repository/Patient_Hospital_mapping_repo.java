package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Patient_Hospital_mapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Patient_Hospital_mapping_repo extends JpaRepository<Patient_Hospital_mapping,String> {

    @Query(value = "SELECT hospital_id FROM patient_hospital_mapping WHERE patient_id=?1",nativeQuery = true)
    List<String> fetchHospitalIdsByPatientId(String patientId);
}
