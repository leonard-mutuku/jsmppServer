/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.config.ApplicationProperties;
import com.smpp.config.ApplicationProperties.SMPP;
import com.smpp.utils.Constants;
import java.io.IOException;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.SMPPServerSessionListener;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.ServerResponseDeliveryListener;
import org.jsmpp.session.SessionStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 *
 * @author leonard
 */
@EnableAsync
@Service
public class SmppServerService {

    private final ApplicationProperties props;
    private final ServerMessageReceiverListener serverMessageReceiverListener;
    private final ServerResponseDeliveryListener serverResponseDeliveryListener;
    private final SessionStateListener sessionStateListener;
    private final TaskExecutor taskExecutor;

    private SMPPServerSessionListener sessionListener = null;
    private boolean running = true;

    @Autowired
    public SmppServerService(
            final ApplicationProperties properties,
            @Qualifier("serverMessageReceiverListener") final ServerMessageReceiverListenerImpl serverMessageReceiverListener,
            @Qualifier("serverResponseDeliveryListener") final ServerResponseDeliveryListenerImpl serverResponseDeliveryListener,
            @Qualifier("sessionStateListener") final SessionStateListenerImpl sessionStateListener,
            @Qualifier("smppTaskExecutor") final TaskExecutor taskExecutor
    ) {
        this.props = properties;
        this.serverMessageReceiverListener = serverMessageReceiverListener;
        this.serverResponseDeliveryListener = serverResponseDeliveryListener;
        this.sessionStateListener = sessionStateListener;
        this.taskExecutor = taskExecutor;
    }

    @Async
    public void start() {
        Constants.SMPP_LOGGER.info("Initianing SMPP service start up...");
        SMPP smpp = props.getSmpp();
        final int port = smpp.getPort();
        final int transactionTimer = smpp.getTransactionTimer();
        try {
            sessionListener = new SMPPServerSessionListener(port);
            sessionListener.setPduProcessorDegree(5);
            Constants.SMPP_LOGGER.info("SMPP service started and Listening on port {}", port);
            while (running) {
                final SMPPServerSession serverSession = sessionListener.accept();
                serverSession.addSessionStateListener(sessionStateListener);
                Constants.SMPP_LOGGER.info("Accepting connection for session {} with transaction timeout {}",
                        serverSession.getSessionId(), transactionTimer);
                serverSession.setMessageReceiverListener(serverMessageReceiverListener);
                serverSession.setResponseDeliveryListener(serverResponseDeliveryListener);
                serverSession.setTransactionTimer(transactionTimer);

                taskExecutor.execute(new WaitBindTask(serverSession, smpp.getBindTimeout(), smpp.getEnquireLinkTimer()));
            }
        } catch (final IOException e) {
            Constants.SMPP_LOGGER.error("Could not listen on port {}. Error is; {}", port, e.getMessage());
        }
    }

    public void stop() {
        Constants.SMPP_LOGGER.info("Initianing SMPP service power off...");
        try {
            running = false;
            if (sessionListener != null) {
                sessionListener.close();
                sessionListener = null;
                Constants.SMPP_LOGGER.info("SMPP service stopped successful.");
            } else {
                Constants.SMPP_LOGGER.info("SMPP service already stopped!");
            }
        } catch (final IOException e) {
            Constants.SMPP_LOGGER.error("Unable to stop listener. Error is; {}", e.getMessage());
        }
    }

}
