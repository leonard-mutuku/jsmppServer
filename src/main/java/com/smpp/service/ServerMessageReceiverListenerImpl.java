/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smpp.service;

import com.smpp.utils.Constants;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jsmpp.PDUStringException;
import static org.jsmpp.SMPPConstant.STAT_ESME_RSYSERR;
import org.jsmpp.bean.Address;
import org.jsmpp.bean.BroadcastSm;
import org.jsmpp.bean.CancelBroadcastSm;
import org.jsmpp.bean.CancelSm;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DestinationAddress;
import org.jsmpp.bean.OptionalParameter;
import org.jsmpp.bean.QueryBroadcastSm;
import org.jsmpp.bean.QuerySm;
import org.jsmpp.bean.ReplaceSm;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.SubmitMulti;
import org.jsmpp.bean.SubmitSm;
import org.jsmpp.bean.UnsuccessDelivery;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.BroadcastSmResult;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.QueryBroadcastSmResult;
import org.jsmpp.session.QuerySmResult;
import org.jsmpp.session.SMPPServerSession;
import org.jsmpp.session.ServerMessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.session.SubmitMultiResult;
import org.jsmpp.session.SubmitSmResult;
import org.jsmpp.util.MessageIDGenerator;
import org.jsmpp.util.MessageId;
import org.jsmpp.util.RandomMessageIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * @author leonard
 */
public class ServerMessageReceiverListenerImpl implements ServerMessageReceiverListener {

    private final MessageIDGenerator messageIDGenerator;
    private final Charset charset;

    @Autowired
    @Qualifier("smppTaskExecutor")
    private TaskExecutor taskExecutor;

    public ServerMessageReceiverListenerImpl() {
        this.charset = StandardCharsets.ISO_8859_1;
        messageIDGenerator = new RandomMessageIDGenerator();
        Constants.SMPP_LOGGER.info("SMSC charset is {}", charset.name());
    }

    private MessageId generateMessageId() {
        MessageId messageId = null;
        try {
            String id = messageIDGenerator.newMessageId().getValue();
            String stringValue = Integer.valueOf(id, 16).toString();
            messageId = new MessageId(stringValue);
        } catch (PDUStringException ex) {
            Constants.SMS_LOGGER.error("Error generating MessageId. Error is; {}", ex.getMessage());
        }
        
        return messageId;
    }

    private boolean checkRequestDlr(byte reqDlr) {
        return SMSCDeliveryReceipt.SUCCESS.containedIn(reqDlr)
                || SMSCDeliveryReceipt.SUCCESS_FAILURE.containedIn(reqDlr);
    }

    @Override
    public SubmitSmResult onAcceptSubmitSm(SubmitSm submitSm, SMPPServerSession source) throws ProcessRequestException {
        final MessageId messageId = generateMessageId();
        Constants.SMS_LOGGER.info("Receiving submit_sm; Session {} received '{}' message id {}",
                source.getSessionId(), new String(submitSm.getShortMessage(), charset), messageId);
        boolean reqDlr = checkRequestDlr(submitSm.getRegisteredDelivery());
        if (reqDlr) {
            taskExecutor.execute(() -> new DeliveryReceiptTask(source, submitSm, messageId).run());
        }
        
        return new SubmitSmResult(messageId, new OptionalParameter[0]);
    }

    @Override
    public SubmitMultiResult onAcceptSubmitMulti(SubmitMulti submitMulti, SMPPServerSession source) throws ProcessRequestException {
        MessageId messageId = generateMessageId();
        Constants.SMS_LOGGER.info("Receiving submit_multi_sm; session {} message '{}', message id {}",
                source.getSessionId(), new String(submitMulti.getShortMessage(), charset), messageId);
        boolean reqDlr = checkRequestDlr(submitMulti.getRegisteredDelivery());
        if (reqDlr) {
            for (DestinationAddress destAddresse : submitMulti.getDestAddresses()) {
                Address address = (Address) destAddresse;
                taskExecutor.execute(new DeliveryReceiptTask(source, submitMulti, messageId, address));
            }
        }

        return new SubmitMultiResult(messageId.getValue(), new UnsuccessDelivery[0], new OptionalParameter[0]);
    }

    @Override
    public QuerySmResult onAcceptQuerySm(QuerySm querySm, SMPPServerSession source) throws ProcessRequestException {
        Constants.SMPP_LOGGER.info("Received query_sm");
        throw new ProcessRequestException("The replace_sm is not implemented", STAT_ESME_RSYSERR);
    }

    @Override
    public void onAcceptReplaceSm(ReplaceSm replaceSm, SMPPServerSession source) throws ProcessRequestException {
        Constants.SMPP_LOGGER.info("Received replace_sm");
        throw new ProcessRequestException("The replace_sm is not implemented", STAT_ESME_RSYSERR);
    }

    @Override
    public void onAcceptCancelSm(CancelSm cancelSm, SMPPServerSession source) throws ProcessRequestException {
        Constants.SMPP_LOGGER.info("Received cancel_sm");
        throw new ProcessRequestException("The cancel_sm is not implemented", STAT_ESME_RSYSERR);
    }

    @Override
    public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) throws ProcessRequestException {
        Constants.SMPP_LOGGER.info("Received data_sm");
        throw new ProcessRequestException("The data_sm is not implemented", STAT_ESME_RSYSERR);
    }

    @Override
    public BroadcastSmResult onAcceptBroadcastSm(BroadcastSm broadcastSm, SMPPServerSession source) throws ProcessRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onAcceptCancelBroadcastSm(CancelBroadcastSm cancelBroadcastSm, SMPPServerSession source) throws ProcessRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public QueryBroadcastSmResult onAcceptQueryBroadcastSm(QueryBroadcastSm queryBroadcastSm, SMPPServerSession source) throws ProcessRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
