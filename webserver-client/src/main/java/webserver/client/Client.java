package webserver.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.IntStream.range;

@Slf4j
@SpringBootApplication
public class Client {

    private static final int SERVER_PORT = 3000;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) {
        SpringApplication.run(Client.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        try (executor) {
            range(0, 1000).forEach(requestNumber -> {
                executor.submit(() -> sendRequest(requestNumber));
            });
        }
    }

    @SneakyThrows
    private void sendRequest(int requestNumber) {
        var request = "#%s".formatted(requestNumber);
        try (var socket = new Socket(InetAddress.getLocalHost(), SERVER_PORT)) {
            var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(request);
            writer.newLine();
            writer.flush();
            log.info("Request: {}", request);
            var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log.info("Response: {}", reader.readLine());
        }
    }
}
