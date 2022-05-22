package com.bence.mate.mdbs;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

import javax.jms.MessageListener;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Message;

@MessageDriven(name = "iOSMessageReceiver", activationConfig = {
        @ActivationConfigProperty(propertyName = "user", propertyValue = "ecneb"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "password123"),
        @ActivationConfigProperty(propertyName = "shareSubscriptions", propertyValue = "true"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "iOSSubscriber"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "/jms/topic/JmsTopic"),
        @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "price < 2000 OR JMSPriority BETWEEN 5 AND 9 OR lorem LIKE 'Lore_'"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic")}, mappedName = "/jms/topic/JmsTopic")
public class iOSMessageReceiver implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(iOSMessageReceiver.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            LOGGER.info("iOSMessageReceiver: " + textMessage.getText());
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }
}
