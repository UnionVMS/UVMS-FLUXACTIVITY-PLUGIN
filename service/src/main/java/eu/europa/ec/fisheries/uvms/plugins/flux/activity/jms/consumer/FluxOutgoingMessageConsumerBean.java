package eu.europa.ec.fisheries.uvms.plugins.flux.activity.jms.consumer;

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
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.*;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingListType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.PortInitiator;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants.ActivityPluginConstants;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception.MappingException;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception.PluginException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxfaquerymessage._3.FLUXFAQueryMessage;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;
import xeu.connector_bridge.v1.PostMsgType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;

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

    @Override
    public void onMessage(Message inMessage) {
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);
            int timesRedelivered = getTimesRedelivered(inMessage);
            if (timesRedelivered > 0){
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
                case SET_CONFIG :
                    SetConfigRequest setConfig = JAXBMarshaller.unmarshallTextMessage(textMessage, SetConfigRequest.class);
                    SettingListType configurations = setConfig.getConfigurations();
                    if (configurations != null){
                        List<SettingType> settings = configurations.getSetting();
                        if (CollectionUtils.isNotEmpty(settings)){
                            for (SettingType setting : settings) {
                                if (setting.getKey().contains("FLUX_ENDPOINT")){
                                    portInitiator.setupPort(setting.getValue());
                                }
                                log.info("[CONFIG] Config(s) [{}:{}] was correctly set.", setting.getKey(), setting.getValue());
                            }
                        }
                    }
                    break;
                case START :
                    StartRequest startReq = JAXBMarshaller.unmarshallTextMessage(textMessage, StartRequest.class);
                    log.info("[STARTED] Plugin was started!");
                    break;
                default:
                    log.error("Not supported method");
                    break;
            }
        } catch (ExchangeModelMarshallException | NullPointerException ex) {
            log.error("[ Error when receiving message in flux " + startupBean.getRegisterClassName() + " ]", ex);
        } catch (PluginException ex) {
            log.error("[ Error when handling JMS message in flux " + startupBean.getRegisterClassName() + " ]", ex);
        }
    }

    private void sendActivityQueryToFlux(SetFLUXFAQueryRequest request) throws PluginException {
        try {
            String adValue = request.getDestination();
            String dfValue = request.getFluxDataFlow();
            postRequest(getPostMsgType(request, adValue, dfValue), request.getOnValue());
        } catch (MappingException | JAXBException | DatatypeConfigurationException ex) {
            log.error(ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX, ex.getMessage());
            throw new PluginException(ex);
        }
    }

    private int getTimesRedelivered(Message message) {
        try {
            return (message.getIntProperty("JMSXDeliveryCount") - 1);
        } catch (Exception e) {
            return 0;
        }
    }

    private void postRequest(PostMsgType postMsgType, String onValue) {
        while(portInitiator.isWaitingForUrlConfigProperty()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        BridgeConnectorPortType port = portInitiator.getPort();
        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(CONNECTOR_ID, startupBean.getSetting(CLIENT_ID));
        String endPoint = ((BindingProvider) port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY).toString();
        try {
            port.post(postMsgType);
            log.info("Message {} send to {}", onValue, endPoint);
        } catch (WebServiceException ex){
            log.error("Couldn't send message to {}", endPoint, ex.getCause());
        }
    }

    private void sendActivityReportToFlux(SetFLUXFAReportRequest request) throws PluginException {
        try {
            String adValue = request.getDestination();
            String dfValue = request.getFluxDataFlow();
            postRequest(getPostMsgType(request, adValue, dfValue), request.getOnValue());
        } catch (MappingException | DatatypeConfigurationException | JAXBException ex) {
            log.error(ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX, ex.getMessage());
            throw new PluginException(ex);
        }
    }

    private void sendActivityResponseToFlux(SetFLUXFAResponseRequest request) throws PluginException {
        try {
            String adValue = request.getDestination();
            String dfValue = request.getFluxDataFlow();
            postRequest(getPostMsgType(request, adValue, dfValue), request.getOnValue());
        } catch (MappingException | DatatypeConfigurationException | JAXBException ex) {
            log.error(ERROR_WHEN_SENDING_ACTIVITY_REPORT_TO_FLUX, ex.getMessage());
            throw new PluginException(ex);
        }
    }

    private PostMsgType getPostMsgType(PluginBaseRequest request, String adValue, String dfValue) throws DatatypeConfigurationException, MappingException, JAXBException {
        PostMsgType postMsgType = new PostMsgType();
        postMsgType.setAD(adValue);
        postMsgType.setDF(dfValue);
        postMsgType.setAR(true);
        postMsgType.setTO(1234);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        postMsgType.setTODT(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
        String response;
        if (request instanceof SetFLUXFAResponseRequest){
            response = ((SetFLUXFAResponseRequest) request).getResponse();
            postMsgType.setAny(marshalToDOM(JAXBUtils.unMarshallMessage(response, FLUXResponseMessage.class)));
        } else if (request instanceof SetFLUXFAQueryRequest){
            response = ((SetFLUXFAQueryRequest) request).getResponse();
            postMsgType.setAny(marshalToDOM(JAXBUtils.unMarshallMessage(response, FLUXFAQueryMessage.class)));
        } else if (request instanceof SetFLUXFAReportRequest){
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