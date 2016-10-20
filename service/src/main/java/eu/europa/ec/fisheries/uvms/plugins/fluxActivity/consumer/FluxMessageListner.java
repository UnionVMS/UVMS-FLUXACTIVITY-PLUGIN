package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;


import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;

import javax.ejb.EJB;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;


/*@MessageDriven(mappedName = FluxPluginConstants.FLUX_MESSAGE_IN_QUEUE_NEW_NAME,  activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = FluxPluginConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = FluxPluginConstants.FLUX_MESSAGE_IN_QUEUE_NEW),
        @ActivationConfigProperty(propertyName = "connectionFactoryLookup", propertyValue = FluxPluginConstants.FLUX_CONNECTION_FACTORY)
})*/
public class FluxMessageListner implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(FluxMessageListner.class);

    @EJB
    ExchangeService exchange;
    @EJB
    StartupBean startupService;

    @EJB
    PluginService fluxService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.info("Rachana Flux plugin receiver got flux message");

        TextMessage textMessage = (TextMessage) inMessage;

        try {
           if(tryConsumeFAReportMessage(textMessage) ==null){
               LOG.info("Unable to read the xml message. Incompatible message.");
           }
            LOG.info("message sent successfully");
            exchange.sendFLUXFAReportMessageReportToExchange(textMessage.getText());


        } catch (JMSException e) {
            LOG.error("Error while trying to send Flux FAReport message to exchange",e);

        }
    }


    private ExchangeRegistryBaseRequest tryConsumeFAReportMessage(TextMessage textMessage) {
        try {

            return JAXBMarshaller.unmarshallTextMessage(textMessage, FLUXFAReportMessage.class);
        } catch (ExchangeModelMarshallException e) {
            LOG.error("Error while trying read FLUXFA ReportEMssage. Format of xml is incorrect.",e);
            return null;
        }
    }
}
