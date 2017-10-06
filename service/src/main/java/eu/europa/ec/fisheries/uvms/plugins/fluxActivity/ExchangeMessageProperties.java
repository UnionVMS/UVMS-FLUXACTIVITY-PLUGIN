/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;

import java.util.Date;

/**
 * Created by sanera on 16/05/2017.
 */
public class ExchangeMessageProperties {

    private String username;
    private String reportType;
    private String DFValue;
    private String onValue;
    private Date date;
    private PluginType pluginType;
    private String senderReceiver;
    private String messageGuid;

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getReportType() {
        return reportType;
    }
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    public String getDFValue() {
        return DFValue;
    }
    public void setDFValue(String DFValue) {
        this.DFValue = DFValue;
    }
    public String getOnValue() {
        return onValue;
    }
    public void setOnValue(String onValue) {
        this.onValue = onValue;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public PluginType getPluginType() {
        return pluginType;
    }
    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }
    public String getSenderReceiver() {
        return senderReceiver;
    }
    public void setSenderReceiver(String senderReceiver) {
        this.senderReceiver = senderReceiver;
    }
    public String getMessageGuid() {
        return messageGuid;
    }
    public void setMessageGuid(String messageGuid) {
        this.messageGuid = messageGuid;
    }
}
