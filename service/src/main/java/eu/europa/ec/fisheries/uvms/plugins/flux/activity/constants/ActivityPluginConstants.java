/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

*/

package eu.europa.ec.fisheries.uvms.plugins.flux.activity.constants;

public class ActivityPluginConstants {

    private ActivityPluginConstants(){
        super();
    }

    private static final String FA_GROUP_ID_ARTIFACT_ID = "eu.europa.ec.fisheries.uvms.plugins.flux.activity";
    private static final String FA_GROUP_ID_ARTIFACT_ID_AC = "eu.europa.ec.fisheries.uvms.plugins.flux.activityPLUGIN_RESPONSE";

    public static final String CLIENT_ID_EV         = FA_GROUP_ID_ARTIFACT_ID;
    public static final String SUBSCRIPTION_NAME_EV = FA_GROUP_ID_ARTIFACT_ID;
    public static final String MESSAGE_SELECTOR_EV  = "ServiceName='"+ FA_GROUP_ID_ARTIFACT_ID +"'";

    public static final String CLIENT_ID_AC 		= FA_GROUP_ID_ARTIFACT_ID_AC;
    public static final String SUBSCRIPTION_NAME_AC = FA_GROUP_ID_ARTIFACT_ID_AC;
    public static final String MESSAGE_SELECTOR_AC  = "ServiceName='"+ FA_GROUP_ID_ARTIFACT_ID_AC +"'";

    public static final int DEFAULT_MESSAGE_DELAY = 1000;
}