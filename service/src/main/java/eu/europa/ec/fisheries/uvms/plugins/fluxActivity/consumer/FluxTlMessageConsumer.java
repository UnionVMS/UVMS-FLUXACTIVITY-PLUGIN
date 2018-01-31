/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.ExchangeService;
import java.io.Reader;
import java.io.StringReader;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import lombok.extern.slf4j.Slf4j;

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

    @EJB
    ExchangeService exchange;


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {
        log.info("[INFO] Received Message in UVMSFAPluginEvent Queue from FLUX.");
        TextMessage textMessage = (TextMessage) inMessage;
        try {
            if (textMessage == null || textMessage.getText() == null) {
                throw new IllegalArgumentException("Message received in ERS Plugin is null.");
            }
            log.debug("[START] Received FAReportMessage :");
            exchange.sendFishingActivityMessageToExchange(textMessage.getText(), exchange.createExchangeMessagePropertiesForFluxFAReportRequest(textMessage),
                    extractActivityTypeFromMessage(textMessage.getText()));
            log.info("[END] Message sent successfully to exchange module.");
        } catch (Exception e) {
            log.error("Error while trying to send Flux FAReport message to exchange", e);
        }
    }


    public ActivityType extractActivityTypeFromMessage(String document) throws XMLStreamException {
        String faRepLocalName = "FLUXFAReportMessage";
        String faQueLocalName = "FLUXFAQueryMessage";
        Reader reader = new StringReader(document);
        XMLStreamReader xml = XMLInputFactory.newFactory().createXMLStreamReader(reader);
        try {
            while (xml.hasNext()) {
                int nextNodeType = xml.next();
                if (nextNodeType == XMLStreamConstants.START_ELEMENT && faRepLocalName.equals(xml.getLocalName())) {
                    return ActivityType.FA_REPORT;
                } else if (nextNodeType == XMLStreamConstants.START_ELEMENT && faQueLocalName.equals(xml.getLocalName())) {
                    return ActivityType.FA_QUERY;
                }
            }
        } finally {
            xml.close();
        }
        return null;
    }
}
