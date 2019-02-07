package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.*;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.FANamespaceMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.FLUXMessageProducer;
import lombok.extern.slf4j.Slf4j;
import un.unece.uncefact.data.standard.fluxfaquerymessage._3.FLUXFAQueryMessage;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;

@MessageDriven(mappedName = MessageConstants.EVENT_BUS_TOPIC, activationConfig = {
                @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE),
                @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_DURABILITY_STR, propertyValue = MessageConstants.DURABLE_CONNECTION),
                @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_TOPIC),
                @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.EVENT_BUS_TOPIC_NAME),
                @ActivationConfigProperty(propertyName = MessageConstants.SUBSCRIPTION_NAME_STR, propertyValue = ActivityPluginConstants.SUBSCRIPTION_NAME_EV),
                @ActivationConfigProperty(propertyName = MessageConstants.CLIENT_ID_STR, propertyValue = ActivityPluginConstants.CLIENT_ID_EV),
                @ActivationConfigProperty(propertyName = MessageConstants.MESSAGE_SELECTOR_STR, propertyValue = ActivityPluginConstants.MESSAGE_SELECTOR_EV)
})
@Slf4j
public class PluginNameEventBusListener implements MessageListener {

    private static final String ISO_8859_1 = "ISO-8859-1";

    @EJB
    private StartupBean startup;

    @EJB
    private FLUXMessageProducer fluxMessageProducer;

    @Override
    public void onMessage(Message inMessage) {
        log.debug("Eventbus listener for fluxActivity (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            switch (request.getMethod()) {
                case SET_FLUX_RESPONSE:
                    log.info("[INFO] FLUXFAResponse Received in FLUX ACTIVITY PLUGIN.");
                    sendFaResponseToFluxTL(textMessage);
                    break;
                case SEND_FA_QUERY:
                    log.info("[INFO] FLUXFAQuery Received in FLUX ACTIVITY PLUGIN.");
                    sendFaQueryToFluTL(textMessage);
                    break;
                case SEND_FA_REPORT:
                    log.info("[INFO] FLUXFAReportMessage Received in FLUX ACTIVITY PLUGIN.");
                    sendFaReportToFluxTL(textMessage);
                    break;
                case SET_CONFIG :
                    SetConfigRequest setConfig = JAXBMarshaller.unmarshallTextMessage(textMessage, SetConfigRequest.class);
                    log.info("[CONFIG] Config(s) [{}] was correctly set.", setConfig.getConfigurations());
                    break;
                case START :
                    StartRequest startReq = JAXBMarshaller.unmarshallTextMessage(textMessage, StartRequest.class);
                    log.info("[STARTED] Plugin was started!");
                    break;
                default:
                    log.error("Not supported method : [ {} ] and request : [ {} ]", request.getMethod(), ((TextMessage) inMessage).getText());
                    break;
            }
        } catch (MessageException | JMSException e) {
            log.error("Not able to send message to FLUX", e);
        } catch (ExchangeModelMarshallException | NullPointerException e) {
            log.error("[ Error when receiving message in fluxActivity " + startup.getRegisterClassName() + " ]", e);
        } catch (PluginException e) {
            log.error("Not able Process message received in FLUXActivity Plugin", e);
        }
    }

    private void sendFaReportToFluxTL(TextMessage textMessage) throws ExchangeModelMarshallException, PluginException, MessageException {
        SetFLUXFAReportRequest fluxFaReportMessageRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAReportRequest.class);
        if (fluxFaReportMessageRequest == null || fluxFaReportMessageRequest.getResponse() == null) {
            throw new PluginException("Either SetFLUXFAResponseRequest is null or the message inside is null. ");
        }
        log.debug("[DEBUG] FLUXFAReportMessage message received in the Plugin is:" + fluxFaReportMessageRequest.getResponse());
        String fluxReportMessageStr = cleanFLUXReportMessage(fluxFaReportMessageRequest.getResponse());
        if (fluxReportMessageStr == null) {
            throw new PluginException("Cleaned FLUXResponseMessage is null. ");
        }
        fluxMessageProducer.sendModuleMessageWithProps(fluxReportMessageStr, null, fluxMessageProducer.getFLUXMessageProperties(fluxFaReportMessageRequest));
        log.info("[INFO] FLUXFAReportMessage message sent successfully to FLUX TL...");
    }

    private void sendFaQueryToFluTL(TextMessage textMessage) throws ExchangeModelMarshallException, PluginException, MessageException {
        SetFLUXFAQueryRequest setFaQueryRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAQueryRequest.class);
        if (setFaQueryRequest == null || setFaQueryRequest.getResponse() == null) {
            throw new PluginException("Either SetFLUXFAResponseRequest is null or the message inside is null. ");
        }
        log.debug("[DEBUG] FLUXFAQuery message received in the Plugin is:" + setFaQueryRequest.getResponse());
        String fluxFaQueryMessage = cleanFLUXQueryMessage(setFaQueryRequest.getResponse());
        if (fluxFaQueryMessage == null) {
            throw new PluginException("Cleaned FLUXResponseMessage is null. ");
        }
        fluxMessageProducer.sendModuleMessageWithProps(fluxFaQueryMessage, null, fluxMessageProducer.getFLUXMessageProperties(setFaQueryRequest));
        log.info("[INFO] FLUXFAQuery message sent successfully to FLUX TL...");
    }

    private void sendFaResponseToFluxTL(TextMessage textMessage) throws ExchangeModelMarshallException, PluginException, MessageException {
        SetFLUXFAResponseRequest fluxFAResponseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAResponseRequest.class);
        if (fluxFAResponseRequest == null || fluxFAResponseRequest.getResponse() == null) {
            throw new PluginException("Either SetFLUXFAResponseRequest is null or the message inside is null. ");
        }
        log.debug("[DEBUG] FLUXFAResponse message received in the Plugin is:" + fluxFAResponseRequest.getResponse());
        String fluxResponseMessage = cleanFLUXResponseMessage(fluxFAResponseRequest.getResponse());
        if (fluxResponseMessage == null) {
            throw new PluginException("Cleaned FLUXResponseMessage is null. ");
        }
