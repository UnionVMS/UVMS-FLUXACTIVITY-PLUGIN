package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;

/**
 * Created by sanera on 21/06/2016.
 */


@Stateless
@LocalBean
public class FluxMessageProducer {
  /*   @Resource(mappedName = FluxPluginConstants.FLUX_MESSAGE_IN_QUEUE)
    private Queue fluxQueue;

    private Connection connection = null;
    private Session session = null;
    @Resource(lookup = FluxPluginConstants.CONNECTION_FACTORY)
    private ConnectionFactory connectionFactory; */

    final static Logger LOG = LoggerFactory.getLogger(FluxMessageProducer.class);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendModuleMessage(String text) throws JMSException {

        LOG.info("to do: send message to flux:"+text);

        
     /*   try {
            connectQueue();

            TextMessage message = session.createTextMessage();
            message.setText(text);
            session.createProducer(fluxQueue).send(message);

        } catch (JMSException e) {
            LOG.error("[ Error when sending data source message. ] {}", e.getMessage());
            throw new JMSException(e.getMessage());
        } finally {
            disconnectQueue();
        }*/
    }

    /* private void disconnectQueue() {
        try {
            connection.stop();
            connection.close();
        } catch (JMSException e) {
            LOG.error("[ Error when stopping or closing JMS queue. ] {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void connectQueue() throws JMSException {
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
    }*/
}
