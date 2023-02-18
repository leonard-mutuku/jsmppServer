/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.utils.Constants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jsmpp.extra.SessionState;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 *
 * @author leonard
 */
@EnableAsync
@Component
public class SynchronizedSession {

    private final List<String> syncSessions = Collections.synchronizedList(new ArrayList<>());

    public void synSession(SessionState newState, String sessionId) {
        synchronized (syncSessions) {
            if (newState.equals(SessionState.BOUND_TX) || newState.equals(SessionState.BOUND_RX) || newState.equals(SessionState.BOUND_TRX)) {
                syncSessions.add(sessionId);
                Constants.SMPP_LOGGER.info("session {} added. New session count is {}. \nSessions => {}",
                        sessionId, syncSessions.size(), syncSessions);
            } else if (newState.equals(SessionState.CLOSED)) {
                syncSessions.removeIf(s-> s.equals(sessionId));
                Constants.SMPP_LOGGER.info("session {} closed. New session count is {}. \nSessions => {}",
                        sessionId, syncSessions.size(), syncSessions);
            }
        }
    }

}
