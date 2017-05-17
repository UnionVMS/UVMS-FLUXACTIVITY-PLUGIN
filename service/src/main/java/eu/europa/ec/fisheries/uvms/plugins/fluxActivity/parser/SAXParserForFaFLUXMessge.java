/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.parser;

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
 * Created by sanera on 16/05/2017.
 */
public class SAXParserForFaFLUXMessge extends DefaultHandler {
    private String uuid;
    private boolean isFLUXReportDocumentStart;
    private boolean isIDStart;
    private boolean isUUIDForFluxReportDocument;
    private String uuidValue;


    public SAXParserForFaFLUXMessge(){

    }

    public void parseDocument(String message) throws SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        try {
            parser = factory.newSAXParser();
            //   parser.parse("testSaxFAReport.xml", this);

          //  parser.parse("s011a_REP010_TRA.xml", this);
            StringReader sr = new StringReader(message);
            InputSource source = new InputSource(sr);
            parser.parse(source,this);
            //    parser.parse("Activity_RQ_RS1_Test.xml", this);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }


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


    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {

        if("rsm:FLUXReportDocument".equals(elementName)){
            isFLUXReportDocumentStart =true;
        }

        if("ram:ID".equals(elementName) && isFLUXReportDocumentStart){
            isIDStart =true;
            String value =attributes.getValue("schemeID");
            if("UUID".equals(value)){
                isUUIDForFluxReportDocument =true;
            }
        }
        System.out.println("startElement :"+elementName);
    }

    @Override

    public void endElement(String s, String s1, String element) throws SAXException {
        System.out.println("endElement :"+element);

        if("rsm:FLUXReportDocument".equals(element)){
            isFLUXReportDocumentStart =false;
        }

        if("ram:ID".equals(element)){
            isIDStart =false;
            isUUIDForFluxReportDocument =false;
        }
    }

    @Override
    public void characters(char[] ac, int i, int j) throws SAXException {

        String  tmpValue = new String(ac, i, j);

        if(isUUIDForFluxReportDocument){
            uuidValue = tmpValue;
            System.out.println("UUID value found:"+tmpValue);
            throw new UUIDSAXException("Found the required value . so, stop parsing entire document");
        }


    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
