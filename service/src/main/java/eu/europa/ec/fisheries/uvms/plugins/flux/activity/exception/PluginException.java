package eu.europa.ec.fisheries.uvms.plugins.flux.activity.exception;

public class PluginException extends Exception {

    private static final long serialVersionUID = 1L;

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }
}
