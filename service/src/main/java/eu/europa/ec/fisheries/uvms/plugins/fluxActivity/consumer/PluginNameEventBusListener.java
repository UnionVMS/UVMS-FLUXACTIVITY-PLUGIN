package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAResponseRequest;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityPluginConstatns;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.exception.PluginException;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.FluxMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
    StartupBean startup;

    @EJB
    FluxMessageProducer fluxMessageProducer;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.debug("Eventbus listener for fluxActivity (MessageConstants.PLUGIN_SERVICE_CLASS_NAME): {}", startup.getRegisterClassName());

        TextMessage textMessage = (TextMessage) inMessage;

        try {

            PluginBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, PluginBaseRequest.class);

            String responseMessage = null;

            switch (request.getMethod()) {

                case SET_FLUX_RESPONSE:
                    LOG.info("--FLUXFAResponse Received in FLUX ACTIVITY PLUGIN.");
                    SetFLUXFAResponseRequest fluxFAResponseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, SetFLUXFAResponseRequest.class);
                    responseMessage =fluxFAResponseRequest.getResponse();
                    fluxMessageProducer.readJMSPropertiesFromExchangeResponse(fluxFAResponseRequest); // Initialize JMS Properties before sending message to FLUXQueue
                    LOG.debug("--FLUXFAResponse message received in the Plugin is:"+responseMessage);
                    break;
                default:
                    LOG.error("Not supported method");
                    break;
            }


            fluxMessageProducer.sendModuleMessage(cleanFLUXResponseMessage(responseMessage),null);
            LOG.info("--FLUXFAResponse message sent successfully to FLUX");

        }catch (MessageException e) {
            LOG.error("Not able to send message to FLUX",e);
        }
        catch (ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message in fluxActivity " + startup.getRegisterClassName() + " ]", e);
        }
    }

    private String cleanFLUXResponseMessage(String fluxFAResponse){
        String cleanXMLMessage=null;

        try {
            FLUXResponseMessage fluxResponseMessage = eu.europa.ec.fisheries.uvms.plugins.fluxActivity.mapper.JAXBMarshaller.unMarshallMessage(fluxFAResponse, FLUXResponseMessage.class);
            cleanXMLMessage =JAXBMarshaller.marshallJaxBObjectToString(fluxResponseMessage);
            LOG.info("Cleaned FLUXResponse :"+cleanXMLMessage);
        } catch (PluginException e) {
            LOG.error("PluginException when trying to clean FLUXResponseMessage",e);
        } catch (ExchangeModelMarshallException e) {
            LOG.error("ExchangeModelMarshallException when trying to clean FLUXResponseMessage",e);
        }

        return cleanXMLMessage;
    }
}
