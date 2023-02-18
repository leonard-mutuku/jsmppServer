/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.config;

import com.smpp.service.SmppServerService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 *
 * @author leonard
 */
@Component
@Configuration
public class serverListener {
    
    @Autowired
    private SmppServerService service;
    
    @PostConstruct
    public void initiateSmppServer() {
        service.start();
    }
    
    @PreDestroy
    public void destroySmppServer() {
        service.stop();
    }   
    
}
