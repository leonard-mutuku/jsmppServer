/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.utils.Constants;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.ServerResponseDeliveryListener;
import org.jsmpp.session.SubmitMultiResult;
import org.jsmpp.session.SubmitSmResult;

/**
 *
 * @author leonard
 */
public class ServerResponseDeliveryListenerImpl implements ServerResponseDeliveryListener {

    @Override
    public void onSubmitSmRespSent(SubmitSmResult submitSmResult, SMPPServerSession source) {
        Constants.SMPP_LOGGER.info("submit_sm_resp_sent with message id {} on session {}",
                submitSmResult.getMessageId(), source.getSessionId());
    }

    @Override
    public void onSubmitSmRespError(SubmitSmResult submitSmResult, Exception cause, SMPPServerSession source) {
        Constants.SMPP_LOGGER.error("submit_sm_resp_error with message id {} on session {}: {}",
                submitSmResult.getMessageId(), source.getSessionId(), cause.getMessage());
    }

    @Override
    public void onSubmitMultiRespSent(SubmitMultiResult submitMultiResult, SMPPServerSession source) {
        Constants.SMPP_LOGGER.info("submit_multi_resp_sent with message id {} on session {}",
                submitMultiResult.getMessageId(), source.getSessionId());
    }

    @Override
    public void onSubmitMultiRespError(SubmitMultiResult submitMultiResult, Exception cause, SMPPServerSession source) {
        Constants.SMPP_LOGGER.error("submit_sm_resp_error with message id {} on session {}: {}",
                submitMultiResult.getMessageId(), source.getSessionId(), cause.getMessage());
    }

}
