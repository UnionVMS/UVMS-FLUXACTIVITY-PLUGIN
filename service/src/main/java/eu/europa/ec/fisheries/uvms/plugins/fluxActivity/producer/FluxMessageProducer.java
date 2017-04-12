/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer;


import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.FluxParameters;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.StartupBean;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.UUID;

import static eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.FluxConnectionConstants.*;


@Stateless
@LocalBean
public class FluxMessageProducer {

    @EJB
    private StartupBean startUpBean;

    private HornetQConnectionFactory connectionFactory = null;
    private Connection connection = null;
    private Queue bridgeQueue = null;
    Session session = null;

    private static final Logger LOG = LoggerFactory.getLogger(PluginMessageProducer.class);

    /**
     * Sends a message from this Module to the flux Bridge queue.
     *
     * @param textMessage
     */
    public void sendMessageToFluxBridge(String textMessage) {
        try {
            openRemoteConnection();
            LOG.debug("Got connection to the FLUX queue ");
            TextMessage fluxMsgToSend = prepareMessage(textMessage, session);
            LOG.debug(" Trying to send message to the queue.. ");
            getProducer(bridgeQueue).send(fluxMsgToSend);
            LOG.debug(">>> Message sent correctly to FLUX node. ID : [[ " + fluxMsgToSend.getJMSMessageID() + " ]]");
        } catch (Exception ex) {
            LOG.error("Error while trying to send message to FLUX node.", ex);
        } finally {
            closeConnection();
        }
    }

    /**
     * Creates a new JMS Session and returns it;
     *
     * @return Session
     * @throws JMSException
     */
    private void openRemoteConnection() throws JMSException {
        try {
            loadRemoteQueueProperties();
        } catch (NamingException ex) {
            LOG.error("Error when open connection to JMS broker", ex);
            throw new JMSException(ex.getMessage());
        }
        LOG.debug("Opening connection to JMS broker");
        try {
            final FluxParameters fluxParameters = startUpBean.getFluxParameters();
            connection = connectionFactory.createConnection(fluxParameters.getProviderId(), fluxParameters.getProviderPwd());
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            LOG.error("Error when open connection to JMS broker. Going to << RETRY >> now.", ex);
            retryConnecting();
        }
    }

    /**
     * Retry connecting to FLUX TL.
     *
     * @throws JMSException
     */
    private void retryConnecting() throws JMSException {
        LOG.debug("ReTrying to open connection to Flux TL.");
        try {
            final FluxParameters fluxParameters = startUpBean.getFluxParameters();
            connection = connectionFactory.createConnection(fluxParameters.getProviderId(), fluxParameters.getProviderPwd());
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException ex) {
            LOG.error("Error when retrying to open connection to Flux TL. Going to << FAIL >> now.", ex);
            throw ex;
        }
    }

    /**
     * Creates a MessageProducer for the given destination;
     *
     * @param destination
     * @return MessageProducer
     * @throws JMSException
     */
    private MessageProducer getProducer(Destination destination) throws JMSException {
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        producer.setTimeToLive(60000L);
        return producer;
    }

    /**
     * Prepare the message for sending and set minimal set of attributes, required by FLUX TL JMS
     * to understand how it should process it;
     *
     * @param textMessage
     * @return fluxMsg
     * @throws JMSException
     * @throws DatatypeConfigurationException
     */
    private TextMessage prepareMessage(String textMessage, Session session) throws JMSException {
        TextMessage fluxMsg = session.createTextMessage();
        fluxMsg.setText(textMessage);
        fluxMsg.setStringProperty(CONNECTOR_ID, CONNECTOR_ID_VAL);
        fluxMsg.setStringProperty(FLUX_ENV_AD, FLUX_ENV_AD_VAL);
        //fluxMsg.setStringProperty(FLUX_ENV_TO, FLUX_ENV_TO_VAL);
        fluxMsg.setStringProperty(FLUX_ENV_DF, FLUX_ENV_DF_VAL);
        fluxMsg.setStringProperty(BUSINESS_UUID, createBusinessUUID());
        fluxMsg.setStringProperty(FLUX_ENV_TODT, createStringDate());
        fluxMsg.setStringProperty(FLUX_ENV_AR, FLUX_ENV_AR_VAL);
        printMessageProperties(fluxMsg);
        return fluxMsg;
    }

    /**
     * Creates the initial context (with the remote flux queue properties) and initializes the connectionFactory.
     *
     * @throws NamingException
     * @throws JMSException
     */
    private void loadRemoteQueueProperties() throws NamingException, JMSException {
        Properties contextProps = new Properties();
        final FluxParameters fluxParameters = startUpBean.getFluxParameters();
        LOG.debug("FLUX providerURL:"+fluxParameters.getProviderUrl());
        LOG.debug("FLUX SECURITY_PRINCIPAL:"+fluxParameters.getProviderId());
        LOG.debug("FLUX SECURITY_CREDENTIALS:"+fluxParameters.getProviderPwd());
        LOG.debug("JMS Queue:"+JMS_QUEUE_BRIDGE);
        contextProps.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        contextProps.put(Context.PROVIDER_URL, fluxParameters.getProviderUrl());
        contextProps.put(Context.SECURITY_PRINCIPAL, fluxParameters.getProviderId());
        contextProps.put(Context.SECURITY_CREDENTIALS, fluxParameters.getProviderPwd());
        Context context = new InitialContext(contextProps);
        connectionFactory = (HornetQConnectionFactory) context.lookup(REMOTE_CONNECTION_FACTORY);
        bridgeQueue = (Queue) context.lookup(JMS_QUEUE_BRIDGE);
    }

    private String createStringDate() {
        GregorianCalendar gcal = (GregorianCalendar) GregorianCalendar.getInstance();
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

    /**
     * BUSINESS_UUID has a prefix, a date-time combination and a serial - thus it is semi unique
     *
     * @return randomUUID
     */
    private String createBusinessUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Closes a JMS connection;
     * Disconnects from the actual connection if it is still active;
     */
    protected void closeConnection() {
        try {
            if (session != null) {
                System.out.println("\n\nClosing session.");
                session.close();
            }
            if (connection != null) {
                System.out.println("Succesfully closed the connection and/or session.");
                connection.stop();
                connection.close();
            }
            LOG.info("Succesfully disconnected from FLUX BRIDGE Remote queue.");
        } catch (JMSException e) {
            LOG.error("[ Error when stopping or closing JMS queue ] {}", e);
        }
    }

    private void printMessageProperties(TextMessage fluxMsg) throws JMSException {
        LOG.info("Prepared message with the following properties  : \n\n");
        int i = 0;
        Enumeration propertyNames = fluxMsg.getPropertyNames();
        String propName;
        while (propertyNames.hasMoreElements()) {
            i++;
            propName = (String) propertyNames.nextElement();
            System.out.println(i + ". " + propName + " : " + fluxMsg.getStringProperty(propName));
        }
    }

}
