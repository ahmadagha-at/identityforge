package dev.identityforge.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.identityforge.client.web.ClientGatewayException;
import dev.identityforge.client.web.UserInfoClient;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

class UserInfoClientTest {

    @Test
    void returnsTheUserInfoResponse() {
        WebClient webClient = WebClient.builder().exchangeFunction(request ->
                reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .body("{\"sub\":\"alice-subject\",\"name\":\"Alice\"}")
                        .build())).build();
        UserInfoClient client = new UserInfoClient(webClient, "http://auth-server:9000/userinfo");

        assertThat(client.load()).containsAllEntriesOf(
                Map.of("sub", "alice-subject", "name", "Alice"));
    }

    @Test
    void translatesRejectedUserInfoRequests() {
        WebClient webClient = WebClient.builder().exchangeFunction(request ->
                reactor.core.publisher.Mono.just(ClientResponse.create(HttpStatus.UNAUTHORIZED)
                        .build())).build();
        UserInfoClient client = new UserInfoClient(webClient, "http://auth-server:9000/userinfo");

        assertThatThrownBy(client::load)
                .isInstanceOf(ClientGatewayException.class)
                .hasMessage("The UserInfo request was rejected.");
    }
}
