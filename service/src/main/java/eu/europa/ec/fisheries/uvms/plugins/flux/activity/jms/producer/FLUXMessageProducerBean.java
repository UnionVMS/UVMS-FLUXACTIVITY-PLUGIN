/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/

package eu.europa.ec.fisheries.uvms.plugins.flux.activity.jms.producer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAQueryRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAReportRequest;
import eu.europa.ec.fisheries.schema.exchange.plugin.v1.SetFLUXFAResponseRequest;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants.ActivityType;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.*;

@Stateless
@LocalBean
@Slf4j
public class FLUXMessageProducerBean extends AbstractProducer {

    private static final String FLUX_ENV_AD = "AD";
    private static final String FLUX_ENV_DF = "DF";
    private static final String BUSINESS_UUID = "BUSINESS_UUID";
    private static final String FLUX_ENV_FR = "FR";
    private static final String ON = "ON";

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_PLUGIN_BRIDGE;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void sendMessageToBridgeQueue(PluginBaseRequest request, ActivityType msgType) throws MessageException {
        log.info("[JMS] Sending message outside of FLUX-FMC through :::-->>> JMS");
        String xmlMessage;
        if (ActivityType.FA_RESPONSE.equals(msgType)){
            xmlMessage = ((SetFLUXFAResponseRequest) request).getResponse();
        } else if (ActivityType.FA_QUERY.equals(msgType)){
            xmlMessage = ((SetFLUXFAQueryRequest) request).getResponse();
        } else if (ActivityType.FA_REPORT.equals(msgType)){
            xmlMessage = ((SetFLUXFAReportRequest) request).getResponse();
        } else {
            throw new IllegalArgumentException("The message forwarded from Exchange cannot be handeled by th system");
        }
        sendModuleMessageWithProps(xmlMessage, getDestination(), getFLUXMessageProperties(request));
        log.info("[INFO] Outgoing message ({}) with ON :[{}] send to [{}] \n\n", msgType, request.getOnValue(), request.getDestination());
    }

    public Map<String, String> getFLUXMessageProperties(PluginBaseRequest pluginReq) {
        Map<String, String> messageProperties = new HashMap<>();
        if (pluginReq != null) {
            messageProperties.put(FLUXMessageProducerBean.FLUX_ENV_AD, pluginReq.getDestination());
            messageProperties.put(FLUXMessageProducerBean.FLUX_ENV_FR, pluginReq.getSenderOrReceiver());
            messageProperties.put(FLUXMessageProducerBean.FLUX_ENV_DF, pluginReq.getFluxDataFlow());
            messageProperties.put(FLUXMessageProducerBean.BUSINESS_UUID, pluginReq.getOnValue());
            messageProperties.put(FLUXMessageProducerBean.ON, createBusinessUUID());
        } else {
            log.error("PluginBaseRequest is null so, could not set AD/FR/DF values to the FLUXMEssage");
        }
        return messageProperties;
    }

    /**
     * BUSINESS_UUID has a prefix, a date-time combination and a serial - thus it is semi unique
     *
     * @return randomUUID
     */
    private String createBusinessUUID() {
        return UUID.randomUUID().toString();
    }
}
