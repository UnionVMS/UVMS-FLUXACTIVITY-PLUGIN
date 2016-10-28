/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import eu.europa.ec.fisheries.schema.exchange.movement.v1.SetReportMovementType;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author jojoha
 */
public abstract class PluginDataHolder {

    public final static String PLUGIN_PROPERTIES_KEY = "fluxActivity.properties";
    public final static String PROPERTIES_KEY        = "settings.properties";
    public final static String CAPABILITIES_KEY      = "capabilities.properties";

    private Properties fluxActivityApplicaitonProperties;
    private Properties fluxActivityProperties;
    private Properties fluxActivityCapabilities;

    private final ConcurrentMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SetReportMovementType> cachedMovement = new ConcurrentHashMap<>();

    public ConcurrentMap<String, String> getSettings() {
        return settings;
    }
    public ConcurrentMap<String, String> getCapabilities() {
        return capabilities;
    }
    public ConcurrentMap<String, SetReportMovementType> getCachedMovement() {
        return cachedMovement;
    }
    public Properties getPluginApplicaitonProperties() {
        return fluxActivityApplicaitonProperties;
    }
    public void setPluginApplicaitonProperties(Properties fluxActivityApplicaitonProperties) {
        this.fluxActivityApplicaitonProperties = fluxActivityApplicaitonProperties;
    }
    public Properties getPluginProperties() {
        return fluxActivityProperties;
    }
    public void setPluginProperties(Properties fluxActivityProperties) {
        this.fluxActivityProperties = fluxActivityProperties;
    }
    public Properties getPluginCapabilities() {
        return fluxActivityCapabilities;
    }
    public void setPluginCapabilities(Properties fluxActivityCapabilities) {
        this.fluxActivityCapabilities = fluxActivityCapabilities;
    }

}
