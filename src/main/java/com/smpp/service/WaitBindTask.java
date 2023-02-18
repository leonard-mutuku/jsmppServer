/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.utils.Constants;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jsmpp.PDUStringException;
import org.jsmpp.SMPPConstant;
import org.jsmpp.bean.InterfaceVersion;
import org.jsmpp.session.BindRequest;
import org.jsmpp.session.SMPPServerSession;

/**
 *
 * @author leonard
 */
public class WaitBindTask implements Runnable {

    private final SMPPServerSession serverSession;
    private final long timeout;
    private final int enquireLinkTimer;

    public WaitBindTask(SMPPServerSession serverSession, final long timeout, final int enquireLinkTimer) {
        this.serverSession = serverSession;
        this.timeout = timeout;
        this.enquireLinkTimer = enquireLinkTimer;
    }

    @Override
    public void run() {
        try {
            Constants.SMPP_LOGGER.info("Wait for bind request on session {} (timeout {})",
                    serverSession.getSessionId(), timeout);
            final BindRequest bindRequest = serverSession.waitForBind(timeout);
            Constants.SMPP_LOGGER.info("Accepting bind for session {}, interface version {}",
                    serverSession.getSessionId(), bindRequest.getInterfaceVersion());
            try {
                bindRequest.accept("sys", InterfaceVersion.IF_50);
            } catch (PDUStringException e) {
                Constants.SMPP_LOGGER.error("PDU string exception is; {}", e.getMessage());
                bindRequest.reject(SMPPConstant.STAT_ESME_RSYSERR);
            }
            serverSession.setEnquireLinkTimer(enquireLinkTimer);
        } catch (final IllegalStateException | TimeoutException | IOException e) {
            Constants.SMPP_LOGGER.error("WaitBindTask Failed!. Error is; {}", e.getMessage());
        }
    }
    
}
