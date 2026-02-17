package ru.yandex.practicum.graduation.core.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.stats.client.configuration.ClientConfig;

@SpringBootApplication
@Import(ClientConfig.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.yandex.practicum.graduation.core.interaction")
public class RequestService {
    public static void main(String[] args) {
        SpringApplication.run(RequestService.class, args);
    }
}
