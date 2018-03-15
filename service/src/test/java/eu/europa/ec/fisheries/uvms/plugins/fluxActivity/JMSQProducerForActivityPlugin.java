/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by sanera on 02/06/2016.
 */
public class JMSQProducerForActivityPlugin {

    // Message details
    public static final String CONNECTOR_ID = "CONNECTOR_ID";
    public static final String CONNECTOR_ID_VAL = "JMS MDM Business AP1";

    public static final String FLUX_ENV_AD = "AD";
    public static final String FLUX_ENV_AD_VAL = "XEU";

    public static final String FLUX_ENV_DF = "DF";
    public static final String FLUX_ENV_DF_VAL = "urn:un:unece:uncefact:fisheries:FLUX:MDM:EU:2";

    public static final String FLUX_ENV_TODT = "TODT";

    public static final String FLUX_ENV_AR = "AR";
    public static final String FLUX_ENV_AR_VAL = "True";

    // Business procedure signature
    public static final String BUSINESS_UUID = "BUSINESS_UUID";

    private static final String FILE_PATH = "/Users/emrinalgr/Desktop/s003c_REP003-responding-to-QUE003_ACC.xml";

    public static void main(String[] args) {

        Reader fileReader = null;
        try {
            System.out.println("start: ");
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
            props.setProperty(Context.PROVIDER_URL, "tcp://10.142.0.118:61616");
            Context ctx = new InitialContext(props);
            System.out.println("context created ");
            ConnectionFactory connectionFactory = (ConnectionFactory) ctx.lookup("ConnectionFactory");

            System.out.println("connection factory created ");
            Connection connection = connectionFactory.createConnection();
            connection.start();


            Destination destination = (Destination) ctx.lookup("dynamicQueues/UVMSFAPluginEvent");
            System.out.println("destination created ");

            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);


            MessageProducer producer = session.createProducer(destination);
            TextMessage message = prepareMessage(getFluxFAReportMessage(), session);
            Destination destinationReporting = (Destination) ctx.lookup("dynamicQueues/UVMSReporting");
            message.setJMSReplyTo(destinationReporting);

            // send message
            System.out.println("sending xml message ");
            producer.send(message);

            System.out.println("Sent: " + message.getText());


            connection.close();
        } catch (Exception e) {
            System.out.println("exception: ");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Prepare the message for sending and set minimal set of attributes, required by FLUX TL JMS;
     *
     * @param textMessage
     * @return fluxMsg
     * @throws JMSException
     */
    private static TextMessage prepareMessage(String textMessage, Session session) throws JMSException {
        TextMessage fluxMsg = session.createTextMessage();
        fluxMsg.setText(textMessage);
        fluxMsg.setStringProperty(CONNECTOR_ID, CONNECTOR_ID_VAL);
        fluxMsg.setStringProperty(FLUX_ENV_AD, FLUX_ENV_AD_VAL);
        fluxMsg.setStringProperty(FLUX_ENV_DF, FLUX_ENV_DF_VAL);
        fluxMsg.setStringProperty("ON", "abc@abc.com");
        fluxMsg.setStringProperty(BUSINESS_UUID, createBusinessUUID());
        fluxMsg.setStringProperty(FLUX_ENV_TODT, createStringDate());
        fluxMsg.setStringProperty(FLUX_ENV_AR, FLUX_ENV_AR_VAL);
        fluxMsg.setStringProperty("FR", "XEU");
        System.out.println(fluxMsg);
        System.out.println(fluxMsg);
        return fluxMsg;
    }

    private static String createBusinessUUID() {
        return UUID.randomUUID().toString();
    }

    private static String createStringDate() {
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
        gcal.setTime(new Date(System.currentTimeMillis() + 1000000));
        XMLGregorianCalendar xgcal;
        try {
            xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            return xgcal.toString();
        } catch (DatatypeConfigurationException | NullPointerException e) {
            return null;
        }
    }

    private static String getFluxFAReportMessage() throws IOException {
        File xmlFile = new File(FILE_PATH);


        Reader fileReader = null;
        String fluxFAReportMessage = null;
        try {
            fileReader = new FileReader(xmlFile);
            BufferedReader bufReader = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = bufReader.readLine();

            while (line != null) {
                sb.append(line).append("\n");
                line = bufReader.readLine();
            }
            fluxFAReportMessage = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileReader.close();
        }


        return fluxFAReportMessage;
    }

}
