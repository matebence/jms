package com.bence.mate.services;

import com.bence.mate.models.Message;
import java.util.logging.Logger;
import java.util.Enumeration;

import javax.enterprise.context.ApplicationScoped;
import javax.jms.JMSPasswordCredential;
import javax.jms.JMSConnectionFactory;
import javax.annotation.Resource;
import javax.jms.JMSSessionMode;
import javax.inject.Inject;

import javax.jms.TemporaryQueue;
import javax.jms.ObjectMessage;
import javax.jms.QueueBrowser;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSConsumer;
import javax.jms.TextMessage;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.Topic;

@ApplicationScoped
public class MessagingService {

    private final static Logger LOGGER = Logger.getLogger(MessagingService.class.getName());

    @Resource(mappedName = "java:/jms/queue/JmsQueue")
    private Queue messeagingQueue;

    @Resource(mappedName = "java:/jms/topic/JmsTopic")
    private Topic messagingTopic;

    @Inject
    @JMSSessionMode(JMSContext.DUPS_OK_ACKNOWLEDGE)
    @JMSConnectionFactory("java:/ConnectionFactory")
    @JMSPasswordCredential(userName="ecneb",password="password123")
    private JMSContext context;

    public void topic(Message message) {
        try {
            TextMessage textMessage = context.createTextMessage(message.getMessage());
            textMessage.setJMSPriority(9);
            textMessage.setStringProperty("lorem", "Lorem");
            textMessage.setDoubleProperty("price", 1500);

            textMessage.setStringProperty("JMSXGroupID", "GROUP-0");

            JMSProducer producer = context.createProducer();

            producer.setTimeToLive(2000);
            Thread.sleep(3000);

            producer.setProperty("isAndroid", true);
            producer.send(messagingTopic, textMessage);
            browser();
        } catch (InterruptedException | JMSException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public void queue(Message message) {
        try {
            ObjectMessage objectMessage = context.createObjectMessage(message);
            TemporaryQueue replyQueue = context.createTemporaryQueue();
            JMSProducer producer = context.createProducer();

            objectMessage.setJMSReplyTo(replyQueue);

            producer.setDeliveryDelay(1000);

            producer.setPriority(3);
            producer.send(messeagingQueue, objectMessage);

            producer.setPriority(4);
            producer.send(messeagingQueue, objectMessage);
            //producer.send(messeagingQueue, message);

            JMSConsumer replyConsumer = context.createConsumer(replyQueue);
            TextMessage replyReceived = (TextMessage) replyConsumer.receive(5000);
            LOGGER.info("Replied text: " + replyReceived.getText());

            LOGGER.info(objectMessage.getJMSMessageID());
            browser();
        } catch (JMSException e) {
            LOGGER.info(e.getMessage());
        }
    }

    private void browser() throws JMSException {
        QueueBrowser queueBrowser = context.createBrowser(messeagingQueue);
        Enumeration messagesEnum = queueBrowser.getEnumeration();

        while(messagesEnum.hasMoreElements()) {
            TextMessage eachMessage = (TextMessage) messagesEnum.nextElement();
            LOGGER.info("Browsing: " + eachMessage.getText());
        }
    }
}
