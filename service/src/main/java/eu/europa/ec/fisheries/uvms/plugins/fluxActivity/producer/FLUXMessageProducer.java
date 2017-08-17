/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer;

import eu.europa.ec.fisheries.schema.exchange.plugin.v1.PluginBaseRequest;
import eu.europa.ec.fisheries.uvms.message.AbstractProducer;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.FluxConnectionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sanera on 14/08/2017.
 */
@Stateless
@LocalBean
public class FLUXMessageProducer extends AbstractProducer {

    private static final Logger LOG = LoggerFactory.getLogger(FLUXMessageProducer.class);

    public static final String FLUX_ENV_AD      = "AD";
    public static final String FLUX_ENV_DF      = "DF";
    public static final String BUSINESS_UUID    = "BUSINESS_UUID";
    public static final String FLUX_ENV_TODT    = "TODT";
    public static final String FLUX_ENV_AR = "AR";
    public static final String FLUX_ENV_FR = "FR";
    public static final String FLUX_ENV_TO = "TO";
    public static final String FLUX_ENV_CT = "CT";
    public static final String FLUX_ENV_VB = "VB";


    @Override
    public String getDestinationName() {
        return FluxConnectionConstants.FLUX_JMS_QUEUE_BRIDGE;
    }



    public  Map<String, String> getFLUXMessageProperties(PluginBaseRequest responseRequest) {

        Map<String, String> messageProperties = new HashMap<>();

        if(responseRequest !=null){
            messageProperties.put(FLUXMessageProducer.FLUX_ENV_AD,responseRequest.getDestination());
            messageProperties.put(FLUXMessageProducer.FLUX_ENV_FR,responseRequest.getSenderOrReceiver());
            messageProperties.put(FLUXMessageProducer.FLUX_ENV_DF,responseRequest.getFluxDataFlow());
        }else {
            LOG.error("PluginBaseRequest is null so, could not set AD/FR/DF values to the FLUXMEssage");
        }

        messageProperties.put(FLUXMessageProducer.FLUX_ENV_AR,"true");
        messageProperties.put(FLUXMessageProducer.BUSINESS_UUID,createBusinessUUID());
        messageProperties.put(FLUXMessageProducer.FLUX_ENV_TODT,createStringDate());
        messageProperties.put(FLUXMessageProducer.FLUX_ENV_TO,"60");
        messageProperties.put(FLUXMessageProducer.FLUX_ENV_CT,"admin@dgmare.com");
        messageProperties.put(FLUXMessageProducer.FLUX_ENV_VB,"ERROR");

        return messageProperties;
    }


    /**
     * BUSINESS_UUID has a prefix, a date-time combination and a serial - thus it is semi unique
     *
     * @return randomUUID
     */
    protected String createBusinessUUID() {
        return UUID.randomUUID().toString();
    }

    protected String createStringDate() {
        GregorianCalendar gcal = (GregorianCalendar) Calendar.getInstance();
        gcal.setTime(new Date(System.currentTimeMillis() + 1000000));
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            return xgcal.toString();
        } catch (DatatypeConfigurationException | NullPointerException e) {
            LOG.error("Error occured while creating newXMLGregorianCalendar", e);
            return null;
        }
    }


}
