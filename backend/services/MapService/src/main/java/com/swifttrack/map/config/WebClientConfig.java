package com.swifttrack.map.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for external API calls
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    
    private final MapServiceProperties properties;
    
    /**
     * WebClient for Nominatim API
     */
    @Bean(name = "nominatimWebClient")
    public WebClient nominatimWebClient() {
        return createWebClient(
            properties.getNominatim().getEffectiveUrl(),
            properties.getNominatim().getTimeout(),
            properties.getNominatim().getUserAgent()
        );
    }
    
    /**
     * WebClient for OSRM API
     */
    @Bean(name = "osrmWebClient")
    public WebClient osrmWebClient() {
        return createWebClient(
            properties.getOsrm().getEffectiveUrl(),
            properties.getOsrm().getTimeout(),
            properties.getNominatim().getUserAgent()
        );
    }
    
    /**
     * WebClient for GraphHopper API
     */
    @Bean(name = "graphHopperWebClient")
    public WebClient graphHopperWebClient() {
        return createWebClient(
            properties.getGraphhopper().getBaseUrl(),
            properties.getGraphhopper().getTimeout(),
            properties.getNominatim().getUserAgent()
        );
    }
    
    /**
     * Create a configured WebClient instance
     */
    private WebClient createWebClient(String baseUrl, int timeoutMs, String userAgent) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
            .responseTimeout(Duration.ofMillis(timeoutMs))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
            );
        
        // Increase max in-memory size for large responses (e.g., polylines)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();
        
        return WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .defaultHeader("User-Agent", userAgent)
            .defaultHeader("Accept", "application/json")
            .build();
    }
}
