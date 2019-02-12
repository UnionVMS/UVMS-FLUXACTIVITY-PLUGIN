/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception.UUIDSAXException;
import eu.europa.ec.fisheries.uvms.plugins.flux.activity.parser.SaxParserUUIDExtractor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.SAXException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class SaxParserUUIDExtractorTest {

    private SaxParserUUIDExtractor faQueryUuidExtractor;
    private SaxParserUUIDExtractor faReportUuidExtractor;
    private SaxParserUUIDExtractor faResponseUuidExtractor;

    private String FAQUERYTESTXMLPATH = "src/test/resources/testData/fluxFaQueryMessage.xml";
    private String FAREPORTTESTXMLPATH = "src/test/resources/testData/fluxFaReportMessage.xml";
    private String FLUXRESPONSETESTXMLPATH = "src/test/resources/testData/fluxResponseMessage.xml";
    private String MOVEMNT_XML_PATH = "src/test/resources/testData/movementExample.xml";

    private String faQuerySample;
    private String faReportSample;
    private String fluxResponseSample;
    private String movementExample;

    PrintStream logger = System.out;

    @Before
    @SneakyThrows
    public void init(){
        faQueryUuidExtractor = new SaxParserUUIDExtractor(ActivityType.FA_QUERY);
        faReportUuidExtractor = new SaxParserUUIDExtractor(ActivityType.FA_REPORT);
        faResponseUuidExtractor = new SaxParserUUIDExtractor(ActivityType.FLUX_RESPONSE);

        faQuerySample = getFileAsString(FAQUERYTESTXMLPATH);
        faReportSample = getFileAsString(FAREPORTTESTXMLPATH);
        fluxResponseSample = getFileAsString(FLUXRESPONSETESTXMLPATH);
        movementExample = getFileAsString(MOVEMNT_XML_PATH);
    }

    @Test
    public void testWrongDocument(){
        String messageGuid = null;
        try {
            faReportUuidExtractor.parseDocument(movementExample);
        } catch (SAXException e) {
            // below message would be thrown once value is found.
            if (e instanceof UUIDSAXException){
                messageGuid = faQueryUuidExtractor.getUuidValue();
            }
        }
        logger.println("FaQuery GUID : " + messageGuid);
        assertNull(messageGuid);
    }

    @Test
    public void testFaQueryUUIDExtraction(){
        String messageGuid = null;
        try {
            faQueryUuidExtractor.parseDocument(faQuerySample);
        } catch (SAXException e) {
            // below message would be thrown once value is found.
            if (e instanceof UUIDSAXException){
                messageGuid = faQueryUuidExtractor.getUuidValue();
            }
        }
        logger.println("FaQuery GUID : " + messageGuid);
        assertNotNull(messageGuid);
    }

    @Test
    public void testFaReportUUIDExtraction(){
        String messageGuid = null;
        try {
            faReportUuidExtractor.parseDocument(faReportSample);
        } catch (SAXException e) {
            // below message would be thrown once value is found.
            if (e instanceof UUIDSAXException){
                messageGuid = faReportUuidExtractor.getUuidValue();
            }
        }
        logger.println("FaReport GUID : " + messageGuid);
        assertNotNull(messageGuid);
    }

    @Test
    public void testFluxResponseUUIDExtraction(){
        String messageGuid = null;
        try {
            faResponseUuidExtractor.parseDocument(fluxResponseSample);
        } catch (SAXException e) {
            // below message would be thrown once value is found.
            if (e instanceof UUIDSAXException){
                messageGuid = faResponseUuidExtractor.getUuidValue();
            }
        }
        logger.println("FluxResponse GUID : " + messageGuid);
        assertNotNull(messageGuid);
    }


    private String getFileAsString(String pathToFile) throws IOException {
        return IOUtils.toString(new FileInputStream(pathToFile));
    }

}
