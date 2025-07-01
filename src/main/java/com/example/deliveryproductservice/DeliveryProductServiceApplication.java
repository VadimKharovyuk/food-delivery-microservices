package com.example.deliveryproductservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeliveryProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryProductServiceApplication.class, args);
        ///добавить избраные маназины

//        HTTP 422 - это код ошибки "Unprocessable Entity", означающий, что сервер понял запрос, но не смог его обработать
//        "Query too long - 82/20 tokens" - это означает, что ваш адрес содержит 82 токена, а лимит Mapbox составляет всего 20 токенов
//
//        Проблема: Адрес "ул. Героев Труда 22 А, Харьков, Харьковская, Украина" после URL-кодирования стал слишком длинным для API Mapbox.
//        Решения:
//
//        Сократите адрес - используйте только основные части:
//
//        Вместо: "ул. Героев Труда 22 А, Харьков, Харьковская, Украина"
//        Попробуйте: "Героев Труда 22А, Харьков" или просто "Харьков"

        }

    }
