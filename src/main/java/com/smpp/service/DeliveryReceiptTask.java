/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.utils.Constants;
import java.io.IOException;
import java.util.Date;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Address;
import org.jsmpp.bean.DataCodings;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GSMSpecificFeature;
import org.jsmpp.bean.MessageMode;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SubmitMulti;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.util.DeliveryReceiptState;
import org.jsmpp.util.MessageId;

/**
 *
 * @author leonard
 */
public class DeliveryReceiptTask implements Runnable {

    private final SMPPServerSession session;
    private final MessageId messageId;

    private final String serviceType;

    private final TypeOfNumber sourceAddrTon;
    private final NumberingPlanIndicator sourceAddrNpi;
    private final String sourceAddress;

    private final TypeOfNumber destAddrTon;
    private final NumberingPlanIndicator destAddrNpi;
    private final String destAddress;

    private final int totalSubmitted;
    private final int totalDelivered;

    private final byte[] shortMessage;

    public DeliveryReceiptTask(SMPPServerSession session,
            SubmitSm submitSm, MessageId messageId) {
        this.session = session;
        this.messageId = messageId;

        serviceType = submitSm.getServiceType();

        // reversing destination to source
        sourceAddrTon = TypeOfNumber.valueOf(submitSm.getDestAddrTon());
        sourceAddrNpi = NumberingPlanIndicator.valueOf(submitSm.getDestAddrNpi());
        sourceAddress = submitSm.getDestAddress();

        // reversing source to destination
        destAddrTon = TypeOfNumber.valueOf(submitSm.getSourceAddrTon());
        destAddrNpi = NumberingPlanIndicator.valueOf(submitSm.getSourceAddrNpi());
        destAddress = submitSm.getSourceAddr();

        totalSubmitted = totalDelivered = 1;

        shortMessage = submitSm.getShortMessage();
    }

    public DeliveryReceiptTask(SMPPServerSession session,
            SubmitMulti submitMulti, MessageId messageId, Address address) {
        this.session = session;
        this.messageId = messageId;

        serviceType = submitMulti.getServiceType();

        // reversing destination to source
        sourceAddrTon = TypeOfNumber.valueOf(address.getTon());
        sourceAddrNpi = NumberingPlanIndicator.valueOf(address.getNpi());
        sourceAddress = address.getAddress();

        // reversing source to destination
        destAddrTon = TypeOfNumber.valueOf(submitMulti.getSourceAddrTon());
        destAddrNpi = NumberingPlanIndicator.valueOf(submitMulti.getSourceAddrNpi());
        destAddress = submitMulti.getSourceAddr();

        // distribution list assumed only contains single address
        totalSubmitted = totalDelivered = submitMulti.getDestAddresses().length;

        shortMessage = submitMulti.getShortMessage();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Constants.SMPP_LOGGER.error(e.getMessage());
        }
        SessionState state = session.getSessionState();
        if (!state.isReceivable()) {
            Constants.SMS_LOGGER.error("Not sending DLR for message id: {} msisdn: {} since session state is {}",
                    messageId, sourceAddress, state);
            return;
        }
        try {

            DeliveryReceipt delRec = new DeliveryReceipt(messageId.toString(), totalSubmitted, totalDelivered,
                    new Date(), new Date(), DeliveryReceiptState.DELIVRD, null, new String(shortMessage));
            session.deliverShortMessage(
                    serviceType,
                    sourceAddrTon,
                    sourceAddrNpi,
                    sourceAddress,
                    destAddrTon,
                    destAddrNpi,
                    destAddress,
                    new ESMClass(MessageMode.DEFAULT, MessageType.SMSC_DEL_RECEIPT, GSMSpecificFeature.DEFAULT),
                    (byte) 0,
                    (byte) 0,
                    new RegisteredDelivery(0),
                    DataCodings.ZERO,
                    delRec.toString().getBytes());
            Constants.SMS_LOGGER.info("Sending delivery reciept for message id: {} msisdn: {}", messageId, sourceAddress);
        } catch (IOException | InvalidResponseException | PDUException | NegativeResponseException | ResponseTimeoutException e) {
            Constants.SMS_LOGGER.error("Failed sending delivery_receipt for message id: {} msisdn: {}, Error is; {}",
                    messageId, sourceAddress, e.getMessage());
        }
    }
}
