package ch.maxant.websocketdemo.mcs;

import org.slf4j.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(
        activationConfig={@ActivationConfigProperty(propertyName="destination", propertyValue="/jms/queue/events")},
        messageListenerInterface=MessageListener.class)
public class EventService {

    @Inject
    Logger logger;

    public void onMessage(Message m) throws JMSException {
        logger.info("ON MESSAGE: " + ((TextMessage)m).getText());
    }

}
