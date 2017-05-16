/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants;

public class FluxConnectionConstants {

    private FluxConnectionConstants(){}

    public static final String DESTINATION_TYPE_QUEUE                = "javax.jms.Queue";
    public static final String FLUX_CONNECTION_FACTORY               = "java:/FluxFactory";

	// ConnectionFactory details
    public static final String REMOTE_CONNECTION_FACTORY = "java:/jms/RemoteConnectionFactory";
    public static final String INITIAL_CONTEXT_FACTORY   = "org.jboss.naming.remote.client.InitialContextFactory";

    // Queue details
    public static final String JMS_QUEUE_BRIDGE = "java:/jms/queue/bridge";

    // Message details
    public static final String CONNECTOR_ID     = "CONNECTOR_ID";
    public static final String CONNECTOR_ID_VAL = "JMS MDM Business AP1";

    public static final String FLUX_ENV_AD      = "AD";
    public static final String FLUX_ENV_AD_VAL  = "XEU";
    public static final String FLUX_ENV_DF      = "DF";
    public static final String FLUX_ENV_TODT    = "TODT";

    public static final String FLUX_ENV_DF_VAL  = "urn:un:unece:uncefact:fisheries:FLUX:FA:EU:2";

    // Business procedure signature

    public static final String BUSINESS_UUID             = "BUSINESS_UUID";
    public static final String FLUX_ENV_AR = "AR";
    public static final String FLUX_ENV_AR_VAL = "true";

   /// public static  final String FLUX_MESSAGE_IN_REMOTE_QUEUE_NAME = "FAQuery";
    //public static  final String FLUX_MESSAGE_IN_REMOTE_QUEUE = "java:/jms/queue/FAQuery"; // This Queue has been configured on FLUX box. We will receive FAReportMessage in this queue.


     public static  final String FLUX_MESSAGE_IN_REMOTE_QUEUE_NAME = "FAPlugin";
     public static  final String FLUX_MESSAGE_IN_REMOTE_QUEUE = "java:/jms/queue/FAPlugin";
}