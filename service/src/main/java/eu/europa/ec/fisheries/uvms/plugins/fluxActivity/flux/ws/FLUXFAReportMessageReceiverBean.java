/*
 * ﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
 * © European Union, 2015-2016.
 *
 * This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.flux.ws;


import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import eu.europa.ec.fisheries.schema.exchange.module.v1.*;
import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.FANamespaceMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.PluginToExchangeProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jboss.ws.api.annotation.WebContext;
import org.w3c.dom.Element;
import un.unece.uncefact.data.standard.fluxfaquerymessage._3.FLUXFAQueryMessage;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.fluxresponsemessage._6.FLUXResponseMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FAQuery;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXReportDocument;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXResponseDocument;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.IDType;
import xeu.bridge_connector.v1.RequestType;
import xeu.bridge_connector.v1.VerbosityType;

@Stateless
@WebService(serviceName = "FLUXFAReportMessageService", targetNamespace = "urn:xeu:bridge-connector:wsdl:v1", portName = "BridgeConnectorPortType", endpointInterface = "xeu.bridge_connector.wsdl.v1.BridgeConnectorPortType")
@WebContext(contextRoot = "/unionvms/activity-service")
@Slf4j
public class FLUXFAReportMessageReceiverBean extends AbstractFluxReceiver {

    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String FLUXFAQUERY_MESSAGE = "FLUXFAQueryMessage";
    private static final String FLUXFAREPORT_MESSAGE = "FLUXFAReportMessage";
    private static final String FLUXRESPONSE_MESSAGE = "FLUXResponseMessage";

    @EJB
    private StartupBean startupBean;

    @EJB
    private PluginToExchangeProducer exchange;

    @Override
    protected void sendToExchange(RequestType rt) throws JAXBException, MessageException, TransformerException {
        Element element = rt.getAny();
        String localName = element.getLocalName();
        Map<QName, String> otherAttributes = rt.getOtherAttributes();
        String faMessageXml = ripXMLMessageFromRequest(element);
        ExchangeBaseRequest exchangeBaseRequest;
        switch (localName) {
            case FLUXFAQUERY_MESSAGE:
                exchangeBaseRequest = new SetFAQueryMessageRequest();

                JAXBContext jc = JAXBContext.newInstance(FLUXFAQueryMessage.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                FLUXFAQueryMessage xmlMessage = (FLUXFAQueryMessage) unmarshaller.unmarshal(rt.getAny());
                FAQuery faQuery = xmlMessage.getFAQuery();
                String guid = null;
                if (faQuery != null){
                    IDType id = faQuery.getID();
                    guid = id.getValue();
                }
                exchangeBaseRequest.setMessageGuid(guid);
                exchangeBaseRequest.setMethod(ExchangeModuleMethod.SET_FA_QUERY_MESSAGE);
                ((SetFAQueryMessageRequest) exchangeBaseRequest).setRequest(cleanFLUXQueryMessage(faMessageXml));
                break;
            case FLUXFAREPORT_MESSAGE:

                JAXBContext jc2 = JAXBContext.newInstance(FLUXFAReportMessage.class);
                Unmarshaller unmarshaller2 = jc2.createUnmarshaller();
                FLUXFAReportMessage xmlMessage2 = (FLUXFAReportMessage) unmarshaller2.unmarshal(rt.getAny());
                FLUXReportDocument fluxReportDocument = xmlMessage2.getFLUXReportDocument();
                String guid2 = null;
                if (fluxReportDocument != null){
                    List<IDType> fluxReportDocumentIDS = fluxReportDocument.getIDS();
                    if (CollectionUtils.isNotEmpty(fluxReportDocumentIDS)){
                        guid2 = fluxReportDocumentIDS.get(0).getValue();
                    }
                }

                exchangeBaseRequest = new SetFLUXFAReportMessageRequest();
                exchangeBaseRequest.setMethod(ExchangeModuleMethod.SET_FLUX_FA_REPORT_MESSAGE);
                exchangeBaseRequest.setMessageGuid(guid2);
                ((SetFLUXFAReportMessageRequest) exchangeBaseRequest).setRequest(cleanFLUXReportMessage(faMessageXml));
                break;
            case FLUXRESPONSE_MESSAGE:
                exchangeBaseRequest = new SetFLUXFAResponseMessageRequest();
                exchangeBaseRequest.setMethod(ExchangeModuleMethod.RCV_FLUX_FA_RESPONSE_MESSAGE);

                JAXBContext jc3 = JAXBContext.newInstance(FLUXResponseDocument.class);
                Unmarshaller unmarshaller3 = jc3.createUnmarshaller();
                FLUXFAReportMessage xmlMessage3 = (FLUXFAReportMessage) unmarshaller3.unmarshal(rt.getAny());
                FLUXReportDocument fluxReportDocument2 = xmlMessage3.getFLUXReportDocument();
                String guid3 = null;
                if (fluxReportDocument2 != null){
                    List<IDType> fluxReportDocumentIDS = fluxReportDocument2.getIDS();
                    if (CollectionUtils.isNotEmpty(fluxReportDocumentIDS)){
                        guid3 = fluxReportDocumentIDS.get(0).getValue();
                    }
                }
                exchangeBaseRequest.setMessageGuid(guid3);
                ((SetFLUXFAResponseMessageRequest) exchangeBaseRequest).setRequest(cleanFLUXResponseMessage(faMessageXml));
                break;
            default:
                exchangeBaseRequest = new SetFLUXFAReportMessageRequest();
                exchangeBaseRequest.setMethod(ExchangeModuleMethod.UNKNOWN);
                log.warn("UNKNOWN type of message was received in Activity Plugin!");
                break;
        }
        populateCommonProperties(exchangeBaseRequest, rt, otherAttributes.get(new QName("USER")), otherAttributes.get(new QName("FR")));
        exchange.sendModuleMessage(JAXBUtils.marshallJaxBObjectToString(exchangeBaseRequest, ISO_8859_1, true), null);
    }

    private String ripXMLMessageFromRequest(Element element) throws TransformerException {
        StreamResult result = new StreamResult(new StringWriter());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), result);
        transformer.setOutputProperty(OutputKeys.ENCODING, ISO_8859_1);
        return result.getWriter().toString();
    }

    private void populateCommonProperties(ExchangeBaseRequest exchangeBaseRequest, RequestType rt, String user, String fr) {
        // TODO : Map those 2 unmapped properties
        String ad = rt.getAD();
        VerbosityType vb = rt.getVB();
        exchangeBaseRequest.setUsername(user);
        exchangeBaseRequest.setFluxDataFlow(rt.getDF());
        exchangeBaseRequest.setDf(rt.getDF());
        exchangeBaseRequest.setDate(new Date());
        exchangeBaseRequest.setPluginType(PluginType.FLUX);
        exchangeBaseRequest.setSenderOrReceiver(fr);
        exchangeBaseRequest.setOnValue(rt.getON());
        exchangeBaseRequest.setTo(rt.getTO() != null ? rt.getTO().toString() : null);
        exchangeBaseRequest.setTodt(rt.getTODT() != null ? rt.getTODT().toString() : null);
    }

    @Override
    protected StartupBean getStartupBean() {
        return startupBean;
    }

    private String cleanFLUXResponseMessage(String fluxFAResponse) {
        String cleanXMLMessage = null;
        if (fluxFAResponse == null) {
            log.error("fluxFAResponse received in clean method is null");
            return cleanXMLMessage;
        }
        try {
            FLUXResponseMessage fluxResponseMessage = JAXBUtils.unMarshallMessage(fluxFAResponse, FLUXResponseMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage, ISO_8859_1, true, new FANamespaceMapper());
        } catch (JAXBException e) {
            log.error("PluginException when trying to clean FLUXResponse", e);
        }
        return cleanXMLMessage;
    }

    private String cleanFLUXReportMessage(String faReportMessageWrapper) {
        String cleanXMLMessage = null;
        if (faReportMessageWrapper == null) {
            log.debug(String.format("Cleaned FLUXResponse :%s", cleanXMLMessage));
            return cleanXMLMessage;
        }
        try {
            FLUXFAReportMessage fluxResponseMessage = JAXBUtils.unMarshallMessage(faReportMessageWrapper, FLUXFAReportMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage, ISO_8859_1, true, new FANamespaceMapper());
        } catch (JAXBException e) {
            log.error("PluginException when trying to clean FLUXFAReportMessage", e);
        }
        return cleanXMLMessage;
    }

    private String cleanFLUXQueryMessage(String faQueryMessageWrapper) {
        String cleanXMLMessage = null;
        if (faQueryMessageWrapper == null) {
            log.error("faQueryMessageWrapper received in clean method is null");
            return cleanXMLMessage;
        }
        try {
            FLUXFAQueryMessage fluxResponseMessage = JAXBUtils.unMarshallMessage(faQueryMessageWrapper, FLUXFAQueryMessage.class);
            cleanXMLMessage = JAXBUtils.marshallJaxBObjectToString(fluxResponseMessage, ISO_8859_1, true, new FANamespaceMapper());
        } catch (JAXBException e) {
            log.error("PluginException when trying to clean FLUXFAQueryMessage", e);
        }
        return cleanXMLMessage;
    }


}