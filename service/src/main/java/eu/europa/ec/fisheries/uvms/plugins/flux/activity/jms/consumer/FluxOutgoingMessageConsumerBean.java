package eu.europa.ec.fisheries.uvms.plugins.flux.activity.jms.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.*;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingListType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.PortInitiator;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants.ActivityPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception.MappingException;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.jms.producer.FLUXMessageProducerBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.jms.producer.PluginToExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxfaquerymessage._3.FLUXFAQueryMessage;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;
import xeu.connector_bridge.v1.AssignedONType;
import xeu.connector_bridge.v1.PostMsgOutType;
import xeu.connector_bridge.v1.PostMsgType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.List;

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
public class FluxOutgoingMessageConsumerBean implements MessageListener {

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CONNECTOR_ID = "connectorID";
    private static final String ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX = "[ Error when sending activity report to FLUX. ] {}";

    @EJB
    private PortInitiator portInitiator;

    @EJB
    private StartupBean startupBean;

    @EJB
    private FLUXMessageProducerBean jmsProducer;

    @EJB
    private PluginToExchangeProducer exchangeProducer;

    @Override
    public void onMessage(Message inMessage) {
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            int timesRedelivered = getTimesRedelivered(inMessage);
            if (timesRedelivered > 0) {
                log.info("Received method {}, redelivered {} times", request.getMethod(), getTimesRedelivered(inMessage));
            }
            switch (request.getMethod()) {
                case SEND_FA_REPORT:
                    SetFLUXFAReportRequest activityReportRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAReportRequest.class);
                    sendActivityReportToFlux(activityReportRequest);
                    break;
                case SET_FLUX_RESPONSE:
                    SetFLUXFAResponseRequest activityResponseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAResponseRequest.class);
                    sendActivityResponseToFlux(activityResponseRequest);
                    break;
                case SEND_FA_QUERY:
                    SetFLUXFAQueryRequest activityQueryRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAQueryRequest.class);
                    sendActivityQueryToFlux(activityQueryRequest);
                    break;
                case SET_CONFIG:
                    SetConfigRequest setConfig = JAXBMarshaller.unmarshallTextMessage(textMessage, SetConfigRequest.class);
                    SettingListType configurations = setConfig.getConfigurations();
                    if (configurations != null) {
                        List<SettingType> settings = configurations.getSetting();
                        if (CollectionUtils.isNotEmpty(settings)) {
                            for (SettingType setting : settings) {
                                String settingKey = setting.getKey();
                                String settingValue = setting.getValue();
                                if (settingKey.endsWith("FLUX_ENDPOINT")) {
                                    if (StringUtils.isNotEmpty(settingValue)) {
                                        portInitiator.setupPort(settingValue);
                                        portInitiator.setWsIsSetup(true);
                                        log.info("Activated the WS OUT service with endpoint : [{}]", settingValue);
                                    } else {
                                        log.warn("WS OUT endpoint has been deactivated since the endpont value is NULL!");
                                        portInitiator.setWsIsSetup(false);
                                    }
                                }
                                log.info("[CONFIG] Config(s) [{}:{}] was correctly set.", settingKey, settingValue);
                            }
                        }
                    }
                    break;
                case START:
                    StartRequest startReq = JAXBMarshaller.unmarshallTextMessage(textMessage, StartRequest.class);
                    log.info("[STARTED] Plugin was started!");
                    break;
                default:
                    log.error("Not supported method");
                    break;
            }
        } catch (ExchangeModelMarshallException | NullPointerException ex) {
            log.error("Error when receiving message in flux " + startupBean.getRegisterClassName() , ex);
        } catch (PluginException ex) {
            log.error("Error when handling JMS message in flux " + startupBean.getRegisterClassName() , ex);
        }
    }

    private int getTimesRedelivered(Message message) {
        try {
            return (message.getIntProperty("JMSXDeliveryCount") - 1);
        } catch (Exception e) {
            log.error("Could not retrieve property JMSXDeliveryCount",e);
            return 0;
        }
    }

    private void sendActivityReportToFlux(SetFLUXFAReportRequest request) throws PluginException {
        try {
            postRequest(request, ActivityType.FA_REPORT);
        } catch (MappingException | DatatypeConfigurationException | JAXBException | MessageException ex) {
            throw new PluginException(ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX,ex);
        }
    }

    private void sendActivityResponseToFlux(SetFLUXFAResponseRequest request) throws PluginException {
        try {
            postRequest(request, ActivityType.FA_RESPONSE);
        } catch (MappingException | DatatypeConfigurationException | JAXBException | MessageException ex) {
            throw new PluginException(ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX,ex);
        }
    }

    private void sendActivityQueryToFlux(SetFLUXFAQueryRequest request) throws PluginException {
        try {
            postRequest(request, ActivityType.FA_QUERY);
        } catch (MappingException | JAXBException | DatatypeConfigurationException | MessageException ex) {
            throw new PluginException(ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX,ex);
        }
    }

    private void postRequest(PluginBaseRequest request, ActivityType msgType) throws JAXBException, DatatypeConfigurationException, MappingException, MessageException {
        if (portInitiator.isWsIsSetup()) {
            sendMessageThroughWS(request, msgType);
        } else {
            jmsProducer.sendMessageToBridgeQueue(request, msgType);
        }
    }

    private void sendMessageThroughWS(PluginBaseRequest request, ActivityType msgType) throws DatatypeConfigurationException, MappingException, JAXBException {
        log.info("[WEBSERVICE] Sending message outside of FLUX-FMC through :::-->>> WS\n\n");
        PostMsgType postMsgType = getPostMsgType(request, msgType);
        int waitingTimes = 120;
        while (portInitiator.isWaitingForUrlConfigProperty() && waitingTimes > 0) {
            try {
                log.warn("Webservice needs to wait for the URL to be set up. Waiting for the {} time (MAX 60 Times)", waitingTimes);
                Thread.sleep(1000);
                waitingTimes--;
            } catch (InterruptedException ignored) {
                log.error("thread interruption exception",ignored);
                Thread.currentThread().interrupt();
            }
        }
        BridgeConnectorPortType port = portInitiator.getPort();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(CONNECTOR_ID, startupBean.getSetting(CLIENT_ID));
        String endPoint = ((BindingProvider) port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY).toString();
        try {
            PostMsgOutType post = port.post(postMsgType);
            List<AssignedONType> assignedON = post.getAssignedON();
            upgradeResponseWithOnMessage(assignedON,request.getResponseLogGuid());
            log.info("[INFO] Outgoing message ({}) with ON :[{}] send to [{}]", msgType, request.getOnValue(), endPoint);
        } catch (WebServiceException | NullPointerException ex) {
            log.error("[ERROR] Couldn't send message to endpoint: " + endPoint, ex);
        }
    }

    private void upgradeResponseWithOnMessage(List<AssignedONType> assignedOn,String responseGuid)  {
       String onValue = assignedOn.isEmpty()? null:assignedOn.get(0).getON();
        String stringMessage = null;
        try {
            stringMessage = ExchangeModuleRequestMapper.createUpdateOnMessageRequest(onValue, responseGuid);
            exchangeProducer.sendModuleMessage(stringMessage, null);
        } catch (ExchangeModelMarshallException | MessageException e) {
            log.error("Couldn't send message: " + stringMessage + " with onValue: " + onValue,e);
        }

    }

    private PostMsgType getPostMsgType(PluginBaseRequest request, ActivityType msgType) throws DatatypeConfigurationException, MappingException, JAXBException {
        PostMsgType postMsgType = new PostMsgType();
        postMsgType.setAD(request.getDestination());
        postMsgType.setDF(request.getFluxDataFlow());
        String response;
        if (ActivityType.FA_RESPONSE.equals(msgType)) {
            response = ((SetFLUXFAResponseRequest) request).getResponse();
            postMsgType.setAny(marshalToDOM(JAXBUtils.unMarshallMessage(response, FLUXResponseMessage.class)));
        } else if (ActivityType.FA_QUERY.equals(msgType)) {
            response = ((SetFLUXFAQueryRequest) request).getResponse();
            postMsgType.setAny(marshalToDOM(JAXBUtils.unMarshallMessage(response, FLUXFAQueryMessage.class)));
        } else if (ActivityType.FA_REPORT.equals(msgType)) {
            response = ((SetFLUXFAReportRequest) request).getResponse();
            postMsgType.setAny(marshalToDOM(JAXBUtils.unMarshallMessage(response, FLUXFAReportMessage.class)));
        }
        return postMsgType;
    }

    private Element marshalToDOM(Object toBeWrapped) throws MappingException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(toBeWrapped.getClass());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.newDocument();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(toBeWrapped, document);
            return document.getDocumentElement();
        } catch (ParserConfigurationException | JAXBException e) {
            throw new MappingException("Could not wrap object " + toBeWrapped + " in post msg", e);
        }
    }
}
