package eu.europa.ec.fisheries.uvms.plugins.fluxActivity;

import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.constants.ActivityType;
import eu.europa.ec.fisheries.uvms.plugins.fluxActivity.consumer.FluxTlMessageConsumer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;

import static junit.framework.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class FluxTlMessageConsumerTest {

    private String MOVEMNT_XML_PATH = "src/test/resources/testData/movementExample.xml";

    @Mock
    FluxTlMessageConsumer fluxTLConsumer;

    @Test
    public void testGetActivityTypeMethod() throws IOException, XMLStreamException {
        String fileAsString = getFileAsString(MOVEMNT_XML_PATH);
        ActivityType activityType = fluxTLConsumer.extractActivityTypeFromMessage(fileAsString);
        assertNull(activityType);
    }

    private String getFileAsString(String pathToFile) throws IOException {
        return IOUtils.toString(new FileInputStream(pathToFile));
    }
}
