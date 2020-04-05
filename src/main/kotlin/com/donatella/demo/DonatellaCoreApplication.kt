package com.donatella.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.TelegramBotsApi



@SpringBootApplication
class DonatellaCoreApplication

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<DonatellaCoreApplication>(*args)
}
