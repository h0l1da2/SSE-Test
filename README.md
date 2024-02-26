# Server-Sent Event

*웹 애플리케이션이 단방향 이벤트 스트림을 처리하고 서버가 데이터를 보낼 때마다 업데이트를 받을 수 있게 하는 HTTP 표준*

소켓 통신(전이중 모드)과 달리 반이중 모드 → *단방향 통신*

- 클라이언트에서 서버로 이벤트를 보낼 순 없다.

# 클라이언트

서버에서 보낸 이벤트를 수신하는 법은 간단하다.

1. `EventSource` 인스턴스 만들기
2. `message` Event 수신 → 이벤트에 대한 핸들러 연결
    1. 정의된 필드가 있다면, 지정된 이름의 이벤트로 수신된다.

### EventSource 인스턴스 만들기

```jsx
const evtSource = new EventSource("ssedemo.php");
```

→ 이건 같은 origin 일 경우

```jsx
const evtSource = new EventSource("//api.example.com/ssedemo.php", {
    withCredentials: true,
});
```

→ 다른 origin 에서 받는 거라면 url 과 옵션을 설정해주자 !

### message 이벤트 수신

```jsx
evtSource.onmessage = (event) => {
    const newElement = document.createElement("li");
    const eventList = document.getElementById("list");

    newElement.textContent = `message: ${event.data}`;
    eventList.appendChild(newElement);
};
```

→ 이게 기본이지만 보통 어떤 이벤트인지 명시할테니.. 아래 코드를 확인하는 것이 더 정확할듯.

```jsx
evtSource.addEventListener("ping", (event) => {
    const newElement = document.createElement("li");
    const eventList = document.getElementById("list");
    const time = JSON.parse(event.data).time;
    newElement.textContent = `ping at ${time}`;
    eventList.appendChild(newElement);
});
```

→ “ping” 이벤트를 받는 코드

- 이렇게 이벤트로 받은 값을 가져와 클라이언트에서 사용하면 됩니다 ^_^/

# 주의 사항

![Untitled](https://prod-files-secure.s3.us-west-2.amazonaws.com/d43185c1-2231-423a-96de-fb255729ae89/de5ded2f-d68d-49e7-ac70-2b5465bffba1/Untitled.png)

`HTTP/2` 를 사용하지 않는다면 한 브라우저에서는 6개의 이벤트까지만 연결될 수 있는듯.

그렇다면 서버에서는 어떻게 이벤트를 보낼까용?

# 서버에서 이벤트 보내기

- MIME type 을 이용해서 응답해야한다. (Content-tpye: text/event-stream)
- 각 알림은 텍스트 블록으로 전송된다.

# event-stream 형식

- UTF-8 을 사용하여 인코딩하는 간단한 텍스트 데이터 스트림.
- 한 쌍의 줄바꿈 문자로 구분되며, 줄 앞에 콜론 `:`  을 붙이면 주석이라 표시한다.
    - 주석을 이용해서 주기적으로 주석으로 설명을 보내면 연결 시간 초과를 방지할 수 있다!

## 필드

`event`  → 이벤트 이름.

- 이 이름을 받는 리스너로 해당 요청이 전달 됨.

`data`  → 데이터.

- 연속된 줄을 받으면 연결해줌.

`id`  → EventSource 개체의 마지막 이벤트 id 값

`retry`  → 재연결 시간.

- 서버와 연결이 끊어지면 브라우저가 다시 연결을 시도하기 전에 `retry` 시간까지 기다림.

### 예시 : 그냥 문자열 데이터만 보내는 이벤트

```bash
: this is a test stream # 주석

data: some text # 한 줄

data: another message # 총 두 줄 짜리 묶음 데이터
data: with two lines
```

### 예시 : json 데이터 보내는 이벤트

```bash
event: userconnect
data: {"username": "bobby", "time": "02:33:48"}

event: usermessage
data: {"username": "bobby", "time": "02:34:11", "text": "Hi everyone."}

event: userdisconnect
data: {"username": "bobby", "time": "02:34:23"}

event: usermessage
data: {"username": "sean", "time": "02:34:36", "text": "Bye, bobby."}
```

- json 으로는 형식을 이렇게 보낸다.

### 예시 : 혼합 이벤트

```bash
event: userconnect
data: {"username": "bobby", "time": "02:33:48"}

data: Here's a system message of some kind that will get used
data: to accomplish some task.

event: usermessage
data: {"username": "bobby", "time": "02:34:11", "text": "Hi everyone."}
```

- 요렇게 혼합해서 보낼 수도 있다.

# Spring 에서는 어떻게 씁니까 그럼?

```bash
dependencies {
	// dependency 추가
	implementation 'org.springframework.boot:spring-boot-starter-webflux:3.2.2'
}
```

## String 으로

```java
    @GetMapping(value = "/stream-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFlux() {
        // 서버에 초 단위로 현재 시간을 보내는 이벤트
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> "Flux Event : " + LocalTime.now());
    }
```

- 간단하게 Flux 객체에 문자열을 담아 서버에 초 단위로 시간을 보내는 이벤트를 보낸다.
- 문자열로 보내는 것.
- 이 방법을 사용할 경우 MediaType 명시가 필요. (불ㅡ편)

## ServerSentEvent 객체로

```java
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
                        .data(getUser())
                        .build());
    }
```

- ServerSentMessage 객체에 원하는 class type 으로 담아 보낼 수 있다.
- 1 초마다 해당 user 를 `user-event` 라는 이름으로 이벤트를 보낸다.
- 실제 sse 전송 시나리오를 그대로 사용 가능하고, `text/event-stream` 데이터 유형 선언을 하지 않아도 된다.

## 구독

```java
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
```

- SSE 구독하는 코드
    - 처음에 구독을 보내서 WebClient → 누군지 알려줘야 함.
    - 말 그대로 구독을 시작한다고 보면 된다.
- 성공 / 실패 시나리오 로그를 찍는다.

## SseEmitter 사용 예시

```java
    @GetMapping("/stream-sse-mvc")
    public SseEmitter streamSseMvc() {
        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();

        sseMvcExecutor.execute(() -> {
            for (int i = 0; true; i++) {
                try {
                    SseEventBuilder event = SseEmitter.event()
                            .id(String.valueOf(i))
                            .data(getUser())
                            .name("user-event-mvc");
                    emitter.send(event);
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    emitter.completeWithError(e);
                }
            }
        });
        return emitter;
    }
```

- `SseEmitter` 를 이용해 데이터를 푸쉬하는 작업을 하는 스레드를 정의한다.
- `Emitter` 인스턴스를 반환하고 연결을 열어둔다. 😊
    - 스레드가 1000milis 쉬며 계속 user 객체를 보내는 알림을 보내도록 설정하는 api.
    - 계속 열어두긴 하지만 소켓과 다르게 단방향으로 알림만 보낼 뿐!