/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer;

import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.FluxConnectionConstants;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.ExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Created by sanera on 14/08/2017.
 */
@MessageDriven(mappedName = FluxConnectionConstants.FLUX_MESSAGE_IN_QUEUE,  activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = FluxConnectionConstants.DESTINATION_TYPE_QUEUE),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = FluxConnectionConstants.FLUX_MESSAGE_IN_QUEUE_NAME),
        @ActivationConfigProperty(propertyName = "messagingType", propertyValue = FluxConnectionConstants.CONNECTION_TYPE)
})
public class FLUXMessageConsumer implements MessageListener {
    final static Logger LOG = LoggerFactory.getLogger(FLUXMessageConsumer.class);

    @EJB
    ExchangeService exchange;


    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void onMessage(Message inMessage) {

        LOG.info("------Received Message in ERS Activity plugin from FLUX--------");

        TextMessage textMessage = (TextMessage) inMessage;

        try {
            if(textMessage ==null || textMessage.getText() ==null)
                throw new Exception("Message received in ERS Plugin is null.");

            LOG.debug("Received FAReportMessage :");
            exchange.sendFLUXFAReportMessageReportToExchange(textMessage.getText(),exchange.createExchangeMessagePropertiesForFluxFAReportRequest(textMessage));
            LOG.info("message sent successfully to exchange module");


        } catch (Exception e) {
            LOG.error("Error while trying to send Flux FAReport message to exchange",e);
        }
    }
}
