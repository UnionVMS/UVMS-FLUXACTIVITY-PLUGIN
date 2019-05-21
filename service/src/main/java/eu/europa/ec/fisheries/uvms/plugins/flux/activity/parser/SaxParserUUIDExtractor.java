/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */

package eu.europa.ec.fisheries.uvms.plugins.flux.activity.parser;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception.UUIDSAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class Will use SAX Parser to parse input XML document and extracts UUID value of FLUXReportDocument
 * Created by sanera on 16/05/2017.
 */
public class SaxParserUUIDExtractor extends DefaultHandler {

    final static Logger LOG = LoggerFactory.getLogger(SaxParserUUIDExtractor.class);

    private static final String FA_REPORT_DOCUMENT_UUID_CONTAINER_TAG = "FLUXReportDocument";
    private static final String FA_QUERY_UUID_CONTAINER_TAG = "FAQuery";
    private static final String FLUX_RESPONSE_UUID_CONTAINER_TAG = "FLUXResponseDocument";

    private static final String ID_TAG = "ID";
    private static final String ID_TAG_FOR_FLUX_RESPONSE = "ID";
    private static final String UUID_ATTRIBUTE = "UUID";

    private boolean isStartOfInterestedTag;
    private boolean isIDStart;
    private boolean isUUIDStart;
    private String uuidValue; // store FLUXReportDocument UUID value inside this

    // Three case here : FaReportMessage, FaQueryMessage, FLUXResponseMessage
    private String CONTAINER_TAG;

    public SaxParserUUIDExtractor(ActivityType type){
        switch (type){
            case FA_QUERY:
                CONTAINER_TAG = FA_QUERY_UUID_CONTAINER_TAG;
                break;
            case FA_REPORT:
                CONTAINER_TAG = FA_REPORT_DOCUMENT_UUID_CONTAINER_TAG;
                break;
            case FA_RESPONSE:
                CONTAINER_TAG = FLUX_RESPONSE_UUID_CONTAINER_TAG;
                break;
            case UNKNOWN:
                CONTAINER_TAG = FLUX_RESPONSE_UUID_CONTAINER_TAG;
                break;
        }
    }

    /**
     * This method parse input document using SAX parser
     *
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
            parser.parse(source, this);
        } catch (ParserConfigurationException e) {
            LOG.error("Parse exception while trying to parse incoming message from flux.", e);
        } catch (IOException e) {
            LOG.error("IOException while trying to parse incoming message from flux.", e);
        }
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) {
        // We need to extract UUID value for FLUXReportDocument. So, Mark when the tag is found.
        String cleanElementName =  cleanElementName(elementName);
        if (CONTAINER_TAG.equals(cleanElementName)) {
            isStartOfInterestedTag = true;
            LOG.debug("FLUXReportDocument tag found.");
        }
        if (isStartOfInterestedTag && (ID_TAG.equals(cleanElementName) || ID_TAG_FOR_FLUX_RESPONSE.equals(cleanElementName))) {
            isIDStart = true;
            LOG.debug("Found ID tag inside FLUXReportDocument tag");
            String value = attributes.getValue("schemeID");
            if (UUID_ATTRIBUTE.equals(value)) {
                LOG.debug("Found UUID schemeID inside ID tag");
                isUUIDStart = true;
            }
        }
    }

    @Override
    public void endElement(String s, String s1, String element) {
        String cleanElementName =  cleanElementName(element);
        if (CONTAINER_TAG.equals(cleanElementName)) {
            isStartOfInterestedTag = false;
            LOG.debug("FLUXReportDocument tag Ended.");
        }
        if (ID_TAG.equals(cleanElementName)) {
            isIDStart = false;
            isUUIDStart = false;
            LOG.debug("ID tag Ended.");
        }
    }

    @Override
    public void characters(char[] ac, int i, int j) throws SAXException {
        String tmpValue = new String(ac, i, j);
        // Extract UUID value and stop parsing of further document?
        if (isUUIDStart) {
            uuidValue = tmpValue;
            throw new UUIDSAXException("Found the required value . so, stop parsing entire document");
        }
    }

    private String cleanElementName(String elementName) {
        String cleanElementName = elementName;
        if(elementName != null && elementName.indexOf(":") > 0){
            cleanElementName = elementName.substring(elementName.indexOf(":") + 1);
        }
        return cleanElementName;
    }

    public String getUuidValue() {
        return uuidValue;
    }
}
