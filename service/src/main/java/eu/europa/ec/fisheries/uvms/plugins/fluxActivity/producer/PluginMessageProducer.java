package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer;

import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ModuleQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.*;

@Stateless
@LocalBean
public class PluginMessageProducer {

    @Resource(mappedName = ExchangeModelConstants.EXCHANGE_MESSAGE_IN_QUEUE)
    private Queue exchangeQueue;

    @Resource(mappedName = ExchangeModelConstants.PLUGIN_EVENTBUS)
    private Topic eventBus;

    @Resource(lookup = ExchangeModelConstants.CONNECTION_FACTORY)
    private ConnectionFactory connectionFactory;

    private Connection connection = null;
    private Session session = null;

    private static final Logger LOG = LoggerFactory.getLogger(PluginMessageProducer.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendResponseMessage(String text, TextMessage requestMessage) throws JMSException {
        try {
            connectQueue();
            TextMessage message = session.createTextMessage();
            message.setJMSDestination(requestMessage.getJMSReplyTo());
            message.setJMSCorrelationID(requestMessage.getJMSMessageID());
            message.setText(text);
            session.createProducer(requestMessage.getJMSReplyTo()).send(message);
        } catch (JMSException e) {
            LOG.error("[ Error when sending jms message. ] {}", e);
            throw new JMSException(e.getMessage());
        } finally {
            disconnectQueue();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendModuleMessage(String text, ModuleQueue queue) throws JMSException {
        try {
            connectQueue();
            TextMessage message = session.createTextMessage();
            message.setText(text);
            switch (queue) {
                case EXCHANGE:
                    session.createProducer(exchangeQueue).send(message);
                    break;
                default:
                    LOG.error("[ Sending Queue is not implemented ]");
                    break;
            }
            return message.getJMSMessageID();
        } catch (JMSException e) {
            LOG.error("[ Error when sending data source message. ] {}", e);
            throw new JMSException(e.getMessage());
        } finally {
            disconnectQueue();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String sendEventBusMessage(String text, String serviceName) throws JMSException {
        try {
            connectQueue();
            TextMessage message = session.createTextMessage();
            message.setText(text);
            message.setStringProperty(ExchangeModelConstants.SERVICE_NAME, serviceName);
            session.createProducer(eventBus).send(message);
            return message.getJMSMessageID();
        } catch (JMSException e) {
            LOG.error("[ Error when sending message. ] {0}", e);
            throw new JMSException(e.getMessage());
        } finally {
            disconnectQueue();
        }
    }

    private void connectQueue() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }

    private void disconnectQueue() {
        try {
            connection.stop();
            connection.close();
        } catch (JMSException e) {
            LOG.error("[ Error when stopping or closing JMS queue. ] {}", e);
        }
    }
}
