/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.config;

import com.smpp.config.ApplicationProperties.Client;
import com.smpp.service.ServerMessageReceiverListenerImpl;
import com.smpp.service.ServerResponseDeliveryListenerImpl;
import com.smpp.service.SessionStateListenerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 *
 * @author leonard
 */
@Configuration
public class SmppBeanConfig {

    @Autowired
    private ApplicationProperties props;

    @Bean
    @Qualifier("serverMessageReceiverListener")
    public ServerMessageReceiverListenerImpl getServerMessageReceiverListener() {
        return new ServerMessageReceiverListenerImpl();
    }

    @Bean
    @Qualifier("serverResponseDeliveryListener")
    public ServerResponseDeliveryListenerImpl getServerResponseDeliveryListener() {
        return new ServerResponseDeliveryListenerImpl();
    }

    @Bean
    @Qualifier("sessionStateListener")
    public SessionStateListenerImpl getSessionStateListener() {
        return new SessionStateListenerImpl();
    }

    @Bean
    @Qualifier("smppTaskExecutor")
    public TaskExecutor getSmppTaskExecutor() {
        Client config = props.getClient();
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(config.getCorePoolSize());
        threadPoolTaskExecutor.setMaxPoolSize(config.getMaxPoolSize());
        threadPoolTaskExecutor.setQueueCapacity(config.getPoolQueueCapacity());
        threadPoolTaskExecutor.setThreadNamePrefix("smpp-task-");
        return threadPoolTaskExecutor;
    }

}
