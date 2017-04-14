package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeType;
import eu.europa.ec.fisheries.schema.exchange.common.v1.AcknowledgeTypeType;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PingRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetCommandRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetConfigRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAResponseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetReportRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangePluginResponseMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityPluginConstatns;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.PluginMessageProducer;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.PluginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@MessageDriven(mappedName = ExchangeModelConstants.PLUGIN_EVENTBUS, activationConfig = {
        @ActivationConfigProperty(propertyName = "messagingType",          propertyValue = ExchangeModelConstants.CONNECTION_TYPE),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = ActivityPluginConstatns.DURABLE),
        @ActivationConfigProperty(propertyName = "destinationType",        propertyValue = ExchangeModelConstants.DESTINATION_TYPE_TOPIC),
        @ActivationConfigProperty(propertyName = "destination",            propertyValue = ExchangeModelConstants.EVENTBUS_NAME),
        @ActivationConfigProperty(propertyName = "subscriptionName",       propertyValue = ActivityPluginConstatns.SUBSCRIPTION_NAME_EV),
        @ActivationConfigProperty(propertyName = "clientId",               propertyValue = ActivityPluginConstatns.CLIENT_ID_EV),
        @ActivationConfigProperty(propertyName = "messageSelector",        propertyValue = ActivityPluginConstatns.MESSAGE_SELECTOR_EV)
})
public class PluginNameEventBusListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(PluginNameEventBusListener.class);

    @EJB
    PluginService service;

    @EJB
    PluginMessageProducer messageProducer;

    @EJB
    StartupBean startup;

      @EJB
    MessageProducer fluxMessageProducer;
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.debug("Eventbus listener for fluxActivity (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());

        TextMessage textMessage = (TextMessage) inMessage;

        try {

            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);

            String responseMessage = null;

            switch (request.getMethod()) {
                case SET_CONFIG:
                    SetConfigRequest setConfigRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetConfigRequest.class);
                    AcknowledgeTypeType setConfig = service.setConfig(setConfigRequest.getConfigurations());
                    AcknowledgeType setConfigAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), setConfig);
                    responseMessage = ExchangePluginResponseMapper.mapToSetConfigResponse(startup.getRegisterClassName(), setConfigAck);
                    break;
                case SET_COMMAND:
                    SetCommandRequest setCommandRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetCommandRequest.class);
                    AcknowledgeTypeType setCommand = service.setCommand(setCommandRequest.getCommand());
                    AcknowledgeType setCommandAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), setCommandRequest.getCommand().getUnsentMessageGuid(), setCommand);
                    responseMessage = ExchangePluginResponseMapper.mapToSetCommandResponse(startup.getRegisterClassName(), setCommandAck);
                    break;
                case SET_REPORT:
                    SetReportRequest setReportRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetReportRequest.class);
                    AcknowledgeTypeType setReport = service.setReport(setReportRequest.getReport());
                    AcknowledgeType setReportAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), setReportRequest.getReport().getUnsentMessageGuid(), setReport);
                    responseMessage = ExchangePluginResponseMapper.mapToSetReportResponse(startup.getRegisterClassName(), setReportAck);
                    break;
                case START:
                    //StartRequest startRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, StartRequest.class);
                    AcknowledgeTypeType start = service.start();
                    AcknowledgeType startAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), start);
                    responseMessage = ExchangePluginResponseMapper.mapToStartResponse(startup.getRegisterClassName(), startAck);
                    break;
                case STOP:
                    //StopRequest stopRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, StopRequest.class);
                    AcknowledgeTypeType stop = service.stop();
                    AcknowledgeType stopAck = ExchangePluginResponseMapper.mapToAcknowlegeType(textMessage.getJMSMessageID(), stop);
                    responseMessage = ExchangePluginResponseMapper.mapToStopResponse(startup.getRegisterClassName(), stopAck);
                    break;
                case PING:
                    PingRequest pingRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, PingRequest.class);
                    LOG.info(pingRequest.toString());
                    responseMessage = ExchangePluginResponseMapper.mapToPingResponse(startup.isIsEnabled(), startup.isIsEnabled());
                    break;

                case SET_FLUX_RESPONSE:
                    LOG.info("--FLUXFAResponse Received in FLUX ACTIVITY PLUGIN.");
                   SetFLUXFAResponseRequest fluxFAResponseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAResponseRequest.class);
                   try {
                        fluxMessageProducer.sendModuleMessage(fluxFAResponseRequest.getResponse(),null);
                        LOG.info("--FLUXFAResponse message sent successfully to FLUX");
                   } catch (MessageException e) {
                        LOG.error("Not able to send message to FLUX");
                    }
                    break;
                default:
                    LOG.error("Not supported method");
                    break;
            }

            messageProducer.sendResponseMessage(responseMessage, textMessage);

        } catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in fluxActivity " + startup.getRegisterClassName() + " ]", e);
        } catch (JMSException ex) {
            LOG.error("[ Error when handling JMS message in fluxActivity " + startup.getRegisterClassName() + " ]", ex);
        }
    }
}
