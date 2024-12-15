package hello.hello_spring.conroller;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.UUID;

@Controller
public class HelloController {
    @GetMapping("hello")
    public String hello(Model model) {
        model.addAttribute("data", "hello");
        return "hello";
    }

    @GetMapping("cookie")
    public ResponseEntity<Void> setCookies(@RequestParam("kafkaName") String kafkaName) {
        String clientId = UUID.randomUUID().toString();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie", "kafkaName=" + kafkaName + "; Path=/; HttpOnly");
        headers.add("Set-Cookie", "clientId=" + clientId + "; Path=/; HttpOnly");
        return ResponseEntity.ok().headers(headers).build();
    }

    @GetMapping("sse")
    public SseEmitter getMethodName(@CookieValue("clientId") String clientId,
            @CookieValue("kafkaName") String kafkaName) {
        SseEmitter emitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        emitter.onCompletion(() -> {
            System.out.println("SSE connection completed for clientId: " + clientId);
            executor.shutdown();
        });
        emitter.onTimeout(() -> {
            System.out.println("SSE connection timed out for clientId: " + clientId);
            emitter.complete();
            executor.shutdown();
        });
        emitter.onError((ex) -> {
            System.out.println("SSE connection error for clientId: " + clientId);
            emitter.completeWithError(ex);
            executor.shutdown();
        });
        executor.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("Hello " + clientId + ", message #" + (i + 1) + " from " + kafkaName));
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });

        return emitter;
    }

    @GetMapping("sse1")
    public SseEmitter getasd(@RequestParam("clientId") String clientId) {
        SseEmitter emitter = new SseEmitter();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        emitter.onCompletion(() -> {
            System.out.println("SSE connection completed for clientId: " + clientId);
            executor.shutdown();
        });
        emitter.onTimeout(() -> {
            System.out.println("SSE connection timed out for clientId: " + clientId);
            emitter.complete();
            executor.shutdown();
        });
        emitter.onError((ex) -> {
            System.out.println("SSE connection error for clientId: " + clientId);
            emitter.completeWithError(ex);
            executor.shutdown();
        });
        executor.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("Hello " + clientId + ", message #" + (i + 1)));
                    Thread.sleep(1000);
                }
                emitter.complete();
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });

        return emitter;
    }
}
