package com.patient.patientapp.service;

import com.patient.patientapp.dto.*;
import com.patient.patientapp.entity.Consent_request;
import com.patient.patientapp.entity.Doctor_info;
import com.patient.patientapp.entity.Hospital_info;
import com.patient.patientapp.entity.Patient_info;
import com.patient.patientapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private Patient_Hospital_mapping_repo patient_hospital_mapping_repo;

    @Autowired
    private Environment environment;

    @Autowired
    private Ehr_info_repo ehr_info_repo;

    @Value("${consentManager.url}")
    private String consentManagerBaseUrl;

    @Value("${consentmanager.client.id}")
    private String consentManagerClientId;

    @Value("${consentmanager.client.secret}")
    private String consentManagerClientSecret;

    private String consentToken;

    private Map<String,String> tokenMap=new HashMap<>();

    public List<ConsentNotificationResponse> getConsentRequests(String patientId){
        List<Consent_request> consentReqList=consent_request_repo.getConsentRequestsForPatient(patientId);
        List<ConsentNotificationResponse> consentRepList=new ArrayList<>();
        if(consentReqList!=null){
            for(Consent_request consent_request:consentReqList){
                ConsentNotificationResponse consentNotificationResponse=new ConsentNotificationResponse();
                consentNotificationResponse.setConsent_request_id(consent_request.getConsent_request_id());
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

    private String getConsentToken(){
        if(consentToken==null){
            AuthRequest authRequest=new AuthRequest();
            authRequest.setUsername(consentManagerClientId);
            authRequest.setPassword(consentManagerClientSecret);
            RestTemplate restTemplate=new RestTemplate();
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<?> httpEntity=new HttpEntity<>(authRequest,headers);
            String url=consentManagerBaseUrl + "/patient-authenticate";
            ResponseEntity<String> response=restTemplate.exchange(url,HttpMethod.POST,httpEntity,String.class);
            consentToken=response.getBody();
        }
        return consentToken;
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
        String token=getConsentToken();
        token="Bearer " + token;
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        List<String> l=new ArrayList<>();
        l.add(token);
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(createConsent,headers);
        String url=consentManagerBaseUrl + "/create-consent";

        ResponseEntity<String> responseEntity=restTemplate.exchange(url, HttpMethod.POST,httpEntity,String.class);
        if(responseEntity.getStatusCode().is5xxServerError()){
            return null;
        }
        consent_request.setRequest_status("Completed");
        consent_request_repo.save(consent_request);
        return responseEntity.getBody();

    }

    public GetEhrResponse fetchEhrOfPatient(String patientId){
        List<String> hospitalIds= patient_hospital_mapping_repo.fetchHospitalIdsByPatientId(patientId);
        System.out.println(hospitalIds.get(0));
        GetEhrResponse getEhrResponse=new GetEhrResponse();
        List<GetEhrHospitalRecords> hospitalRecords=new ArrayList<>();
        if(hospitalIds!=null){
            for(String hospitalId:hospitalIds){
                String tokenKey=hospitalId + ".authenticate.url";
                String key=hospitalId + ".getehr.url";
                String url=environment.getProperty(key);
                String tokenUrl=environment.getProperty(tokenKey);
                System.out.println(tokenUrl);
                String token=null;
                if(!tokenMap.containsKey(hospitalId)){
                    String secret=environment.getProperty(hospitalId + ".secret");
                    token=fetchHospitalToken(tokenUrl,patientId,secret);
                    tokenMap.put(hospitalId,token);
                }
                else{
                    token=tokenMap.get(hospitalId);
                }
                url=url + "/" +patientId;
                ResponseEntity<List<GetEhrEpisodesInfo>> response=fetchEhrFromHospital(url,token);
                GetEhrHospitalRecords getEhrHospitalRecords=new GetEhrHospitalRecords();
                String hospitalName=hospital_info_repo.getHospitalById(hospitalId).getHospital_name();
                getEhrHospitalRecords.setHospitalName(hospitalName);
                getEhrHospitalRecords.setHospitalId(hospitalId);
                getEhrHospitalRecords.setEpisodes(response.getBody());
                hospitalRecords.add(getEhrHospitalRecords);
            }
            getEhrResponse.setHospitalRecords(hospitalRecords);
            getEhrResponse.setEhrId(ehr_info_repo.getEhrIdByPatientId(patientId));
        }
        return getEhrResponse;
    }

    private ResponseEntity<List<GetEhrEpisodesInfo>> fetchEhrFromHospital(String url,String token){
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        token="Bearer " + token;
        List<String> l=new ArrayList<>();
        l.add(token);
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);
        ResponseEntity<List<GetEhrEpisodesInfo>> response=restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<GetEhrEpisodesInfo>>() {
        });
        return response;
    }

    private String fetchHospitalToken(String url,String patientId,String secret){
        System.out.println("Calling token api of hospital");
        AuthRequest authRequest=new AuthRequest();
        authRequest.setUsername(patientId);
        authRequest.setPassword(secret);
        RestTemplate restTemplate=new RestTemplate();
        HttpHeaders headers=new HttpHeaders();
        HttpEntity<?> httpEntity=new HttpEntity<>(authRequest,headers);
        ResponseEntity<String> response=restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        String token=response.getBody();
        return token;
    }


}
