package com.example.sse.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;

@RestController
@RequestMapping("/sse")
public class SseTestController {

    @GetMapping(value = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        // 서버에 초 단위로 현재 시간을 보내는 이벤트
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux Event : " + LocalTime.now());
    }

}
