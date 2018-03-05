/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.ExchangeMessageProperties;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.parser.SaxParserUUIDExtractor;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.parser.UUIDSAXException;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.FluxFaPluginExchangeService;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

/**
 * Created by sanera on 14/08/2017.
 */
@MessageDriven(mappedName = MessageConstants.QUEUE_FLUX_FA_MESSAGE_IN, activationConfig = {
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_TYPE_STR, propertyValue = MessageConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = MessageConstants.DESTINATION_STR, propertyValue = MessageConstants.QUEUE_FLUX_FA_MESSAGE_IN_NAME),
        @ActivationConfigProperty(propertyName = MessageConstants.MESSAGING_TYPE_STR, propertyValue = MessageConstants.CONNECTION_TYPE)
})
@Slf4j
public class FluxTlMessageConsumer implements MessageListener {

    private static final String FLUXFAREPORT_MESSAGE_START_XSD_ELEMENT = "FLUXFAReportMessage";
    private static final String FLUXFAQUERY_MESSAGE_START_XSD_ELEMENT = "FLUXFAQueryMessage";
    private static final String FLUXRESPONSE_MESSAGE_START_XSD_ELEMENT = "FLUXResponseMessage";

    private static final String exchangeUsername = "flux";
    private static final String DF = "DF";
    private static final String FR = "FR";
    private static final String ON = "ON";

    @EJB
    private FluxFaPluginExchangeService exchangeService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {
        log.info("[INFO] Received Message in UVMSFAPluginEvent Queue from FLUX.");
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            if (textMessage == null || textMessage.getText() == null) {
                throw new IllegalArgumentException("Message received in ERS Plugin is null.");
            }
            final ActivityType faMessageType = extractActivityTypeFromMessage(textMessage.getText());
            exchangeService.sendFishingActivityMessageToExchange(textMessage.getText(),
                    createExchangeMessagePropertiesForFluxFAReportRequest(textMessage, faMessageType),
                    faMessageType);
        } catch (Exception e) {
            log.error("[ERROR] Error while trying to send Flux FAReport message to exchange", e);
        }
    }


    private ActivityType extractActivityTypeFromMessage(String document) throws XMLStreamException {
        Reader reader = new StringReader(document);
        XMLStreamReader xml = XMLInputFactory.newFactory().createXMLStreamReader(reader);
        ActivityType type = null;
        while (xml.hasNext()) {
            int nextNodeType = xml.next();
            if (nextNodeType == XMLStreamConstants.START_ELEMENT) {
                if (FLUXFAREPORT_MESSAGE_START_XSD_ELEMENT.equals(xml.getLocalName())) {
                    type = ActivityType.FA_REPORT;
                    break;
                } else if (FLUXFAQUERY_MESSAGE_START_XSD_ELEMENT.equals(xml.getLocalName())) {
                    type = ActivityType.FA_QUERY;
                    break;
                } else if (FLUXRESPONSE_MESSAGE_START_XSD_ELEMENT.equals(xml.getLocalName())) {
                    type = ActivityType.FLUX_RESPONSE;
                }
            }
        }
        xml.close();
        return type;
    }

    /**
     * Create object with all necessary properties required to communicate with exchange
     *
     * @param textMessage
     * @return
     * @throws JMSException
     */
    public ExchangeMessageProperties createExchangeMessagePropertiesForFluxFAReportRequest(TextMessage textMessage, ActivityType type) throws JMSException {
        ExchangeMessageProperties exchangeMessageProperties = new ExchangeMessageProperties();
        exchangeMessageProperties.setUsername(exchangeUsername);
        exchangeMessageProperties.setDate(new Date());
        exchangeMessageProperties.setPluginType(PluginType.FLUX);
        exchangeMessageProperties.setDFValue(extractStringPropertyFromJMSTextMessage(textMessage, DF));
        exchangeMessageProperties.setSenderReceiver(extractStringPropertyFromJMSTextMessage(textMessage, FR));
        exchangeMessageProperties.setOnValue(extractStringPropertyFromJMSTextMessage(textMessage, ON));
        exchangeMessageProperties.setMessageGuid(extractMessageGuidFromInputXML(textMessage.getText(), type));
        log.info("Properties read from the message:" + exchangeMessageProperties);
        return exchangeMessageProperties;
    }


    //Extract UUID value from FLUXReportDocument as messageGuid
    private String extractMessageGuidFromInputXML(String message, ActivityType type) {
        String messageGuid = null;
        SaxParserUUIDExtractor saxParserForFaFLUXMessge = new SaxParserUUIDExtractor(type);
        try {
            saxParserForFaFLUXMessge.parseDocument(message);
        } catch (SAXException e) {
            // below message would be thrown once value is found.
            if (e instanceof UUIDSAXException)
                log.debug("************************************************");
            messageGuid = saxParserForFaFLUXMessge.getUuidValue();
            log.debug("UUID found:" + messageGuid);
            log.debug("************************************************");
        }
        return messageGuid;
    }

    private String extractStringPropertyFromJMSTextMessage(TextMessage textMessage, String property) {
        String value = null;
        try {
            value = textMessage.getStringProperty(property);
        } catch (JMSException e) {
            log.error("Couldn't find the property [ " + property + " ] in JMS Text Message:" + property, e);
        }
        return value;
    }
}
