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
}
