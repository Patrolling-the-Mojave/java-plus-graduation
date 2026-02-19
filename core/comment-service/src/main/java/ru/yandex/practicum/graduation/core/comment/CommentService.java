package ru.yandex.practicum.graduation.core.comment;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import ru.practicum.stats.client.configuration.ClientConfig;

@Import(ClientConfig.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "ru.yandex.practicum.graduation.core.interaction")
@SpringBootApplication
public class CommentService {
    public static void main(String[] args) {
        SpringApplication.run(CommentService.class, args);
    }
}
