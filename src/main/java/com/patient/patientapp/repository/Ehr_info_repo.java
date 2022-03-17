package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Ehr_info;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Ehr_info_repo extends JpaRepository<Ehr_info,String> {
}
