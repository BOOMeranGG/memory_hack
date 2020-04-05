package com.donatella.demo.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule

class JacksonUtil {

    companion object {
        fun serializingObjectMapper(): ObjectMapper {
            return ObjectMapper()
                    .registerModule(ParameterNamesModule())
                    .registerModule(Jdk8Module())
                    .registerModule(JavaTimeModule())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        }
    }
}