package webserver.database;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.UUID.randomUUID;

@Slf4j
@SpringBootApplication
public class Database {

    private static final int PORT = 3030;
    private static final int CONCURRENCY = 10;

    private final ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);

    public static void main(String[] args) {
        SpringApplication.run(Database.class);
    }

    @EventListener(ApplicationReadyEvent.class)
    @SneakyThrows
    public void run() {
        var serverSocket = new ServerSocket(PORT);
        var requestNumber = 0;
        while (true) {
            var socket = serverSocket.accept();
            var currentRequestNumber = requestNumber++;
            log.info("++++++ {}", currentRequestNumber);
            executor.submit(() -> processRequest(socket, currentRequestNumber));
        }
    }

    @SneakyThrows
    private void processRequest(Socket socket, int requestNumber) {
        log.info("start  {}", requestNumber);
        try (var writer = new OutputStreamWriter(socket.getOutputStream())) {
            Thread.sleep(Duration.ofSeconds(1));
            writer.write(randomUUID().toString());
        }
        log.info("finish {}", requestNumber);
    }
}
