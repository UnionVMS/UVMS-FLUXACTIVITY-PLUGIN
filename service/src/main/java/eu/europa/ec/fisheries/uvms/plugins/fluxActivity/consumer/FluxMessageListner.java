package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginFault;
import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.FluxPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.ExchangeService;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = FluxPluginConstants.FLUX_MESSAGE_IN_QUEUE, activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = FluxPluginConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = FluxPluginConstants.QUEUE_FLUX_RECEIVER_NAME)
})
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
            LOG.info("textMessage flux plugin :"+textMessage.getText());
        } catch (JMSException e) {
            LOG.error("[ Error when receiving textMessage in flux ]", e);
            e.printStackTrace();
        }

        try {

           // RequestType requestType = JAXBMarshaller.unmarshallTextMessage(textMessage, RequestType.class);
        //  FLUXFAReportMessage fluxFAReportMessage = JAXBMarshaller.unmarshallTextMessage(textMessage, FLUXFAReportMessage.class);

      /*  JAXBContext jc =JAXBContext.newInstance(FLUXFAReportMessageType.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            StringReader sr = new StringReader(textMessage.getText());
            StreamSource source = new StreamSource(sr);

            JAXBElement<FLUXFAReportMessageType> root = unmarshaller.unmarshal(source,FLUXFAReportMessageType.class);
            FLUXFAReportMessageType fluxFAReportMessage =root.getValue();
           // FLUXFAReportMessage fluxFAReportMessage = FluxMessageResponseMapper.extractFLUXFAReportMessage(requestType.getAny());
            LOG.info("sending flux message to exchange"); */
           //   exchange.sendFLUXFAReportMessageReportToExchange(fluxFAReportMessage);
            LOG.info("message sent successfully");
            exchange.sendFLUXFAReportMessageReportToExchange(textMessage.getText());

     /*  } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in flux ]", e);
        } catch (JAXBException e) {
            e.printStackTrace();*/
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void handlePluginFault(PluginFault fault) {
        LOG.error(startupService.getPluginResponseSubscriptionName() + " received fault " + fault.getCode() + " : " + fault.getMessage());
    }

    private ExchangeRegistryBaseRequest tryConsumeRegistryBaseRequest(TextMessage textMessage) {
        try {
            return JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
        } catch (ExchangeModelMarshallException e) {
            return null;
        }
    }
}
