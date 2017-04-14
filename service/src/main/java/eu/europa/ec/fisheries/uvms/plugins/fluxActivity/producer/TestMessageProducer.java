package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer;

import eu.europa.ec.fisheries.uvms.message.AbstractRemoteProducer;
import eu.europa.ec.fisheries.uvms.message.ConnectionProperties;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.FluxParameters;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.FluxConnectionConstants;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.TextMessage;

/**
 * Created by sanera on 13/04/2017.
 */
@Stateless
@LocalBean
@Slf4j
public class TestMessageProducer extends  AbstractRemoteProducer  {

    @EJB
    private StartupBean startUpBean;

    @Override
    public void sendModuleResponseMessage(TextMessage message, String text, String moduleName) {
       LOG.info("This method is not supported");
    }

    @Override
    public String getDestinationName() {
        return FluxConnectionConstants.JMS_QUEUE_BRIDGE;
    }


    @Override
    public ConnectionProperties getConnectionProperties() {
        ConnectionProperties connectionProperties = new ConnectionProperties();
        final FluxParameters fluxParameters = startUpBean.getFluxParameters();
        connectionProperties.setProviderURL(fluxParameters.getProviderUrl());
        connectionProperties.setUsername(fluxParameters.getProviderId());
        connectionProperties.setPassword(fluxParameters.getProviderPwd());
        LOG.debug("ConnectionProperties------>"+connectionProperties);
        return connectionProperties;
    }
}
