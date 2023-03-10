/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 *
 * @author leonard
 */
@Component
@ConfigurationProperties(prefix = "configs")
public class ApplicationProperties {

    @NestedConfigurationProperty
    private final SMPP smpp = new SMPP();
    @NestedConfigurationProperty
    private final Client client = new Client();

    public SMPP getSmpp() {
        return smpp;
    }

    public Client getClient() {
        return client;
    }

    @Getter
    @Setter
    public static class SMPP {

        private String host;
        private String userId;
        private String password;
        private int port = 2775;
        private Long bindTimeout = 20000L;
        private Integer enquireLinkTimer = 30000;
        private Integer transactionTimer = 20000;

        @Override
        public String toString() {
            return """
                   JSMPP Bind Configuration
                   {
                   \tJSMPP Host = """ + host + "\n"
                    + "\tJSMPP Username = " + userId + "\n"
                    + "\tJSMPP Password = " + password + "\n"
                    + "\tJSMPP Port = " + port + "\n"
                    + "\tJSMPP bind Timeout = " + bindTimeout + "\n"
                    + "\tJSMPP enquire Link Timer = " + enquireLinkTimer + "\n"
                    + "\tJSMPP transaction Timer = " + transactionTimer + "\n}";
        }
    }

    @Getter
    @Setter
    public static class Client {

        private int corePoolSize = 10;
        private int maxPoolSize = 100;
        private int poolQueueCapacity = 250;

        @Override
        public String toString() {
            return """
                   SMPP Client Configuration
                   {
                   \tExecutor core pool size = """ + corePoolSize + "\n"
                    + "\tExecutor max pool size = " + maxPoolSize + "\n"
                    + "\tExecutor pool Queue Capacity = " + poolQueueCapacity + "\n}";
        }
    }

}
