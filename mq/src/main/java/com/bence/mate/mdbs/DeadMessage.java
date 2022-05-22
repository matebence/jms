package com.bence.mate.mdbs;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import javax.jms.MessageListener;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Message;

@MessageDriven(name = "DeadMessage", activationConfig = {
        @ActivationConfigProperty(propertyName = "user", propertyValue = "bence"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "password123"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "/jms/queue/DLQ"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")}, mappedName = "/jms/queue/DLQ")
public class DeadMessage implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(ExpiredMessage.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            LOGGER.warning("Message: " + textMessage.getText() + "  cannot be routed to their correct destination");
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }
}
