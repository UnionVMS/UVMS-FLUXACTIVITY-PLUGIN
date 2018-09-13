package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import eu.europa.ec.fisheries.schema.exchange.plugin.types.v1.PluginType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.CapabilityListType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.ServiceType;
import eu.europa.ec.fisheries.schema.exchange.service.v1.SettingListType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangeModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.mapper.ServiceMapper;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.producer.PluginToEventBusTopicProducer;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service.FileHandlerBean;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import java.util.Map;
import java.util.Properties;

import static eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityPluginConstatns.DEFAULT_MESSAGE_DELAY;

@Singleton
@Startup
@DependsOn({"PluginToEventBusTopicProducer", "FileHandlerBean"})
@Slf4j
public class StartupBean extends PluginDataHolder {

    private static final int MAX_NUMBER_OF_TRIES = 10;
    private boolean isRegistered = false;
    private boolean isEnabled = false;
    private boolean waitingForResponse = false;
    private int numberOfTriesExecuted = 0;
    private String registeredClassName = "FluxActivityPlugin";
    @Getter
    private int messageDelay = DEFAULT_MESSAGE_DELAY;
    @Getter
    private boolean messageDelayEnabled = true;

    @EJB
    private PluginToEventBusTopicProducer messageProducer;

    @EJB
    private FileHandlerBean fileHandler;

    private CapabilityListType capabilities;
    private SettingListType settingList;
    private ServiceType serviceType;
    private FluxParameters fluxParameters;

    @PostConstruct
    public void startup() {

        //This must be loaded first!!! Not doing that will end in dire problems later on!
        super.setPluginApplicaitonProperties(fileHandler.getPropertiesFromFile(PluginDataHolder.PLUGIN_PROPERTIES_KEY));
        registeredClassName = getPLuginApplicationProperty("application.groupid");
        messageDelay = Integer.parseInt(getPLuginApplicationProperty("message.delay"));
        messageDelayEnabled = Boolean.parseBoolean(getPLuginApplicationProperty("message.delay.enabled"));

        //These can be loaded in any order
        super.setPluginProperties(fileHandler.getPropertiesFromFile(PluginDataHolder.PROPERTIES_KEY));
        super.setPluginCapabilities(fileHandler.getPropertiesFromFile(PluginDataHolder.CAPABILITIES_KEY));

        ServiceMapper.mapToMapFromProperties(super.getSettings(), super.getPluginProperties(), getRegisterClassName());
        ServiceMapper.mapToMapFromProperties(super.getCapabilities(), super.getPluginCapabilities(), null);

        capabilities = ServiceMapper.getCapabilitiesListTypeFromMap(super.getCapabilities());
        settingList = ServiceMapper.getSettingsListTypeFromMap(super.getSettings());
        serviceType = ServiceMapper.getServiceType(getRegisterClassName(),
                getApplicaionName(), "Flux Plugin for accepting Fishing Activities related XML messages.", PluginType.SATELLITE_RECEIVER, getPluginResponseSubscriptionName());
        register();

        log.debug("Settings updated in plugin {}", registeredClassName);
        for (Map.Entry<String, String> entry : super.getSettings().entrySet()) {
            log.debug("Setting: KEY: {} , VALUE: {}", entry.getKey(), entry.getValue());
        }
        populateFluxParameters();
        log.info("PLUGIN STARTED");
    }

    /**
     * Populates the flux connection parameters getting them from the properties file.
     */
    private void populateFluxParameters() {
        fluxParameters = new FluxParameters();
        Properties plugProps = super.getPluginApplicaitonProperties();
        fluxParameters.populate(
                (String)plugProps.get("provider.url"),
                (String)plugProps.get("security.principal.id"),
                (String)plugProps.get("security.principal.pwd"));
    }

    @PreDestroy
    public void shutdown() {
        unregister();
    }

    @Schedule(second = "*/30", minute = "*", hour = "*", persistent = false)
    public void timeout() {
        if (!waitingForResponse && !isRegistered && numberOfTriesExecuted < MAX_NUMBER_OF_TRIES) {
            log.info(getRegisterClassName() + " is not registered, trying to register");
            register();
            numberOfTriesExecuted++;
        }
    }

    private void register() {
        log.info("Registering to Exchange Module");
        setWaitingForResponse(true);
        try {
            String registerServiceRequest = ExchangeModuleRequestMapper.createRegisterServiceRequest(serviceType, capabilities, settingList);
            messageProducer.sendEventBusMessage(registerServiceRequest, ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE);
        } catch (MessageException | ExchangeModelMarshallException e) {
            log.error("Failed to send registration message to {}", ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE,e);
            setWaitingForResponse(false);
        }
    }

    private void unregister() {
        log.info("Unregistering from Exchange Module");
        try {
            String unregisterServiceRequest = ExchangeModuleRequestMapper.createUnregisterServiceRequest(serviceType);
            messageProducer.sendEventBusMessage(unregisterServiceRequest, ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE);
        } catch (MessageException | ExchangeModelMarshallException e) {
            log.error("Failed to send unregistration message to {}", ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE,e);
        }
    }

    private String getPLuginApplicationProperty(String key) {
        try {
            return (String) super.getPluginApplicaitonProperties().get(key);
        } catch (Exception e) {
            log.error("Failed to getSetting for key: " + key, getRegisterClassName(),e);
            return null;
        }
    }

    private String getSetting(String key) {
        try {
            log.debug("Trying to get setting {} ", registeredClassName + "." + key);
            return super.getSettings().get(registeredClassName + "." + key);
        } catch (Exception e) {
            log.error("Failed to getSetting for key: " + key, registeredClassName,e);
            return null;
        }
    }

    public String getPluginResponseSubscriptionName() {
        return getRegisterClassName() + getSetting("application.responseTopicName");
    }
    public String getResponseTopicMessageName() {
        return getSetting("application.groupid");
    }
    public String getRegisterClassName() {
        return registeredClassName;
    }
    private String getApplicaionName() {
        return getPLuginApplicationProperty("application.name");
    }
    public boolean isWaitingForResponse() {
        return waitingForResponse;
    }
    public void setWaitingForResponse(boolean waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }
    public boolean isIsRegistered() {
        return isRegistered;
    }
    public void setIsRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
    public boolean isIsEnabled() {
        return isEnabled;
    }
    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    public FluxParameters getFluxParameters() {
        return fluxParameters;
    }

}
