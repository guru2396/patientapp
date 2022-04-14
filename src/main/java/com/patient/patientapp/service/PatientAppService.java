package com.patient.patientapp.service;

import com.patient.patientapp.dto.*;
import com.patient.patientapp.entity.*;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientAppService {

    @Autowired
    private Patient_login_info_repo patient_login_info_repo;

    @Autowired
    private Environment environment;

    @Autowired
    private JwtService jwtService;

    @Value("${consentManager.url}")
    private String consentManagerBaseUrl;

    @Value("${consentmanager.client.id}")
    private String consentManagerClientId;

    @Value("${consentmanager.client.secret}")
    private String consentManagerClientSecret;

    @Value("${centraldbserver.url}")
    private String centraldbServerUrl;

    @Value("${centraldbserver.clientId}")
    private String centraldbServerClientId;

    @Value("${centraldbserver.clientSecret}")
    private String centraldbServerClientSecret;

    private String consentToken;

    private String centralServerToken;

    private Map<String,String> tokenMap=new HashMap<>();

    private PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    public String registerPatient(PatientRegistrationDto patientRegistrationDto){


            String hash_password = passwordEncoder.encode(patientRegistrationDto.getPatient_password());
            patientRegistrationDto.setPatient_password(hash_password);
             //saving hashed password in database;

            //patient_info.setPatient_password(patientRegistrationDto.getPatient_password());

            //boolean matched = passwordEncoder.matches(plaintextpassword in string, hashedpassword from database);
            String url=centraldbServerUrl + "/register-patient";
            RestTemplate restTemplate=new RestTemplate();
            HttpHeaders headers=new HttpHeaders();
            String token=getCentralServerToken();
            token="Bearer " + token;
            List<String> l=new ArrayList<>();
            l.add(token);
            headers.put("Authorization",l);
            HttpEntity<?> httpEntity=new HttpEntity<>(patientRegistrationDto,headers);
            ResponseEntity<String> response=restTemplate.exchange(url,HttpMethod.POST,httpEntity,String.class);
            if(response.getStatusCodeValue()==200){
                Patient_login_info patient_login_info=new Patient_login_info();
                patient_login_info.setPatient_id(response.getBody());
                patient_login_info.setPatient_email(patientRegistrationDto.getPatient_email());
                patient_login_info.setPatient_password(hash_password);
                patient_login_info.setPatient_name(patientRegistrationDto.getPatient_name());
                patient_login_info_repo.save(patient_login_info);
                return response.getBody();
            }
            /*try{
                patient_info_repo.save(patient_info);
            }catch(Exception e){
                return null;
            }*/
            //return patient_info.getPatient_id(); //returns this id to PatientAppController
            return null;
    }

    public String loginPatient(AuthRequest authRequest){
        Patient_login_info patient_info=patient_login_info_repo.getLoginInfoByEmail(authRequest.getUsername());
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
        String url=centraldbServerUrl + "/get-consent-notifications" + "/" + patientId;
        RestTemplate restTemplate=new RestTemplate();
        String token=getCentralServerToken();
        token="Bearer " + token;
        List<String> l=new ArrayList<>();
        l.add(token);
        HttpHeaders headers=new HttpHeaders();
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);
        ResponseEntity<List<ConsentNotificationResponse>> response=restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<ConsentNotificationResponse>>() {
        });
        if(response.getStatusCodeValue()==200){
            return response.getBody();
        }
        /*List<Consent_request> consentReqList=consent_request_repo.getConsentRequestsForPatient(patientId);
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
        }*/
        return null;
    }

    public Patient_login_info getPatientById(String patientId){
        return patient_login_info_repo.getLoginInfoById(patientId);
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

    private String getCentralServerToken(){
        if(centralServerToken==null){
            AuthRequest authRequest=new AuthRequest();
            authRequest.setUsername(centraldbServerClientId);
            authRequest.setPassword(centraldbServerClientSecret);
            RestTemplate restTemplate=new RestTemplate();
            HttpHeaders headers=new HttpHeaders();
            HttpEntity<?> httpEntity=new HttpEntity<>(authRequest,headers);
            String url=centraldbServerUrl + "/patientapp-authenticate";
            ResponseEntity<String> response=restTemplate.exchange(url,HttpMethod.POST,httpEntity,String.class);
            centralServerToken=response.getBody();
        }
        return centralServerToken;
    }

    public String createConsent(CreateConsentRequest createConsentRequest,String patientId){
        CreateConsent createConsent=new CreateConsent();
        createConsent.setPatient_id(patientId);
        ConsentRequestDto consent_request=getConsentRequestById(createConsentRequest.getConsent_request_id());
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
        updateConsentRequest(createConsentRequest.getConsent_request_id());
        return responseEntity.getBody();

    }

    private ConsentRequestDto getConsentRequestById(String id){
        RestTemplate restTemplate=new RestTemplate();
        String token=getCentralServerToken();
        List<String> l=new ArrayList<>();
        token="Bearer " + token;
        l.add(token);
        HttpHeaders headers=new HttpHeaders();
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);

        String url=centraldbServerUrl + "/get-consentrequest/" + id;
        ResponseEntity<ConsentRequestDto> response=restTemplate.exchange(url,HttpMethod.GET,httpEntity,ConsentRequestDto.class);
        return response.getBody();
    }

    private void updateConsentRequest(String id){
        RestTemplate restTemplate=new RestTemplate();
        String token=getCentralServerToken();
        token="Bearer " + token;
        List<String> l=new ArrayList<>();
        l.add(token);
        HttpHeaders headers=new HttpHeaders();
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);
        String url=centraldbServerUrl + "/update-consentrequest/" + id;
        ResponseEntity<String> response=restTemplate.exchange(url,HttpMethod.POST,httpEntity,String.class);
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
        List<String> hospitalIds= fetchHospitalsForPatient(patientId);
        System.out.println(hospitalIds.get(0));
        GetEhrResponse getEhrResponse=new GetEhrResponse();
        List<GetEhrHospitalRecords> hospitalRecords=new ArrayList<>();
        if(hospitalIds!=null && hospitalIds.size()>0){
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
                String hospitalName=fetchHospitalById(hospitalId).getHospital_name();
                getEhrHospitalRecords.setHospitalName(hospitalName);
                getEhrHospitalRecords.setHospitalId(hospitalId);
                getEhrHospitalRecords.setEpisodes(response.getBody());
                hospitalRecords.add(getEhrHospitalRecords);
            }
            getEhrResponse.setHospitalRecords(hospitalRecords);
            getEhrResponse.setEhrId(fetchEhrIdByPatientId(patientId));
        }
        return getEhrResponse;
    }

    private HospitalDto fetchHospitalById(String id){
        RestTemplate restTemplate=new RestTemplate();
        String token=getCentralServerToken();
        List<String> l=new ArrayList<>();
        token="Bearer " + token;
        l.add(token);
        HttpHeaders headers=new HttpHeaders();
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);
        String url=centraldbServerUrl + "/get-hospital/" + id;
        ResponseEntity<HospitalDto> response=restTemplate.exchange(url,HttpMethod.GET,httpEntity,HospitalDto.class);
        return response.getBody();
    }

    private String fetchEhrIdByPatientId(String id){
        RestTemplate restTemplate=new RestTemplate();
        String token=getCentralServerToken();
        token="Bearer " + token;
        List<String> l=new ArrayList<>();
        l.add(token);
        HttpHeaders headers=new HttpHeaders();
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);
        String url=centraldbServerUrl + "/get-ehrId/" + id;
        ResponseEntity<String> response=restTemplate.exchange(url,HttpMethod.GET,httpEntity,String.class);
        return response.getBody();
    }

    private List<String> fetchHospitalsForPatient(String patientId){
        RestTemplate restTemplate=new RestTemplate();
        String token=getCentralServerToken();
        token="Bearer " + token;
        List<String> l=new ArrayList<>();
        l.add(token);
        HttpHeaders headers=new HttpHeaders();
        headers.put("Authorization",l);
        HttpEntity<?> httpEntity=new HttpEntity<>(headers);
        String url=centraldbServerUrl + "/get-hospitals/" + patientId;
        ResponseEntity<List<String>> response=restTemplate.exchange(url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<String>>() {
        });
        return response.getBody();
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

    public List<AccessLogsDto> fetchAccessLogs(String patientId){
        List<String> hospitals=fetchHospitalsForPatient(patientId);
        List<AccessLogsDto> accessLogsDtoList=new ArrayList<>();
        for (String hospital: hospitals) {
            RestTemplate restTemplate=new RestTemplate();
            String token=getCentralServerToken();
            token="Bearer " + token;
            List<String> l=new ArrayList<>();
            l.add(token);
            HttpHeaders headers=new HttpHeaders();
            headers.put("Authorization",l);
            HttpEntity<?> httpEntity=new HttpEntity<>(headers);
            String envURL=environment.getProperty(hospital+".accessLog.url");
            envURL=envURL +"/" +patientId;
            //String finalurl=url + "/dummyAPI/" + patientId;
            ResponseEntity<List<AccessLogsDto>> response=restTemplate.exchange(envURL, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<AccessLogsDto>>() {
            });
            accessLogsDtoList.addAll(response.getBody());
        }

        return accessLogsDtoList;
    }

    public long generateID(){
        long id=(long) Math.floor(Math.random()*9_000_000_000L)+1_000_000_000L;
        return id;
    }


}
