package com.patient.patientapp.repository;

import com.patient.patientapp.entity.Consent_request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Consent_request_repo extends JpaRepository<Consent_request,String> {

    @Query(value="SELECT * FROM consent_request WHERE patient_id=?1 AND request_status='Pending'",nativeQuery = true)
    List<Consent_request> getConsentRequestsForPatient(String patientId);

    @Query(value = "SELECT * FROM consent_request WHERE consent_request_id=?1",nativeQuery = true)
    Consent_request getConsentRequestById(String id);
}
