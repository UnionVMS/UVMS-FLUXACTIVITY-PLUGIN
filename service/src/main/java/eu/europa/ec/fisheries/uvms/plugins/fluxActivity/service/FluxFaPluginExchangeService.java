/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.ExchangeMessageProperties;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.PluginToExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@LocalBean
@Stateless
@Slf4j
public class FluxFaPluginExchangeService {

    @EJB
    private PluginToExchangeProducer exchangeProducer;

    public void sendFishingActivityMessageToExchange(String receivedMessage, ExchangeMessageProperties prop, ActivityType activityType) {
        try {
            log.info("[START] Preparing request of type [ " + activityType + " ] to send to exchange...");
            String exchnageReqStr = null;
            switch (activityType) {
                case FA_REPORT:
                    exchnageReqStr = ExchangeModuleRequestMapper.createFluxFAReportRequest(receivedMessage, prop.getUsername()
                            , prop.getDFValue(), prop.getDate(), prop.getMessageGuid()
                            , prop.getPluginType(), prop.getSenderReceiver(), prop.getOnValue());
                    break;
                case FA_QUERY:
                    exchnageReqStr = ExchangeModuleRequestMapper.createFaQueryRequest(receivedMessage, prop.getUsername()
                            , prop.getDFValue(), prop.getDate(), prop.getMessageGuid()
                            , prop.getPluginType(), prop.getSenderReceiver(), prop.getOnValue());
                    break;
                case FLUX_RESPONSE:
                    exchnageReqStr = ExchangeModuleRequestMapper.createFluxResponseRequest(receivedMessage, prop.getUsername()
                            , prop.getDFValue(), prop.getDate(), prop.getMessageGuid()
                            , prop.getPluginType(), prop.getSenderReceiver(), prop.getOnValue());
                    break;
                default:
                    log.error("[ERROR] The following type is not mapped or implemented : {}\n Original Message : {}", activityType, receivedMessage);
            }
            if (StringUtils.isNotEmpty(exchnageReqStr)) {
                String messageId = exchangeProducer.sendModuleMessage(exchnageReqStr, null);
                log.info("[END] Request object created and Message sent to [EXCHANGE] module : " + messageId);
            } else {
                log.error("[ERROR] Apparently the incoming message couldn't be mapped to any of the supported types!");
            }
        } catch (ExchangeModelMarshallException e) {
            log.error("[ERROR] Couldn't create FluxFAReportRequest for Exchange!", e);
        } catch (MessageException e) {
            log.error("[ERROR] Couldn't send FluxFAReportRequest to Exchange!", e);
        }
    }

}
