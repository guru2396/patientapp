package com.patient.patientapp.service;

import com.patient.patientapp.dto.ConsentNotificationResponse;
import com.patient.patientapp.dto.CreateConsent;
import com.patient.patientapp.dto.CreateConsentRequest;
import com.patient.patientapp.entity.Consent_request;
import com.patient.patientapp.entity.Doctor_info;
import com.patient.patientapp.entity.Hospital_info;
import com.patient.patientapp.entity.Patient_info;
import com.patient.patientapp.repository.Consent_request_repo;
import com.patient.patientapp.repository.Doctor_info_repo;
import com.patient.patientapp.repository.Hospital_info_repo;
import com.patient.patientapp.repository.Patient_info_repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatientAppService {

    @Autowired
    private Consent_request_repo consent_request_repo;

    @Autowired
    private Doctor_info_repo doctor_info_repo;

    @Autowired
    private Hospital_info_repo hospital_info_repo;

    @Autowired
    private Patient_info_repo patient_info_repo;

    @Value("${consentManager.url}")
    private String consentManagerBaseUrl;

    public List<ConsentNotificationResponse> getConsentRequests(String patientId){
        List<Consent_request> consentReqList=consent_request_repo.getConsentRequestsForPatient(patientId);
        List<ConsentNotificationResponse> consentRepList=new ArrayList<>();
        if(consentReqList!=null){
            for(Consent_request consent_request:consentReqList){
                ConsentNotificationResponse consentNotificationResponse=new ConsentNotificationResponse();
                consentNotificationResponse.setConsent_id(consent_request.getConsent_request_id());
                Doctor_info doctor =doctor_info_repo.getDoctorById(consent_request.getDoctor_id());
                Hospital_info hospital=hospital_info_repo.getHospitalById(consent_request.getHospital_id());
                consentNotificationResponse.setAccess_purpose(consent_request.getAccess_purpose());
                consentNotificationResponse.setRequest_info(consent_request.getRequest_info());
                consentNotificationResponse.setDoctor_name(doctor.getDoctor_name());
                consentNotificationResponse.setHospital_name(hospital.getHospital_name());
                consentRepList.add(consentNotificationResponse);
            }
        }
        return consentRepList;
    }

    public Patient_info getPatientById(String patientId){
        return patient_info_repo.getPatientById(patientId);
    }

    public String createConsent(CreateConsentRequest createConsentRequest,String patientId){
        CreateConsent createConsent=new CreateConsent();
        createConsent.setPatient_id(patientId);
        Consent_request consent_request=consent_request_repo.getConsentRequestById(createConsentRequest.getConsent_request_id());
        createConsent.setDoctor_id(consent_request.getDoctor_id());
        createConsent.setDataCustodianId(createConsentRequest.getDataCustodianId());
        createConsent.setDelegateAccess(createConsentRequest.getDelegateAccess());
        createConsent.setPurpose(createConsentRequest.getPurpose());
        createConsent.setSignature(createConsentRequest.getSignature());
        createConsent.setEpisodes(createConsentRequest.getEpisodes());
        createConsent.setEhr_id(createConsentRequest.getEhr_id());
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        HttpEntity<?> httpEntity=new HttpEntity<>(createConsent,headers);
        String url=consentManagerBaseUrl + "/create-consent";

        ResponseEntity<String> responseEntity=restTemplate.exchange(url, HttpMethod.POST,httpEntity,String.class);
        if(responseEntity.getStatusCode().is5xxServerError()){
            return null;
        }
        return responseEntity.getBody();

    }


}
