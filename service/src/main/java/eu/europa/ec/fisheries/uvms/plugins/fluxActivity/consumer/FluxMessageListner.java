package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;


import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.FluxPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


@MessageDriven(mappedName = FluxPluginConstants.FLUX_MESSAGE_IN_REMOTE_QUEUE_NAME,  activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = FluxPluginConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = FluxPluginConstants.FLUX_MESSAGE_IN_REMOTE_QUEUE),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = FluxPluginConstants.FLUX_CONNECTION_FACTORY)
})
public class FluxMessageListner implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(FluxMessageListner.class);

    @EJB
    ExchangeService exchange;


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.info("------Received FAReportMessage in ERS Activity plugin from FLUX--------");

        TextMessage textMessage = (TextMessage) inMessage;

        try {

            LOG.debug("Received FAReportMessage :"+textMessage.getText());
            exchange.sendFLUXFAReportMessageReportToExchange(textMessage.getText());
            LOG.info("message sent successfully to exchange module");


        } catch (Exception e) {
            LOG.error("Error while trying to send Flux FAReport message to exchange",e);
        }
    }


}
