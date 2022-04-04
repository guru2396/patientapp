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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private JwtService jwtService;

    @Value("${consentManager.url}")
    private String consentManagerBaseUrl;

    @Value("${consentmanager.client.id}")
    private String consentManagerClientId;

    @Value("${consentmanager.client.secret}")
    private String consentManagerClientSecret;

    private String consentToken;

    private Map<String,String> tokenMap=new HashMap<>();

    private PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    public String registerPatient(PatientRegistrationDto patientRegistrationDto){
        Patient_info patient=patient_info_repo.getPatientByEmail(patientRegistrationDto.getPatient_email());
        if(patient==null){
            Patient_info patient_info=new Patient_info();
            patient_info.setPatient_name(patientRegistrationDto.getPatient_name());
            patient_info.setPatient_contact(patientRegistrationDto.getPatient_contact());
            long id=generateID();
            String patientId="PAT_" + id;
            patient_info.setPatient_id(patientId); // String id="PAT_"+UUID.randomUUID().toString();

            patient_info.setPatient_email(patientRegistrationDto.getPatient_email());
            patient_info.setPatient_dob(patientRegistrationDto.getPatient_dob());
            patient_info.setPatient_address(patientRegistrationDto.getPatient_address());
            patient_info.setPatient_gender(patientRegistrationDto.getPatient_gender());
            patient_info.setPatient_emergency_contact(patientRegistrationDto.getPatient_emergency_contact());
            patient_info.setPatient_emergency_contact_name(patientRegistrationDto.getPatient_emergency_contact_name());
            patient_info.setPatient_govtid_type(patientRegistrationDto.getPatient_govtid_type());
            patient_info.setPatient_govtid(patientRegistrationDto.getPatient_govtid());

            String hash_password = passwordEncoder.encode(patientRegistrationDto.getPatient_password());
            patient_info.setPatient_password(hash_password); //saving hashed password in database;

            //patient_info.setPatient_password(patientRegistrationDto.getPatient_password());

            //boolean matched = passwordEncoder.matches(plaintextpassword in string, hashedpassword from database);
            try{
                patient_info_repo.save(patient_info);
            }catch(Exception e){
                return null;
            }
            return patient_info.getPatient_id(); //returns this id to PatientAppController
        }
        else{
            return null;
        }

    }

    public String loginPatient(AuthRequest authRequest){
        Patient_info patient_info=patient_info_repo.getPatientByEmail(authRequest.getUsername());
        if(patient_info!=null){
            boolean isMatch=passwordEncoder.matches(authRequest.getPassword(),patient_info.getPatient_password());
            if(isMatch){
                String token=jwtService.createToken(patient_info.getPatient_id());
                return token;
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

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
        List<EpisodeDetails> episodes=formCreateConsentRequest(createConsentRequest.getSelectedRecords());
        createConsent.setEpisodes(episodes);
       // createConsent.setEpisodes(createConsentRequest.getEpisodes());
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

    private List<EpisodeDetails> formCreateConsentRequest(List<SelectedRecords> selectedRecords){
        List<EpisodeDetails> episodes=new ArrayList<>();
        Map<String,List<String>> map=new HashMap<>();
        for(SelectedRecords selectedRecord:selectedRecords){
            List<String> l=map.getOrDefault(selectedRecord.getEpisodeId(),new ArrayList<>());
            l.add(selectedRecord.getEncounterId());
            map.put(selectedRecord.getEpisodeId(),l);
        }
        for(String episodeId:map.keySet()){
            EpisodeDetails episodeDetails=new EpisodeDetails();
            episodeDetails.setEpisodeId(episodeId);
            List<String> encounterIdList=map.get(episodeId);
            List<EncounterDetails> encounterDetails=new ArrayList<>();
            for(String encounterId:encounterIdList){
                EncounterDetails encounter=new EncounterDetails();
                encounter.setEncounterId(encounterId);
                encounterDetails.add(encounter);
            }
            episodeDetails.setEncounterDetails(encounterDetails);
            episodeDetails.setTime_limit_records("");
            episodes.add(episodeDetails);
        }
        return episodes;
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

    public long generateID(){
        long id=(long) Math.floor(Math.random()*9_000_000_000L)+1_000_000_000L;
        return id;
    }


}
