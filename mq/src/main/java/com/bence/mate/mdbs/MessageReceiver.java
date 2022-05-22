package com.bence.mate.mdbs;

import javax.ejb.ActivationConfigProperty;
import javax.jms.JMSPasswordCredential;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSSessionMode;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

import javax.jms.MessageListener;
import java.util.logging.Logger;
import javax.jms.ObjectMessage;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;
import javax.jms.JMSContext;
import javax.jms.Message;

@MessageDriven(name = "MessageReceiver", activationConfig = {
        @ActivationConfigProperty(propertyName = "user", propertyValue = "bence"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "password123"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "/jms/queue/JmsQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")}, mappedName = "/jms/queue/JmsQueue")
public class MessageReceiver implements MessageListener {

    private final static Logger LOGGER = Logger.getLogger(MessageReceiver.class.getName());

    @Inject
    @JMSSessionMode
    @JMSConnectionFactory("java:/ConnectionFactory")
    @JMSPasswordCredential(userName="ecneb",password="password123")
    private JMSContext context;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            com.bence.mate.models.Message modelMessage = (com.bence.mate.models.Message) objectMessage.getObject();
            LOGGER.info("Message arrived with text: " + modelMessage.toString());

            JMSProducer replyProducer = context.createProducer();
            TextMessage replyMessage = context.createTextMessage("Replying to " + message.getJMSMessageID());

            replyMessage.setJMSCorrelationID(message.getJMSMessageID());
            replyProducer.send(message.getJMSReplyTo(), replyMessage);
        } catch (JMSException e) {
            throw new IllegalStateException(e);
        }
    }
}
