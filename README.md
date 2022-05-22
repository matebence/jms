## Java messeaging service (JMS)

- Messaging is software to software communication technique
- Most used queue systems are
	- ActiveMQ
	- SonicMQ
	- WebspaceMQ
	- TIBCO MQ

JMS has two types of models

> #### Point to point

- We create queues
- Sender the is the producer
- Receiver is the consumer
- The message is consumed only once by one application (consumer)
- If its consumed then the message is gone from the queue

![Point to point](https://raw.githubusercontent.com/matebence/jms/master/point_to_point.png)

> #### Publish/Subscribe

- We create  topics
- Sender is the producer
- Receivers are the subscribers
- Multible applications consumes the same message
- It broadcast the message to everyone

![Publish Subscribe](https://raw.githubusercontent.com/matebence/jms/master/publish_subscribe.png)

> #### JMS API

- ConnectionFactory - Excapsulates the connection configuration parameters
- JMSContext - Provides a combination of session and connection; allows for the create of consumers and producers
- JMSConsumer - Client object for reading messages from a queue or topic
- JMSProducer - Object for building and sending messages to a queie or topic

> #### Message structure

The message has tree parts
- Header (Metadatas)
	- Provider set Headers
		- JMSDestionation
		- JSMDeliveryMode
		- JMSMessageId
		- JMSTimeStamp
		- JMSExpiration
		- JMSRedeliverd
		- JMSPriority
	- Developer set headers
		- JMSReplyTo
		- JMSCorrelationID
		- JMSType

- Properties
	- Application specific
		- setXXXProperty
		- getXXXProperty
	- Provider Specific
		- JMSXUser
		- JMSXAppId
		- JMSXProducerTXID
		- JMSXConsumerTXID
		- JMSXRcvTimestamp
		- JMSDeliveryCount
		- JMSXState
		- JMSXgroupID
		- JMSXGroupSq
- Payload
	- Our data what we send

![Message structure](https://raw.githubusercontent.com/matebence/jms/master/jms_message.png)

> #### JMS Session types

- AUTO_ACKNOWLEDGE - With this acknowledgment mode, the session automatically acknowledges a client's receipt of a message either when the session has successfully returned from a call to receive or when the message listener the session has called to process the message successfully returns.
- CLIENT_ACKNOWLEDGE - With this acknowledgment mode, the client acknowledges a consumed message by calling the message's acknowledge method.
- DUPS_OK_ACKNOWLEDGE - This acknowledgment mode instructs the session to lazily acknowledge the delivery of messages.
- SESSION_TRANSACTED -This value may be passed as the argument to the method createSession(int sessionMode) on the Connection object to specify that the session should use a local transaction.

> #### Security

```xml
<security-serrings>
	<security-setting match="com.bence.mate.#">
		<permission type="createNonDurableQueue" roles="admin" />
		<permission type="deleteNonDurableQueue" roles="admin" />
		<permission type="createDurableQueue" roles="admin" />
		<permission type="deleteDurableQueue" roles="admin" />
		<permission type="createAddress" roles="admin,bence" />
		<permission type="deleteAddress" roles="admin" />
		<permission type="consume" roles="user" />
		<permission type="browse" roles="user" />
		<permission type="send" roles="user" />
		<permission type="manage" roles="admin" />
	</security-serrings>
</security-serrings>
```

> #### JMS features

> Catch all messages

```java
public static void main(String[] args) {
	InitialContext initialContext = null;
	Connection connection = null;

	try {
		initialContext = new InitialContext();
		ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
		 connection = cf.createConnection();
		Session session = connection.createSession();
		Queue queue = (Queue) initialContext.lookup("queue/myQueue");
		MessageProducer producer = session.createProducer(queue);
		TextMessage message1 = session.createTextMessage("Message 1");
		TextMessage message2 = session.createTextMessage("Message 2");

		producer.send(message1);
		producer.send(message2);
		
		QueueBrowser browser = session.createBrowser(queue);
		
		Enumeration messagesEnum = browser.getEnumeration();
		
		while(messagesEnum.hasMoreElements()) {
			TextMessage eachMessage = (TextMessage) messagesEnum.nextElement();
			System.out.println("Browsing:"+eachMessage.getText());
		}
		

		MessageConsumer consumer = session.createConsumer(queue);
		connection.start();
		TextMessage messageReceived = (TextMessage) consumer.receive(5000);
		System.out.println("Message Received: " + messageReceived.getText());
		 messageReceived = (TextMessage) consumer.receive(5000);
		System.out.println("Message Received: " + messageReceived.getText());
		

	} catch (Exception e) {
		e.printStackTrace();
	}
}
```

> Setting priority (default priority is 4)

```java
public static void main(String[] args) throws NamingException {
	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/myQueue");
	
	try(ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()){
		JMSProducer producer = jmsContext.createProducer();
		
		String[] messages = new String[3];
		messages[0] = "Message One";
		messages[1] = "Message Two";
		messages[2] = "Message Three";
		
		producer.setPriority(3);
		producer.send(queue, messages[0]);
		
		producer.setPriority(4);
		producer.send(queue, messages[1]);
		
		producer.setPriority(9);
		producer.send(queue, messages[2]);
		
		JMSConsumer consumer = jmsContext.createConsumer(queue);
		
		for(int i=0;i<3;i++) {
			Message receivedMessage = consumer.receive();
			System.out.println(receivedMessage.getJMSPriority());
			System.out.println(consumer.receiveBody(String.class));
		}
		
	} catch (JMSException e) {
		e.printStackTrace();
	}
}
```

> Request & Reply

```java
public static void main(String[] args) throws NamingException, JMSException {
	
	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/requestQueue");
	//Queue replyQueue = (Queue) context.lookup("queue/replyQueue");
	
	try(ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()){

		JMSProducer producer = jmsContext.createProducer();
		TemporaryQueue replyQueue = jmsContext.createTemporaryQueue();
		TextMessage message = jmsContext.createTextMessage("Arise Awake and stop not till the goal is reached");

		//the reply to header is used only when we programatically create the quueue with the temp quueue
		message.setJMSReplyTo(replyQueue);
		producer.send(queue,message);
		//here we get an messageid
		System.out.println(message.getJMSMessageID());
		
		Map<String,TextMessage> requestMessages = new HashMap<>();
		requestMessages.put(message.getJMSMessageID(), message);
		
		JMSConsumer consumer = jmsContext.createConsumer(queue);
		TextMessage messageReceived = (TextMessage) consumer.receive();
		System.out.println(messageReceived.getText());
		
		JMSProducer replyProducer = jmsContext.createProducer();
		TextMessage replyMessage = jmsContext.createTextMessage("You are awesome!!");
		//we set the corelation id via the messageid
		replyMessage.setJMSCorrelationID(messageReceived.getJMSMessageID());

		replyProducer.send(messageReceived.getJMSReplyTo(), replyMessage);
		
		JMSConsumer replyConsumer = jmsContext.createConsumer(replyQueue);
		//System.out.println( replyConsumer.receiveBody(String.class));
		TextMessage replyReceived = (TextMessage) replyConsumer.receive();
		System.out.println(replyReceived.getJMSCorrelationID());
		//here we get it back
		System.out.println(requestMessages.get(replyReceived.getJMSCorrelationID()).getText());
	}
}
```

> Set message expiry

```java
public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/myQueue");

	//we can access our expired messages from this queueu
	Queue expiryQueue = (Queue) context.lookup("queue/expiryQueue");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		JMSProducer producer = jmsContext.createProducer();
		producer.setTimeToLive(2000);
		producer.send(queue, "Arise Awake and stop not till the goal is reached");
		//the message expires and it goes to the expiry queueu
		Thread.sleep(5000);

		//we wont get here any message
		Message messageReceived = jmsContext.createConsumer(queue).receive(5000);
		System.out.println(messageReceived);
		System.out.println(jmsContext.createConsumer(expiryQueue).receiveBody(String.class));
	}

}
```

> Set deliery delay

```java
public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/myQueue");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		JMSProducer producer = jmsContext.createProducer();
		producer.setDeliveryDelay(3000);
		producer.send(queue, "Arise Awake and stop not till the goal is reached");

		Message messageReceived = jmsContext.createConsumer(queue).receive(5000);
		System.out.println(messageReceived);
	}
}
```

> Custom properties

```java
public static void main(String[] args) throws NamingException, InterruptedException, JMSException {

	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/myQueue");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		JMSProducer producer = jmsContext.createProducer();
		TextMessage textMessage = jmsContext.createTextMessage("Arise Awake and stop not till the goal is reached");
		textMessage.setBooleanProperty("loggedIn", true);
		textMessage.setStringProperty("userToken", "abc123");
		producer.send(queue, textMessage);

		Message messageReceived = jmsContext.createConsumer(queue).receive(5000);
		System.out.println(messageReceived);
		System.out.println(messageReceived.getBooleanProperty("loggedIn"));
		System.out.println(messageReceived.getStringProperty("userToken"));
	}
}
```

> Message types

```java
public static void main(String[] args) throws NamingException, InterruptedException, JMSException {
	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/myQueue");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		JMSProducer producer = jmsContext.createProducer();
		TextMessage textMessage = jmsContext.createTextMessage("Arise Awake and stop not till the goal is reached");
		BytesMessage bytesMessage = jmsContext.createBytesMessage();
		bytesMessage.writeUTF("John");
		bytesMessage.writeLong(123l);
		
		StreamMessage streamMessage = jmsContext.createStreamMessage();
		streamMessage.writeBoolean(true);
		streamMessage.writeFloat(2.5f);
		
		MapMessage mapMessage = jmsContext.createMapMessage();
		mapMessage.setBoolean("isCreditAvailable", true);
		
		ObjectMessage objectMessage = jmsContext.createObjectMessage();
		Patient patient = new Patient();
		patient.setId(123);
		patient.setName("John");
		objectMessage.setObject(patient);
		
		//producer.send(queue, objectMessage);
		producer.send(queue, patient);

		//BytesMessage messageReceived = (BytesMessage) jmsContext.createConsumer(queue).receive(5000);
		//System.out.println(messageReceived.readUTF());
		//System.out.println(messageReceived.readLong());
		
		
		//StreamMessage messageReceived = (StreamMessage) jmsContext.createConsumer(queue).receive(5000);
		//System.out.println(messageReceived.readBoolean());
		//System.out.println(messageReceived.readFloat());
		
		//MapMessage messageReceived = (MapMessage) jmsContext.createConsumer(queue).receive(5000);
		//System.out.println(messageReceived.getBoolean("isCreditAvailable"));
		
		//Patient object = (Patient) messageReceived.getObject();
		Patient patientReceived = jmsContext.createConsumer(queue).receiveBody(Patient.class);
		System.out.println(patientReceived.getId());
		System.out.println(patientReceived.getName());
	}
}
```

> Point to point (load balancing)

```java
public static void main(String[] args) throws NamingException, InterruptedException {
	InitialContext initialContext = new InitialContext();
	Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext("eligibilityuser", "eligibilitypass")) {
		JMSConsumer consumer1 = jmsContext.createConsumer(requestQueue);
		JMSConsumer consumer2 = jmsContext.createConsumer(requestQueue);

		//loadbalancing when put it on diff machines
		for(int i=1;i<=10;i+=2) {
			System.out.println("Consumer1: "+consumer1.receive());
			System.out.println("Consumer2: "+consumer2.receive());
		}


		consumer.setMessageListener(new EligibilityCheckListener());
		Thread.sleep(5000);
	};
}
```

> Publish subscribe (shared subscriptions)

```java
public static void main(String[] args) throws NamingException, JMSException {
	InitialContext context = new InitialContext();
	Topic topic = (Topic) context.lookup("topic/empTopic");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		JMSConsumer consumer = jmsContext.createSharedConsumer(topic, "sharedConsumer");
		JMSConsumer consumer2 = jmsContext.createSharedConsumer(topic, "sharedConsumer");

		for (int i = 1; i <= 10; i += 2) {
			Message message = consumer.receive();
			Employee employee = message.getBody(Employee.class);
			System.out.println("Consumer 1: " + employee.getFirstName());

			Message message2 = consumer2.receive();
			Employee employee2 = message2.getBody(Employee.class);
			System.out.println("Consumer 2: " + employee2.getFirstName());
		}
	}
}
```

> Publish subscribe (durable subscriptions)

```java
public static void main(String[] args) throws NamingException, JMSException, InterruptedException {
	InitialContext context = new InitialContext();
	Topic topic = (Topic) context.lookup("topic/empTopic");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		
		//we create uniqe idenfiers security app and subscription1
		jmsContext.setClientID("securityApp");
		JMSConsumer consumer = jmsContext.createDurableConsumer(topic, "subscription1");
		consumer.close(); //app goes down
		
		Thread.sleep(10000);

		//app is back
		consumer = jmsContext.createDurableConsumer(topic, "subscription1");
		Message message = consumer.receive();
		Employee employee = message.getBody(Employee.class);
		System.out.println(employee.getFirstName());
		
		consumer.close();
		//we unsubscribe completly
		jmsContext.unsubscribe("subscription1");
	}
}
```

> Filter messages

```java
public static void main(String[] args) throws NamingException, JMSException {
	InitialContext initialContext = new InitialContext();
	Queue requestQueue = (Queue) initialContext.lookup("queue/claimQueue");

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext()) {
		JMSProducer producer = jmsContext.createProducer();
		JMSConsumer consumer = jmsContext.createConsumer(requestQueue, "doctorType IN ('neuro','psych') OR JMSPriority BETWEEN 5 AND 9 OR doctorName LIKE 'Joh_'");

		ObjectMessage objectMessage = jmsContext.createObjectMessage();
		//objectMessage.setIntProperty("hospitalId", 1);
		//objectMessage.setDoubleProperty("claimAmount", 1000);
		//objectMessage.setStringProperty("doctorName", "John");
		objectMessage.setStringProperty("doctorType", "gyna");
		Claim claim = new Claim();
		claim.setHospitalId(1);
		claim.setClaimAmount(1000);
		claim.setDoctorName("John");
		claim.setDoctorType("gyna");
		claim.setInsuranceProvider("blue cross");
		objectMessage.setObject(claim);

		producer.send(requestQueue, objectMessage);

		Claim receiveBody = consumer.receiveBody(Claim.class);
		System.out.println(receiveBody.getClaimAmount());
	}
}
```

> Message grouping

```java
public static void main(String[] args) throws Exception {

	InitialContext context = new InitialContext();
	Queue queue = (Queue) context.lookup("queue/myQueue");
	Map<String, String> receivedMessages = new ConcurrentHashMap<>();

	try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
			JMSContext jmsContext = cf.createContext();
			JMSContext jmsContext2 = cf.createContext()) {
		JMSProducer producer = jmsContext.createProducer();
		JMSConsumer consumer1 = jmsContext2.createConsumer(queue);
		consumer1.setMessageListener(new MyListener("Consumer-1", receivedMessages));
		JMSConsumer consumer2 = jmsContext2.createConsumer(queue);
		consumer2.setMessageListener(new MyListener("Consumer-2", receivedMessages));

		int count = 10;
		TextMessage[] messages = new TextMessage[count];
		//it will walys read the same consume because of the group
		for (int i = 0; i < count; i++) {
			messages[i] = jmsContext.createTextMessage("Group-0 message" + i);
			messages[i].setStringProperty("JMSXGroupID", "Group-0");
			producer.send(queue, messages[i]);
		}
		
		Thread.sleep(2000);
		
		for (TextMessage message : messages) {
			if (!receivedMessages.get(message.getText()).equals("Consumer-1")) {
				throw new IllegalStateException(
						"Group Message" + message.getText() + "has gone to the wrong receiver");
			}
		}

	}

}
```

> Session type AUTO_ACKNOWLEDGE

```java
public class MessageProducer {

	public static void main(String[] args) throws NamingException, JMSException {

		InitialContext initialContext = new InitialContext();
		Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

		try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
				JMSContext jmsContext = cf.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {

			JMSProducer producer = jmsContext.createProducer();
			producer.send(requestQueue, "Message 1");
		}
	}
}
```

> Session type CLIENT_ACKNOWLEDGE

```java
public class Consumer {
	public static void main(String[] args) throws NamingException, JMSException {

		InitialContext initialContext = new InitialContext();
		Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

		try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
				JMSContext jmsContext = cf.createContext(JMSContext.CLIENT_ACKNOWLEDGE)) {

			JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
			TextMessage message = (TextMessage) consumer.receive();
			System.out.println(message.getText());
			message.ackowledge();
		}
	}
}
```

> Session type DUPS_OK_ACKNOWLEDGE

```java
public class Producer {

	public static void main(String[] args) throws NamingException, JMSException {

		InitialContext initialContext = new InitialContext();
		Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

		try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
				JMSContext jmsContext = cf.createContext(JMSContext.DUPS_OK_ACKNOWEDGE)) {

			JMSProducer producer = jmsContext.createProducer();
			producer.send(requestQueue, "Message 1");
		}
	}
}
```

> Transactions

```java
public class Producer {

	public static void main(String[] args) throws NamingException, JMSException {

		InitialContext initialContext = new InitialContext();
		Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

		try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
				JMSContext jmsContext = cf.createContext(JMSContext.SESSION_TRANSACTED)) {

			JMSProducer producer = jmsContext.createProducer();
			producer.send(requestQueue, "Message 1");
			producer.send(requestQueue, "Message 2");


			jmsContext.commit();

			//if any exception then
			jmsContext.rollback();
		}
	}
}

public class Consumer {
	public static void main(String[] args) throws NamingException, JMSException {

		InitialContext initialContext = new InitialContext();
		Queue requestQueue = (Queue) initialContext.lookup("queue/requestQueue");

		try (ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
				JMSContext jmsContext = cf.createContext(JMSContext.SESSION_TRANSACTED)) {

			JMSConsumer consumer = jmsContext.createConsumer(requestQueue);
			TextMessage message = (TextMessage) consumer.receive();
			System.out.println(message.getText());

			jmsContext.commit();


			//if any exception then
			jmsContext.rollback();
		}
	}
}
```

> #### cURL scripts

> API health endpoint

```bash
curl -v -XGET -H 'Accept: application/json' 'http://localhost:8080/mq/api/health'
```

> Message resource

```bash
curl -v -XPOST -u bence:password123 -H 'Accept: application/json' -H 'Content-type: application/json' -d '{"id":1,"message":"Test message","year":2022}' 'http://localhost:8080/mq/api/messages/send'

curl -v -XPOST -u ecneb:password123 -H 'Accept: application/json' -H 'Content-type: application/json' -d '{"id":1,"message":"Test message","year":2022}' 'http://localhost:8080/mq/api/messages/broadcast'
```

> #### Annotations

- @Path - Sets the path in URL
- @ApplicationPath - Sets the base path for the whole application

- @GET - method annotation indicates a GET request

- @Produces - Indicates what type of data will be produced
- @Consumes - Indicates what type of data is required for the given endpoint

- @DenyAll - Specifies that no security roles are permitted to access your resources
- @PermitAll - Specifies that all security roles are permitted to access your resources
- @RolesAllowed - specifies the security roles that are permitted to access your resources

- @MessageDriven - To handle the incoming message, we must implement the onMessage() method of the MessageListener interface and annotate the class with @MessageDriven. They look and feel similar to stateless session beans, but can not be directly access by the client.
- @JMSSessionMode - Used to specify the session mode to be used when injecting a JMSContext
- @JMSConnectionFactory - Used to specify the JNDI lookup name of a ConnectionFactory to be used when injecting a JMSContext object.
- @JMSPasswordCredential - Used to specify the userName and password to be used when injecting a JMSContext object.
- @Resource - Used to specify JMS resource(Topic, Queue) via JNDI lookup.
- @ActivationConfigProperty - Used to provide information to the deployer about the configuration of a message driven bean in its operational environment

> #### List of Config properties for Message driven beans

|Name 					|Type 	   			 |Description 			  																																																				    										|Mandatory 				|Default 		|
|-----------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|---------------|
|destination 			|java.lang.String 	 |The jndi name of the Queue or Topic 																																																	    										|Yes 					|NONE
|destinationType 		|java.lang.String 	 |The type of destination valid values are javax.jms.Queue or javax.jms.Topic 																																																		|No 					|NONE
|messageSelector 		|java.lang.String 	 |The message selector of the subscription 																																																											|No 					|NONE
|acknowledgeMode 		|java.lang.Intger	 |The type of acknowledgement when not using transacted jms 																																																						|No 					|NONE
|clientID 				|java.lang.String    |The client id of the connection 																																																	  												|No 					|AUTO_ACKNOWLEDGE
|subscriptionDurability |java.lang.String 	 |Whether topic subscriptions are durable. Valid values are Durable or NonDurable 																																																	|No 					|NONE
|subscriptionName 		|java.lang.String 	 |The subsription name of the topic subscription 																																																									|No 					|NonDurable
|shareSubscriptions 	|java.lang.Boolean 	 |Are messages from a Topic can be shared by multiple consumers? 																																																					|No 					|NONE
|isTopic 				|java.lang.Boolean 	 |Sets the destinationType 																																																	  			    										|No 					|false
|providerAdapterJNDI 	|java.lang.String 	 |The jndi name of the jms provider 																																																	  											|No 					|java:/DefaultJMSProvider
|user 					|java.lang.String    |The user id used to connect to the jms server 																																																									|No 					|NONE
|pass 					|java.lang.String  	 |The password of the user 																																																	  														|No 					|NONE
|maxMessages 			|java.lang.Integer   |Read this number of messages before delivering messages to the mdb. Each message is delivered individually on the same thread in an attempt to avoid context excessive context switching 																							|No 					|1
|minSession 			|java.lang.Integer   |The minimum number of jms sessions that are available to concurrently deliver messages to this mdb 																																												|No 					|1
|maxSession 			|java.lang.Integer   |The maximum number of jms sessions that are available to concurrently deliver messages to this mdb 																																												|No 					|15
|reconnectInterval 		|java.lang.Long  	 |The length of time in seconds between attempts to (re-)connect to the jms provider 																																																|No 					|10 Seconds
|keepAlive 				|java.lang.Long  	 |The length of time in milliseconds that sessions over the minimum are kept alive 																																																	|No 					|60 Seconds
|sessionTransacted 		|java.lang.Boolean   |Whether the sessions are transacted 																																																	  											|No 					|true 
|useDLQ 				|java.lang.Boolean   |Whether to use a DLQ handler 																																																	   													|No 					|true
|dLQJNDIName 			|java.lang.String  	 |The JNDI name of the DLQ 																																																	  			    										|No 					|queue/DLQ
|dLQHandler 			|java.lang.String  	 |The org.jboss.resource.adapter.jms.inflow.DLQHandler implementation class name 																																						    										|No 					|org.jboss.resource.adapter.jms.inflow.dlq.GenericDLQHandler
|dLQUser 				|java.lang.String  	 |The user id used to make the dlq connection to the jms server 																																																					|No 					|NONE
|dLQPassword 			|java.lang.String  	 |The password of the dLQUser 																																																	      												|No 					|NONE
|dLQClientID 			|java.lang.String  	 |The client id of the dlq connection 																																																	    										|No 					|NONE
|dLQMaxResent 			|java.lang.Integer   |The maximum number of times a message is redelivered before it is sent to the DLQ 																																																|No 					|5
|redeliverUnspecified 	|java.lang.Boolean   |Whether to attempt to redeliver a message in an unspecified transaction context 																																																	|No 					|true
|transactionTimeout 	|java.lang.Integer   |Time in seconds for the transaction timeout 																																																										|No 					|Default is the timeout set for the resource manager
|DeliveryActive 		|java.lang.Boolean   |Whether the MDB should make the subscription at initial deployment or wait for start() or stopDelivery() on the corresponding MBean. You can set this to false if you want to prevent messages from being delivered to the MDB (which is still starting) during server startup    |No 					|true

> #### Wildfly setup [(complete WildFly docs)](https://github.com/wildfly/wildfly/blob/main/docs/src/main/asciidoc/_admin-guide/subsystem-configuration/Messaging.adoc)

```bash
# Add a Simple Role Decoder which maps the application Roles from the attribute Roles in the File system.
/subsystem=elytron/simple-role-decoder=from-roles-attribute:add(attribute=Roles)

# Let’s define a new filesystem-realm named fsRealm and its respective path on the file system:
/subsystem=elytron/filesystem-realm=FileSystemRealm:add(path=demofs-realm-users,relative-to=jboss.server.config.dir)

# Next, we add some identities to the Realm:
/subsystem=elytron/filesystem-realm=FileSystemRealm:add-identity(identity=bence)
/subsystem=elytron/filesystem-realm=FileSystemRealm:set-password(identity=bence,clear={password="password123"})
/subsystem=elytron/filesystem-realm=FileSystemRealm:add-identity-attribute(identity=bence,name=Roles, value=["user"])

/subsystem=elytron/filesystem-realm=FileSystemRealm:add-identity(identity=ecneb)
/subsystem=elytron/filesystem-realm=FileSystemRealm:set-password(identity=ecneb,clear={password="password123"})
/subsystem=elytron/filesystem-realm=FileSystemRealm:add-identity-attribute(identity=ecneb,name=Roles, value=["admin","user"])

# Create a new Security Domain which maps our Realm:
/subsystem=elytron/security-domain=JmsSecurityDomain:add(realms=[{realm=FileSystemRealm,role-decoder=from-roles-attribute}],default-realm=FileSystemRealm,permission-mapper=default-permission-mapper)

# We need an Http Authentication Factory which references our Security Domain:
/subsystem=elytron/http-authentication-factory=example-fs-http-auth:add(http-server-mechanism-factory=global,security-domain=JmsSecurityDomain,mechanism-configurations=[{mechanism-name=BASIC,mechanism-realm-configurations=[{realm-name=RealmUsersRoles}]}])

# Finally, a Security Domain in the undertow’s subsystem will be associated with our Http Authentication Factory:
/subsystem=undertow/application-security-domain=AppSecurityDomain:add(http-authentication-factory=example-fs-http-auth)
/subsystem=ejb3/application-security-domain=AppSecurityDomain:add(security-domain=JmsSecurityDomain)

# Restart running server
reload

# Enable ActiveMQ
/extension=org.wildfly.extension.messaging-activemq:add()
/subsystem=messaging-activemq:add
/:reload

# Adding security (for all queues)
/subsystem=messaging-activemq/server=default:add
/subsystem=messaging-activemq/server=default/security-setting=#:add

# Creating dead letter queue and expiry queue
/subsystem=messaging-activemq/server=default/address-setting=#:add(dead-letter-address="jms.queue.DLQ", expiry-address="jms.queue.ExpiryQueue", expiry-delay="-1", max-delivery-attempts="10", max-size-bytes="10485760", page-size-bytes="2097152", message-counter-history-day-limit="10")

# Creating connector type invm
/subsystem=messaging-activemq/server=default/in-vm-connector=in-vm:add(server-id="0")
/subsystem=messaging-activemq/server=default/in-vm-acceptor=in-vm:add(server-id="0")
/subsystem=messaging-activemq/server=default/jms-queue=ExpiryQueue:add(entries=["java:/jms/queue/ExpiryQueue"])
/subsystem=messaging-activemq/server=default/jms-queue=DLQ:add(entries=["java:/jms/queue/DLQ"])

# Creating connection factory
/subsystem=messaging-activemq/server=default/connection-factory=InVmConnectionFactory:add(connectors=["in-vm"], entries=["java:/ConnectionFactory"])
/subsystem=messaging-activemq/server=default/pooled-connection-factory=activemq-ra:add(transaction="xa", connectors=["in-vm"], entries=["java:/JmsXA java:jboss/DefaultJMSConnectionFactory"])
/subsystem=ee/service=default-bindings/:write-attribute(name="jms-connection-factory", value="java:jboss/DefaultJMSConnectionFactory")
/subsystem=ejb3:write-attribute(name="default-resource-adapter-name", value="${ejb.resource-adapter-name:activemq-ra.rar}")
/subsystem=ejb3:write-attribute(name="default-mdb-instance-pool", value="mdb-strict-max-pool")

# Creating custom queues
/subsystem=messaging-activemq/server=default/jms-queue=JmsQueue:add(entries=[java:/jms/queue/JmsQueue])
/subsystem=messaging-activemq/server=default/jms-topic=JmsTopic:add(entries=[java:/jms/topic/JmsTopic])

# Setting up role based auth for JMS
/subsystem=messaging-activemq/server=default/security-setting=#/role=admin:add(send="true", consume="true", create-non-durable-queue="true", delete-non-durable-queue="true", create-durable-queue="true", delete-durable-queue="true")
/subsystem=messaging-activemq/server=default/security-setting=#/role=user:add(send="false", consume="true", create-non-durable-queue="false", delete-non-durable-queue="false")

# Associate auth with security domain
/subsystem=messaging-activemq/server=default:write-attribute(name=elytron-domain, value=JmsSecurityDomain)

# Setting up Remote connection factory (if needed)
/subsystem=messaging-activemq/server=default/connection-factory=RemoteConnectionFactory:add(connectors=["http-connector"], entries = ["java:jboss/exported/jms/RemoteConnectionFactory"])

/subsystem=messaging-activemq/server=default/http-acceptor=http-acceptor:add(http-listener="default")
/subsystem=messaging-activemq/server=default/http-acceptor=http-acceptor-throughput:add(http-listener="default", params={batch-delay="50", direct-deliver="false"})

/subsystem=messaging-activemq/server=default/http-connector=http-connector:add(socket-binding="http", endpoint="http-acceptor")
/subsystem=messaging-activemq/server=default/http-connector=http-connector-throughput:add(socket-binding="http", endpoint="http-acceptor-throughput" ,params={batch-delay="50"})
```

> #### [JMS API for Spring](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/jms.html)