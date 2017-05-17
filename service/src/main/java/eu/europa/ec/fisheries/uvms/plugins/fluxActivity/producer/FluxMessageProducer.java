/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
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
public class FluxMessageProducer extends  AbstractRemoteProducer  {

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