//        /fluxMessageProducer.sendModuleMessageWithProps(fluxResponseMessage, null, fluxMessageProducer.getFLUXMessageProperties(fluxFAResponseRequest));
        log.info("[INFO] FLUXFAResponse message sent successfully to FLUX TL...");
    }

    private String cleanFLUXResponseMessage(String fluxFAResponse) {
        String cleanXMLMessage = null;
        if (fluxFAResponse == null) {
            log.error("fluxFAResponse received in clean method is null");
            return null;
        }
        try {
            FLUXResponseMessage fluxResponseMessage = JAXBUtils.unMarshallMessage(fluxFAResponse, FLUXResponseMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage, ISO_8859_1, true, new FANamespaceMapper());
            log.debug(String.format("Cleaned FLUXResponse :%s", cleanXMLMessage));
        } catch (JAXBException e) {
            log.error("PluginException when trying to clean FLUXResponse", e);
        }
        return cleanXMLMessage;
    }

    private String cleanFLUXReportMessage(String faReportMessageWrapper) {
        String cleanXMLMessage = null;
        if (faReportMessageWrapper == null){
            log.debug(String.format("Cleaned FLUXResponse :%s", cleanXMLMessage));
            return null;
        }
        try {
            FLUXFAReportMessage fluxResponseMessage = JAXBUtils.unMarshallMessage(faReportMessageWrapper, FLUXFAReportMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage, ISO_8859_1, true, new FANamespaceMapper());
            log.debug(String.format("Cleaned FLUXResponse :%s", cleanXMLMessage));
        } catch (JAXBException e) {
            log.error("PluginException when trying to clean FLUXFAReportMessage", e);
        }
        return cleanXMLMessage;
    }

    private String cleanFLUXQueryMessage(String faQueryMessageWrapper) {
        String cleanXMLMessage = null;
        if (faQueryMessageWrapper == null) {
            log.error("faQueryMessageWrapper received in clean method is null");
            return null;
        }
        try {
            FLUXFAQueryMessage fluxResponseMessage = JAXBUtils.unMarshallMessage(faQueryMessageWrapper, FLUXFAQueryMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage, ISO_8859_1, true, new FANamespaceMapper());
            log.debug(String.format("Cleaned FLUXResponse :%s", cleanXMLMessage));
        } catch (JAXBException e) {
            log.error("PluginException when trying to clean FLUXFAQueryMessage", e);
        }
        return cleanXMLMessage;
    }

}
