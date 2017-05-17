package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants;

/**
 * Created by kovian on 28/10/2016.
 */
public class ActivityPluginConstatns {

    private ActivityPluginConstatns(){
        super();
    }

    public static final String FA_GROUP_ID_ARTIFACT_ID = "eu.europa.ec.fisheries.uvms.plugins.fluxActivity";
    public static final String FA_GROUP_ID_ARTIFACT_ID_AC = "eu.europa.ec.fisheries.uvms.plugins.fluxActivityPLUGIN_RESPONSE";

    public static final String CLIENT_ID_EV         = FA_GROUP_ID_ARTIFACT_ID;
    public static final String SUBSCRIPTION_NAME_EV = FA_GROUP_ID_ARTIFACT_ID;
    public static final String MESSAGE_SELECTOR_EV  = "ServiceName='"+ FA_GROUP_ID_ARTIFACT_ID +"'";

    public static final String CLIENT_ID_AC 		= FA_GROUP_ID_ARTIFACT_ID_AC;
    public static final String SUBSCRIPTION_NAME_AC = FA_GROUP_ID_ARTIFACT_ID_AC;
    public static final String MESSAGE_SELECTOR_AC  = "ServiceName='"+ FA_GROUP_ID_ARTIFACT_ID_AC +"'";

    public static final String DURABLE = "Durable";


    public static final String CONNECTION_FACTORY = "ConnectionFactory";
    public static final String CONNECTION_TYPE = "javax.jms.MessageListener";
    public static final String DESTINATION_TYPE_QUEUE = "javax.jms.Queue";
    public static final String RULES_MESSAGE_IN_QUEUE = "jms/queue/UVMSRulesEvent";
    public static final String RULES_MESSAGE_IN_QUEUE_NAME = "UVMSRulesEvent";
    public static final String RULES_RESPONSE_QUEUE = "jms/queue/UVMSRules";
    public static final String QUEUE_DATASOURCE_INTERNAL = "jms/queue/UVMSRulesModel";

    public static final  String MOVEMENT_MESSAGE_IN_QUEUE = "jms/queue/UVMSMovementEvent";
    public static final  String ASSET_MESSAGE_IN_QUEUE = "jms/queue/UVMSAssetEvent";
    public static final String MOBILE_TERMINAL_MESSAGE_IN_QUEUE = "jms/queue/UVMSMobileTerminalEvent";
    public static final String EXCHANGE_MESSAGE_IN_QUEUE = "jms/queue/UVMSExchangeEvent";
    public static final String USER_MESSAGE_IN_QUEUE = "jms/queue/UVMSUserEvent";
    public static final String AUDIT_MESSAGE_IN_QUEUE = "jms/queue/UVMSAuditEvent";

    public static final   String MDC_IDENTIFIER = "clientName";

    public static final  String MODULE_NAME = "rules";

    public static final  String ACTIVITY_MESSAGE_IN_QUEUE = "jms/queue/UVMSActivityEvent";
    public static final  String MDR_EVENT = "jms/queue/UVMSMdrEvent";

    public static final String PLUGIN_EVENTBUS = "jms/topic/EventBus";
    public static final String EVENTBUS_NAME = "EventBus";
}
