/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.ExchangeMessageProperties;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.PluginMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Date;

/**
 *
 * @author jojoha
 */
@LocalBean
@Stateless
public class ExchangeService {

    private static final String exchangeUsername ="flux";
    private static final String DF ="DF";
    private static final String FR ="FR";
    private static final Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

    @EJB
    PluginMessageProducer producer;
        public void sendFLUXFAReportMessageReportToExchange(String fluxFAReportMessage, ExchangeMessageProperties prop) {
       try {
           LOG.info("Prepare FLUXFAReportMessageRequest to send to exchange");
            String text = ExchangeModuleRequestMapper.createFluxFAReportRequest(fluxFAReportMessage,prop.getUsername()
                    ,prop.getDFValue(),prop.getDate(),prop.getMessageGuid(),prop.getPluginType(),prop.getSenderReceiver());
           LOG.info("Exchange request created :"+text);
            String messageId = producer.sendModuleMessage(text, ModuleQueue.EXCHANGE);
           LOG.info("Message sent to exchange module :"+messageId);
        } catch (ExchangeModelMarshallException e) {
            LOG.error("Couldn't create FluxFAReportRequest for Exchange",e);
        } catch (JMSException e1) {
            LOG.error("couldn't send FluxFAReportRequest to exchange",e1);
        }
    }


     public ExchangeMessageProperties createExchangeMessagePropertiesForFluxFAReportRequest(TextMessage textMessage){
        ExchangeMessageProperties exchangeMessageProperties = new ExchangeMessageProperties();
        exchangeMessageProperties.setUsername(exchangeUsername);
        exchangeMessageProperties.setDate(new Date());
        exchangeMessageProperties.setPluginType(PluginType.FLUX);
        exchangeMessageProperties.setDFValue(extractStringPropertyFromJMSTextMessage(textMessage,DF));
        exchangeMessageProperties.setSenderReceiver(extractStringPropertyFromJMSTextMessage(textMessage,FR));

        return exchangeMessageProperties;
    }

    private String extractStringPropertyFromJMSTextMessage(TextMessage textMessage,String property){
        String value=null;
        try {
            value = textMessage.getStringProperty(property);
        } catch (JMSException e) {
            LOG.error("couldn't find the property in JMS Text Message:"+property,e);
        }
        return value;
    }


}
