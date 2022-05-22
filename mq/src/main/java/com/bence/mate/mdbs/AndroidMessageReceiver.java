package com.bence.mate.mdbs;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import javax.jms.MessageListener;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Message;

@MessageDriven(name = "AndroidMessageReceiver", activationConfig = {
        @ActivationConfigProperty(propertyName = "user", propertyValue = "ecneb"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "password123"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "/jms/topic/JmsTopic"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")}, mappedName = "/jms/topic/JmsTopic")
public class AndroidMessageReceiver implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(AndroidMessageReceiver.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            Boolean isAndroid = message.getBooleanProperty("isAndroid");
            if (isAndroid) {
                TextMessage textMessage = (TextMessage) message;
                LOGGER.info("AndroidMessageReceiver: " + textMessage.getText());
            }
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }
}
