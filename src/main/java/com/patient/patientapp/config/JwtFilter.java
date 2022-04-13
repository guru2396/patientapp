package com.patient.patientapp.config;


import com.patient.patientapp.entity.Patient_login_info;
import com.patient.patientapp.service.JwtService;
import com.patient.patientapp.service.PatientAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PatientAppService patientAppService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth=request.getHeader("Authorization");
        if(auth!=null && !"".equals(auth) && auth.startsWith("Bearer ")){
            String subject=jwtService.extractID(auth);
            if(subject!=null && SecurityContextHolder.getContext().getAuthentication()==null){
                if(subject.startsWith("PAT_")){
                    Patient_login_info patient=patientAppService.getPatientById(subject);
                    if(patient!=null){
                        UsernamePasswordAuthenticationToken ut=new UsernamePasswordAuthenticationToken(patient,null,null);
                        ut.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(ut);
                    }

                }
            }

        }
        filterChain.doFilter(request,response);
    }
}
