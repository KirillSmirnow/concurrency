package webserver.server;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class Server {

    private static final int PORT = 3000;
    private static final int CONCURRENCY = 10;
    private static final int DATABASE_PORT = 3030;

    private final ExecutorService executor = CONCURRENCY <= 0 ? Executors.newVirtualThreadPerTaskExecutor() : Executors.newFixedThreadPool(CONCURRENCY);

    public static void main(String[] args) {
        SpringApplication.run(Server.class);
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
        try (
                var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var writer = new OutputStreamWriter(socket.getOutputStream());
        ) {
            var input = reader.readLine();
            var id = requestToDatabase();
            var output = "%s: [%s]%n".formatted(input, id);
            Thread.sleep(Duration.ofSeconds(1));
            writer.write(output);
        }
        log.info("finish {}", requestNumber);
    }

    @SneakyThrows
    private String requestToDatabase() {
        try (var socket = new Socket(InetAddress.getLocalHost(), DATABASE_PORT)) {
            var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return reader.readLine();
        }
    }
}
