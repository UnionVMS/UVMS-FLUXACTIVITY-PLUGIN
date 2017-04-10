package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants;

/**
 * Created by sanera on 27/05/2016.
 */
public class FluxPluginConstants {
    private FluxPluginConstants() {
    }

    public static  final String FLUX_MESSAGE_IN_REMOTE_QUEUE_NAME = "FAQuery";
    public static  final String FLUX_MESSAGE_IN_REMOTE_QUEUE = "java:/jms/queue/FAQuery"; // This Queue has been configured on FLUX box. We will receive FAReportMessage in this queue.
    public static final String DESTINATION_TYPE_QUEUE = "javax.jms.Queue";
    public static final String FLUX_CONNECTION_FACTORY = "java:/FluxFactory";

}
