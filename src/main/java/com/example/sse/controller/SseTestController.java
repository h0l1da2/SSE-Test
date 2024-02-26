package com.example.sse.controller;

import com.example.sse.domain.User;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
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

    /**
     * ServerSentEvent 엔티티 사용하기
     * - 실제 시나리오에서 사용하는 이벤트 메타 데이터 처리 가능.
     * - TEXT_EVENT_STREAM_VALUE 미디어 유형 선언 무시 가능.
     */
    @GetMapping("/stream-sse")
    public Flux<ServerSentEvent<User>> eventEvents() {
        User user = User.builder()
                .name("holiday")
                .username("holiday_k")
                .age(28)
                .build();

        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> ServerSentEvent.<User>builder()
                        .id(String.valueOf(sequence))
                        .event("user-event")
                        .data(user)
                        .build());
    }

}
