package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Patient_login_info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Patient_login_info_repo extends JpaRepository<Patient_login_info,String> {

    @Query(value="SELECT * FROM patient_login_info WHERE patient_email=?1 and is_verified='Y'",nativeQuery = true)
    Patient_login_info getLoginInfoByEmail(String email);

    @Query(value="SELECT * FROM patient_login_info WHERE patient_id=?1",nativeQuery = true)
    Patient_login_info getLoginInfoById(String id);
}
