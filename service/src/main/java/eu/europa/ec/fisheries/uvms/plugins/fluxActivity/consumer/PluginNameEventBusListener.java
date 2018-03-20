package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAQueryRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAReportRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAResponseRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityPluginConstatns;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.mapper.PluginJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.FLUXMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import un.unece.uncefact.data.standard.fluxfaquerymessage._3.FLUXFAQueryMessage;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;

@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_NAME_STR, propertyValue = ActivityPluginConstatns.SUBSCRIPTION_NAME_EV),
        @ActivationConfigProperty(propertyName = MessageConstants.CLIENT_ID_STR, propertyValue = ActivityPluginConstatns.CLIENT_ID_EV),
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGE_SELECTOR_STR, propertyValue = ActivityPluginConstatns.MESSAGE_SELECTOR_EV)
})
public class PluginNameEventBusListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(PluginNameEventBusListener.class);

    @EJB
    private StartupBean startup;

    @EJB
    private FLUXMessageProducer fluxMessageProducer;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {
        LOG.debug("Eventbus listener for fluxActivity (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            switch (request.getMethod()) {
                case SET_FLUX_RESPONSE:
                    LOG.info("[INFO] FLUXFAResponse Received in FLUX ACTIVITY PLUGIN.");
                    sendFaResponseToFluxTL(textMessage);
                    break;
                case SEND_FA_QUERY:
                    LOG.info("[INFO] FLUXFAQuery Received in FLUX ACTIVITY PLUGIN.");
                    sendFaQueryToFluTL(textMessage);
                    break;
                case SEND_FA_REPORT:
                    LOG.info("[INFO] FLUXFAReportMessage Received in FLUX ACTIVITY PLUGIN.");
                    sendFaReportToFluxTL(textMessage);
                    break;
                default:
                    LOG.error("Not supported method");
                    break;
            }
        } catch (MessageException e) {
            LOG.error("Not able to send message to FLUX", e);
        } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in fluxActivity " + startup.getRegisterClassName() + " ]", e);
        } catch (PluginException e) {
            LOG.error("Not able Process message received in FLUXActivity Plugin", e);
        }
    }

    private void sendFaReportToFluxTL(TextMessage textMessage) throws ExchangeModelMarshallException, PluginException, MessageException {
        SetFLUXFAReportRequest fluxFaReportMessageRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAReportRequest.class);
        if (fluxFaReportMessageRequest == null || fluxFaReportMessageRequest.getResponse() == null) {
            throw new PluginException("Either SetFLUXFAResponseRequest is null or the message inside is null. ");
        }
        LOG.debug("[DEBUG] FLUXFAReportMessage message received in the Plugin is:" + fluxFaReportMessageRequest.getResponse());
        String fluxReportMessageStr = cleanFLUXReportMessage(fluxFaReportMessageRequest.getResponse());
        if (fluxReportMessageStr == null) {
            throw new PluginException("Cleaned FLUXResponseMessage is null. ");
        }
        fluxMessageProducer.sendModuleMessageWithProps(fluxReportMessageStr, null, fluxMessageProducer.getFLUXMessageProperties(fluxFaReportMessageRequest));
        LOG.info("[INFO] FLUXFAReportMessage message sent successfully to FLUX TL...");
    }

    private void sendFaQueryToFluTL(TextMessage textMessage) throws ExchangeModelMarshallException, PluginException, MessageException {
        SetFLUXFAQueryRequest setFaQueryRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAQueryRequest.class);
        if (setFaQueryRequest == null || setFaQueryRequest.getResponse() == null) {
            throw new PluginException("Either SetFLUXFAResponseRequest is null or the message inside is null. ");
        }
        LOG.debug("[DEBUG] FLUXFAQuery message received in the Plugin is:" + setFaQueryRequest.getResponse());
        String fluxFaQueryMessage = cleanFLUXQueryMessage(setFaQueryRequest.getResponse());
        if (fluxFaQueryMessage == null){
            throw new PluginException("Cleaned FLUXResponseMessage is null. ");
        }
        fluxMessageProducer.sendModuleMessageWithProps(fluxFaQueryMessage, null, fluxMessageProducer.getFLUXMessageProperties(setFaQueryRequest));
        LOG.info("[INFO] FLUXFAQuery message sent successfully to FLUX TL...");
    }

    private void sendFaResponseToFluxTL(TextMessage textMessage) throws ExchangeModelMarshallException, PluginException, MessageException {
        SetFLUXFAResponseRequest fluxFAResponseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAResponseRequest.class);
        if (fluxFAResponseRequest == null || fluxFAResponseRequest.getResponse() == null) {
            throw new PluginException("Either SetFLUXFAResponseRequest is null or the message inside is null. ");
        }
        LOG.debug("[DEBUG] FLUXFAResponse message received in the Plugin is:" + fluxFAResponseRequest.getResponse());
        String fluxResponseMessage = cleanFLUXResponseMessage(fluxFAResponseRequest.getResponse());
        if (fluxResponseMessage == null) {
            throw new PluginException("Cleaned FLUXResponseMessage is null. ");
        }
        fluxMessageProducer.sendModuleMessageWithProps(fluxResponseMessage, null, fluxMessageProducer.getFLUXMessageProperties(fluxFAResponseRequest));
        LOG.info("[INFO] FLUXFAResponse message sent successfully to FLUX TL...");
    }

    private String cleanFLUXResponseMessage(String fluxFAResponse) {
        String cleanXMLMessage = null;
        if(fluxFAResponse ==null){
            LOG.error("fluxFAResponse received in clean method is null");
            return null;
        }
        try {
            FLUXResponseMessage fluxResponseMessage = PluginJAXBMarshaller.unMarshallMessage(fluxFAResponse, FLUXResponseMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage);
            LOG.info("Cleaned FLUXResponse :" + cleanXMLMessage);
        } catch (PluginException | JAXBException e) {
            LOG.error("PluginException when trying to clean FLUXResponse", e);
        }
        return cleanXMLMessage;
    }

    private String cleanFLUXReportMessage(String faReportMessageWrapper) {
        String cleanXMLMessage = null;
        if(faReportMessageWrapper ==null){
            LOG.error("faReportMessageWrapper received in clean method is null");
            return null;
        }
        try {
            FLUXFAReportMessage fluxResponseMessage = PluginJAXBMarshaller.unMarshallMessage(faReportMessageWrapper, FLUXFAReportMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage);
            LOG.info("Cleaned FLUXFAReportMessage :" + cleanXMLMessage);
        } catch (PluginException | JAXBException e) {
            LOG.error("PluginException when trying to clean FLUXFAReportMessage", e);
        }
        return cleanXMLMessage;
    }

    private String cleanFLUXQueryMessage(String faQueryMessageWrapper) {
        String cleanXMLMessage = null;
        if(faQueryMessageWrapper ==null){
            LOG.error("faQueryMessageWrapper received in clean method is null");
            return null;
        }
        try {
            FLUXFAQueryMessage fluxResponseMessage = PluginJAXBMarshaller.unMarshallMessage(faQueryMessageWrapper, FLUXFAQueryMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage);
            LOG.info("Cleaned FLUXFAQueryMessage :" + cleanXMLMessage);
        } catch (PluginException | JAXBException e) {
            LOG.error("PluginException when trying to clean FLUXFAQueryMessage", e);
        }
        return cleanXMLMessage;
    }

/*    private String cleanFLUXResponseMessage(String rawMsg, Class<?> clazz) {
        String cleanXMLMessage = null;
        if (rawMsg == null) {
            LOG.error("fluxFAResponse received in clean method is null");
            return null;
        }
        try {
            cleanXMLMessage = PluginJAXBMarshaller.marshallJaxBObjectToString(PluginJAXBMarshaller.unMarshallMessage(rawMsg, clazz.getClass()));
            LOG.info("Cleaned FLUXResponse :" + cleanXMLMessage);
        } catch (PluginException e) {
            LOG.error("PluginException when trying to clean FLUXResponseMessage", e);
        }
        return cleanXMLMessage;
    }*/
}
