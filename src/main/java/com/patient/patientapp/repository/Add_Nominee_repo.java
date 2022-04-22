package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Nominee_info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Add_Nominee_repo extends JpaRepository<Nominee_info,String> {

    @Query(value="SELECT * FROM nominee_info WHERE nominee_email=?1",nativeQuery = true)
    Nominee_info getNomineeByEmail(String email);

    @Query(value = "SELECT * FROM nominee_info WHERE nominee_id=?1",nativeQuery = true)
    Nominee_info getNomineeById(String id);

}
