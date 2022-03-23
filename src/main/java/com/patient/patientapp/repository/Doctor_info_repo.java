package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Doctor_info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Doctor_info_repo extends JpaRepository<Doctor_info,String> {

    @Query(value = "SELECT * FROM doctor_info WHERE doctor_id=?1",nativeQuery = true)
    Doctor_info getDoctorById(String doctorId);
}
