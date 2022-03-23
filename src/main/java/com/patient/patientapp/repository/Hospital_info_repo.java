package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Hospital_info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Hospital_info_repo extends JpaRepository<Hospital_info,String> {

    @Query(value = "SELECT * FROM hospital_info WHERE hospital_id=?1",nativeQuery = true)
    Hospital_info getHospitalById(String hospitalId);
}
