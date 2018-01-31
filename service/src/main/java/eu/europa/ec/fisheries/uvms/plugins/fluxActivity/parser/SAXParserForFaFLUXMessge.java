/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * This class Will use SAX Parser to parse input XML document and extracts UUID value of FLUXReportDocument
 * Created by sanera on 16/05/2017.
 */
public class SAXParserForFaFLUXMessge extends DefaultHandler {
    final static Logger LOG = LoggerFactory.getLogger(SAXParserForFaFLUXMessge.class);

    private static final String FLUX_REPORT_DOCUMENT_TAG ="rsm:FLUXReportDocument";
    private static final String ID_TAG ="ram:ID";
    private static final String UUID_ATTRIBUTE ="UUID";

    private String uuid;
    private boolean isFLUXReportDocumentStart;
    private boolean isIDStart;
    private boolean isUUIDForFluxReportDocument;
    private String uuidValue; // store FLUXReportDocument UUID value inside this


    /**
     * This method parse input document using SAX parser
     * @param message
     * @throws SAXException
     */
    public void parseDocument(String message) throws SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
             StringReader sr = new StringReader(message);
            InputSource source = new InputSource(sr);
            parser.parse(source,this);

        } catch (ParserConfigurationException e) {
            LOG.error("Parse exception while trying to parse incoming message from flux.",e);

        } catch (IOException e) {
            LOG.error("IOException while trying to parse incoming message from flux.",e);
        }

    }


    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {

        // We need to extract UUID value for FLUXReportDocument. So, Mark when the tag is found.
        if(FLUX_REPORT_DOCUMENT_TAG.equals(elementName)){
            isFLUXReportDocumentStart =true;
            LOG.debug("FLUXReportDocument tag found.");
        }
        if(ID_TAG.equals(elementName) && isFLUXReportDocumentStart){
            isIDStart =true;
            LOG.debug("Found ID tag inside FLUXReportDocument tag");
            String value =attributes.getValue("schemeID");
            if(UUID_ATTRIBUTE.equals(value)){
                LOG.debug("Found UUID schemeID inside ID tag");
                isUUIDForFluxReportDocument =true;
            }
        }

    }

    @Override

    public void endElement(String s, String s1, String element) throws SAXException {
        if(FLUX_REPORT_DOCUMENT_TAG.equals(element)){
            isFLUXReportDocumentStart =false;
            LOG.debug("FLUXReportDocument tag Ended.");
        }
        if(ID_TAG.equals(element)){
            isIDStart =false;
            isUUIDForFluxReportDocument =false;
            LOG.debug("ID tag Ended.");
        }
    }

    @Override
    public void characters(char[] ac, int i, int j) throws SAXException {
        String  tmpValue = new String(ac, i, j);
        // Extract UUID value and stop parsing of further document?
        if(isUUIDForFluxReportDocument){
            uuidValue = tmpValue;
            throw new UUIDSAXException("Found the required value . so, stop parsing entire document");
        }
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public boolean isFLUXReportDocumentStart() {
        return isFLUXReportDocumentStart;
    }
    public void setFLUXReportDocumentStart(boolean FLUXReportDocumentStart) {
        isFLUXReportDocumentStart = FLUXReportDocumentStart;
    }
    public boolean isIDStart() {
        return isIDStart;
    }
    public void setIDStart(boolean IDStart) {
        isIDStart = IDStart;
    }
    public boolean isUUIDForFluxReportDocument() {
        return isUUIDForFluxReportDocument;
    }
    public void setUUIDForFluxReportDocument(boolean UUIDForFluxReportDocument) {
        isUUIDForFluxReportDocument = UUIDForFluxReportDocument;
    }
    public String getUuidValue() {
        return uuidValue;
    }
    public void setUuidValue(String uuidValue) {
        this.uuidValue = uuidValue;
    }
}
