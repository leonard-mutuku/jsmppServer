/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.utils.Constants;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.Session;
import org.jsmpp.session.SessionStateListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author leonard
 */
public class SessionStateListenerImpl implements SessionStateListener {

    @Autowired
    private SynchronizedSession syncSession;

    @Override
    public void onStateChange(SessionState newState, SessionState oldState, Session source) {
        String sessionId = source.getSessionId();
        Constants.SMPP_LOGGER.info("Session {} changed from {} to {}", sessionId, oldState, newState);

        syncSession.synSession(newState, sessionId);
    }
}
