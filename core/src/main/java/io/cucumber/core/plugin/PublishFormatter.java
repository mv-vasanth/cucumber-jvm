package io.cucumber.core.plugin;

import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CurlOption;
import io.cucumber.plugin.ColorAware;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import static io.cucumber.core.options.Constants.PLUGIN_PUBLISH_URL_PROPERTY_NAME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public final class PublishFormatter implements ConcurrentEventListener, ColorAware {

    /**
     * Where to publishes messages by default
     */
    public static final String DEFAULT_CUCUMBER_MESSAGE_STORE_URL = "https://messages.cucumber.io/api/reports";

    private final UrlReporter urlReporter = new UrlReporter(System.err);
    private final MessageFormatter delegate;

    public PublishFormatter() throws IOException {
        this.delegate = new MessageFormatter(makeUrlOutputStream(null));
    }

    public PublishFormatter(String token) throws IOException {
        this.delegate = new MessageFormatter(makeUrlOutputStream(token));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        delegate.setEventPublisher(publisher);
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        urlReporter.setMonochrome(monochrome);
    }

    private OutputStream makeUrlOutputStream(String token) throws IOException {
        Map<String, String> properties = CucumberProperties.create();
        // TODO: Move to properties parsing
        String url = properties.getOrDefault(PLUGIN_PUBLISH_URL_PROPERTY_NAME, DEFAULT_CUCUMBER_MESSAGE_STORE_URL);
        List<Map.Entry<String, String>> headers = createAuthorizationHeader(token);
        // TODO: Nice constructor
        CurlOption curlOption = new CurlOption(URI.create(url), CurlOption.HttpMethod.PUT, headers);
        return new UrlOutputStream(curlOption, urlReporter);
    }

    private static List<Map.Entry<String, String>> createAuthorizationHeader(String token) {
        if (token == null) {
            return emptyList();
        }
        return singletonList(new SimpleEntry<>("Authorization", "Bearer " + token));
    }

}