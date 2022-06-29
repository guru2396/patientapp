package com.patient.patientapp.dto;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class AccessLogsDto implements Comparable<AccessLogsDto> {
    private String  log_id;
    private String doctor_name;
    private String hospital_name;
    private String consent_id;
    //private String data_custodian_name;
    private String access_details;
    private String access_purpose;
    private String timestamp;


    private Date getTimeStampAsDate(){
        Date date=null;
        try {
            date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this.timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public int compareTo(AccessLogsDto object) {
        return this.getTimeStampAsDate().compareTo(object.getTimeStampAsDate());
    }
}
