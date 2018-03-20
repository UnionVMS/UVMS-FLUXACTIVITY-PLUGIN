/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import static org.junit.Assert.assertFalse;

import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import org.junit.Test;
import un.unece.uncefact.data.standard.fluxfareportmessage._3.FLUXFAReportMessage;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FLUXReportDocument;

public class JAXBUtilsTest {

    @Test
    public void testMarshallMessageFaReport() throws JAXBException {

        FLUXFAReportMessage fluxfaReportMessage = new FLUXFAReportMessage();
        fluxfaReportMessage.setFLUXReportDocument(new FLUXReportDocument());
        String jaxBObjectToString =
                JAXBUtils.marshallJaxBObjectToString(fluxfaReportMessage, "UTF-8", true);
        assertFalse(jaxBObjectToString.contains("standalone=\"yes\""));
        assertFalse(jaxBObjectToString.contains("encoding=\"UTF-8\""));
        assertFalse(jaxBObjectToString.contains("version=\"1.0\""));

    }

}
