/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.flux.activity;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author jojoha
 */
public abstract class PluginDataHolder {

    final static String PLUGIN_PROPERTIES_KEY = "fluxActivity.properties";
    final static String PROPERTIES_KEY        = "settings.properties";
    final static String CAPABILITIES_KEY      = "capabilities.properties";

    // Contains the properties from : fluxActivity.properties
    private Properties fluxActivityProperties;

    // Contains the properties from : settings.properties
    private Properties fluxActivitySettings;

    // Contains the properties from : capabilities.properties
    private Properties fluxActivityCapabilities;

    private final ConcurrentMap<String, String> settings = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> capabilities = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> properties = new ConcurrentHashMap<>();

    public ConcurrentMap<String, String> getSettings() {
        return settings;
    }
    ConcurrentMap<String, String> getCapabilities() {
        return capabilities;
    }
    Properties getPluginApplicaitonProperties() {
        return fluxActivityProperties;
    }
    void setPluginApplicaitonProperties(Properties fluxActivityApplicaitonProperties) {
        this.fluxActivityProperties = fluxActivityApplicaitonProperties;
    }
    Properties getPluginProperties() {
        return fluxActivitySettings;
    }
    void setPluginSettings(Properties fluxActivityProperties) {
        this.fluxActivitySettings = fluxActivityProperties;
    }
    Properties getPluginCapabilities() {
        return fluxActivityCapabilities;
    }
    void setPluginCapabilities(Properties fluxActivityCapabilities) {
        this.fluxActivityCapabilities = fluxActivityCapabilities;
    }

    public ConcurrentMap<String, String> getProperties() {
        return properties;
    }
}