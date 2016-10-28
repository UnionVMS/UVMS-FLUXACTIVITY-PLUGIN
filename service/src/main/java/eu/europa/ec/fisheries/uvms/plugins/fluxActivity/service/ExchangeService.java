/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service;

import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ModuleQueue;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.PluginMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;

/**
 *
 * @author jojoha
 */
@LocalBean
@Stateless
public class ExchangeService {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeService.class);

    @EJB
    StartupBean startupBean;

    @EJB
    PluginMessageProducer producer;

    public void sendFLUXFAReportMessageReportToExchange(String fluxFAReportMessage) {
       try {
           LOG.info("inside ExchangeService. sendFLUXFAReportMessageReportToExchange");
            String text = ExchangeModuleRequestMapper.createFluxFAReportRequest(fluxFAReportMessage,"flux");
           LOG.info("exchange request created :"+text);
            String messageId = producer.sendModuleMessage(text, ModuleQueue.EXCHANGE);
           LOG.info("send to exchange module :"+messageId);
        } catch (ExchangeModelMarshallException e) {
            LOG.error("Couldn't map movement to setreportmovementtype",e);
        } catch (JMSException e1) {
            LOG.error("couldn't send movement",e1);
        }
    }
}
