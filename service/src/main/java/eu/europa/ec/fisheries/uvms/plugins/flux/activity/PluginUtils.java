package eu.europa.ec.fisheries.uvms.plugins.flux.activity;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PluginUtils {
    private static Properties properties;


    static public  String getFluxEndpoint(){
        loadProperties();
        String fluxEndpoint = (String) properties.get("FLUX_ENDPOINT");
        return fluxEndpoint;
    }



    static public  String getFluxDataflow(){
        loadProperties();
        String dataflow = (String) properties.get("FLUX_DATAFLOW");
        return dataflow;
    }

    static public String getConnectorId(){
        loadProperties();
        String connectorId  = (String) properties.get("CLIENT_ID");
        return connectorId;
    }

    static private void loadProperties(){
        InputStream is = null;
        if (properties==null){
            try {
                properties = new Properties();
                is = PluginUtils.class.getResourceAsStream("/settings.properties");
                properties.load(is);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
