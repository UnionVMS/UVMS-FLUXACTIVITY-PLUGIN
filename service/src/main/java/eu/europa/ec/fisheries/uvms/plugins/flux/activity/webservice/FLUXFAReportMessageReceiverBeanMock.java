/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.flux.activity.webservice;


import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.jms.producer.PluginToExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.jboss.ws.api.annotation.WebContext;
import org.w3c.dom.Element;
import xeu.connector_bridge.v1.PostMsgOutType;
import xeu.connector_bridge.v1.PostMsgType;
import xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType;

@Stateless
@WebService(serviceName = "FLUXFAReportMessageService", targetNamespace = "urn:xeu:connector-bridge:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.connector_bridge.wsdl.v1.BridgeConnectorPortType")
@WebContext(contextRoot = "/unionvms/activity-service")
@Slf4j
public class FLUXFAReportMessageReceiverBeanMock implements BridgeConnectorPortType {

    private static final String ISO_8859_1 = "ISO-8859-1";

    @EJB
    private StartupBean startupBean;

    @EJB
    private PluginToExchangeProducer exchange;

    protected void logMessage(PostMsgType rt) throws TransformerException {
        Element element = rt.getAny();
        String faMessageXml = ripXMLMessageFromRequest(element);
        log.warn("Message consumed by the MOCK TESTING ENDPOINT WARN WARN WARN::::::::CHANGE THE FLUX_ENDPOINT PARAMETER OF FLUXACTIVITY PLUGIN YOU LAZY BASTARD::::::");
        log.debug(faMessageXml);
    }

    private String ripXMLMessageFromRequest(Element element) throws TransformerException {
        StreamResult result = new StreamResult(new StringWriter());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), result);
        transformer.setOutputProperty(OutputKeys.ENCODING, ISO_8859_1);
        return result.getWriter().toString();
    }

    @Override
    public PostMsgOutType post(PostMsgType postMsgType) {
        PostMsgOutType type = new PostMsgOutType();
        try {
            log.debug("Got activity request from FLUX in FLUX plugin");
            logMessage(postMsgType);
            return type;
        } catch (Exception e) {
            log.error("[ Error when receiving data from FLUX. ]", e);
            return type;
        }
    }
}