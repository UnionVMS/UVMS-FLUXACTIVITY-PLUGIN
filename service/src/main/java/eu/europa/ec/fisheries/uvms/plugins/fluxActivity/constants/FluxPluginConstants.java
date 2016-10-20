package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants;

/**
 * Created by sanera on 27/05/2016.
 */
public interface FluxPluginConstants {
     String FLUX_MESSAGE_IN_QUEUE = "java:/jms/queue/ERSPlugin";

     String FLUX_MESSAGE_IN_QUEUE_NEW_NAME = "mdrin";
     String FLUX_MESSAGE_IN_QUEUE_NEW = "jms/queue/mdrin";
     String CONNECTION_TYPE = "javax.jms.MessageListener";
     String DESTINATION_TYPE_QUEUE = "javax.jms.Queue";
     String QUEUE_FLUX_RECEIVER_NAME = "ERSPlugin";
     //String FLUX_MESSAGE_DESTINATION_QUEUE = "java:/jms/queue/ERSPlugin";
     public static final String CONNECTION_FACTORY = "jms/RemoteConnectionFactory";
     public static final String FLUX_CONNECTION_FACTORY = "java:/FluxFactory";

}
