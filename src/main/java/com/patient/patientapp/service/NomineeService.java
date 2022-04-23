package com.patient.patientapp.service;

import com.patient.patientapp.dto.AddNomineeDto;
import com.patient.patientapp.dto.AddNomineeResponseDto;
import com.patient.patientapp.dto.AuthRequest;
import com.patient.patientapp.entity.Nominee_info;
import com.patient.patientapp.repository.Add_Nominee_repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NomineeService {

    @Autowired
    private Add_Nominee_repo add_nominee_repo;

    @Autowired
    private JwtService jwtService;

    public AddNomineeResponseDto addNominee(AddNomineeDto addNomineeDto, String patientId){
        Nominee_info nominee=add_nominee_repo.getNomineeByEmail(addNomineeDto.getNominee_email());
        if(nominee==null){
            Nominee_info nominee_info = new Nominee_info();
            nominee_info.setNominee_name(addNomineeDto.getNominee_name());
            nominee_info.setNominee_email(addNomineeDto.getNominee_email());
            nominee_info.setNominee_contact(addNomineeDto.getNominee_contact());
            nominee_info.setPatient_id((patientId));
            String code=String.valueOf(generateID());
            nominee_info.setNominee_code(code);
            nominee_info.setIs_deleted("N");
            long id=generateID();
            String nomineeId="NOM_"+id;
            nominee_info.setNominee_id(nomineeId);
            add_nominee_repo.save(nominee_info);
            AddNomineeResponseDto addNomineeResponseDto=new AddNomineeResponseDto();
            addNomineeResponseDto.setNominee_id(nomineeId);
            addNomineeResponseDto.setNominee_code(code);
            return addNomineeResponseDto;
        }
        else{
            return null;
        }

    }

    public String loginNominee(AuthRequest authRequest){
        Nominee_info nominee_info=add_nominee_repo.getNomineeByEmail(authRequest.getUsername());
        if(nominee_info!=null){
            boolean isMatch=authRequest.getPassword().equals(nominee_info.getNominee_code());
            if(isMatch){
                String token=jwtService.createToken(nominee_info.getNominee_id());
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

    public Nominee_info getNomineeById(String id){
        return add_nominee_repo.getNomineeById(id);
    }

    public long generateID(){
        long id=(long) Math.floor(Math.random()*9_000_000_000L)+1_000_000_000L;
        return id;
    }
}
