/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.flux.activity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorService;

import javax.ejb.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.util.Collections;

/**
 * This class is intended to initiate the PortType for the intended WS-calls
 *
 */
@Singleton
@Startup
@Slf4j
public class PortInitiator {

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String CONNECTOR_ID = "connectorID";

    @EJB
    private StartupBean startupBean;

    private BridgeConnectorPortType port;

    private boolean waitingForUrlConfigProperty = true;

    @Getter
    @Setter
    private boolean wsIsSetup;

    @Lock(LockType.WRITE)
    public void setupPort(String url) {
        log.info("Setting up port for poster towards FLUX with URL {}", url);
        waitingForUrlConfigProperty = true;
        BridgeConnectorService service = new BridgeConnectorService();
        BridgeConnectorPortType newPort = service.getBridgeConnectorSOAP11Port();
        BindingProvider bp = (BindingProvider) newPort;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        bp.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, Collections.singletonMap(CONNECTOR_ID, Collections.singletonList(startupBean.getSetting(CLIENT_ID))));
        waitingForUrlConfigProperty = false;
        port = newPort;
    }

    public BridgeConnectorPortType getPort() {
        return port;
    }

    public boolean isWaitingForUrlConfigProperty() {
        return waitingForUrlConfigProperty;
    }
}
