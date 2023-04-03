package uk.gov.companieshouse.company_appointments.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public class WiremockTestConfig {

    private static final int port = 8888;

    private static WireMockServer wireMockServer = null;

    public static void setupWiremock() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(port);
            wireMockServer.start();
            configureFor("localhost", wireMockServer.port());
        } else {
            wireMockServer.resetAll();
        }
    }

    public static void stubKafkaApi(Integer responseCode) {
        stubFor(
                post(urlPathMatching("/resource-changed"))
                        .willReturn(aResponse()
                                .withStatus(responseCode)
                                .withHeader("Content-Type", "application/json"))
        );
    }

    public static List<ServeEvent> getServeEvents() {
        return wireMockServer != null ? wireMockServer.getAllServeEvents() : new ArrayList<>();
    }
}

