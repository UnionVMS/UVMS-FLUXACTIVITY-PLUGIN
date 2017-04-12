/*
Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.

 */package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

/**
 * Created by kovian on 26/10/2016.
 */
public class FluxParameters {

    private String providerUrl;
    private String providerId;
    private String providerPwd;


    public void populate(String providerUrl, String providerId, String providerPwd){
        this.providerId = providerId;
        this.providerUrl = providerUrl;
        this.providerPwd = providerPwd;
    }

    public String getProviderUrl() {
        return providerUrl;
    }
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }
    public String getProviderId() {
        return providerId;
    }
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
    public String getProviderPwd() {
        return providerPwd;
    }
    public void setProviderPwd(String providerPwd) {
        this.providerPwd = providerPwd;
    }
}
