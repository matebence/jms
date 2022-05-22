package com.bence.mate.mdbs;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import javax.jms.MessageListener;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Message;

@MessageDriven(name = "ExpiredMessage", activationConfig = {
        @ActivationConfigProperty(propertyName = "user", propertyValue = "ecneb"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "password123"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "/jms/queue/ExpiryQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")}, mappedName = "/jms/queue/ExpiryQueue")
public class ExpiredMessage implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(ExpiredMessage.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            LOGGER.warning("Expired message: " + textMessage.getText());
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }
}
