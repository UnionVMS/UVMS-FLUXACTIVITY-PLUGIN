/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.plugins.fluxActivity.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jojoha
 */
@Startup
@Singleton
public class FileHandlerBean {

    final static Logger LOG = LoggerFactory.getLogger(FileHandlerBean.class);

    public Properties getPropertiesFromFile(String fileName) {
        Properties props = new Properties();
        try {
            InputStream inputStream = FileHandlerBean.class.getClassLoader().getResourceAsStream(fileName);
            props.load(inputStream);
        } catch (IOException e) {
            LOG.debug("Properties file failed to load");
        }
        return props;
    }

}
