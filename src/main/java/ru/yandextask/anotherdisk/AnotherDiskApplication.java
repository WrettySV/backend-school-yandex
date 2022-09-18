package ru.yandextask.anotherdisk;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Yet Another Disk Open API", version = "1.0",
        description = "Вступительное задание в Осеннюю Школу Бэкенд Разработки Яндекса 2022"))
public class AnotherDiskApplication {
    public static void main(String[] args) {

        SpringApplication.run(AnotherDiskApplication.class,args);

    }
}
