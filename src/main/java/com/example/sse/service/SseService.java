package com.example.sse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalTime;

@Slf4j
@Service
public class SseService {

    // SSE Subscribe
    // inStatus 문을 추가하지 않으면 4xx , 5xx 응답에서 WebClientResponseException 발생
    public void consumeServerSentEvent() {
        WebClient webClient = WebClient.create("http://localhost:8000/sse-server");

        // body Type
        ParameterizedTypeReference<ServerSentEvent<String>> type =
                new ParameterizedTypeReference<>() {};

        Flux<ServerSentEvent<String>> eventStream = webClient.get()
                .uri("/stream-sse")
                .retrieve()
                .bodyToFlux(type);

        // SSE subscribe
        eventStream.subscribe(
               // success
                content -> log.info("Time: {} - event: name[{}], id [{}], content[{}] ",
                        LocalTime.now(), content.event(), content.id(), content.data()),
                // error
                error -> log.error("Error receiving SSE: {}", error),
                () -> log.info("Completed!!!")
        );
    }
}
