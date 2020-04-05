package com.donatella.demo.api

import com.donatella.demo.model.dto.ImageBase64Dto
import com.donatella.demo.model.dto.SimilarityDto
import org.apache.commons.io.FileUtils.readFileToByteArray
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.File
import java.util.*


private const val SIMILARITY_URI = "https://8b7f58b8.ngrok.io/compared-faces"

@Component
class SimilarityChecker {

    fun howMuchSimilarityPercent(userFileName: String, veteranFileName: String, chatId: String): Int {
        val userFile = File(userFileName)
        val veteranFile = File(veteranFileName)
        val userBytes = readFileToByteArray(userFile)
        val veteranBytes = readFileToByteArray(veteranFile)

        val userBase64 = Base64
            .getEncoder()
            .encodeToString(userBytes)
        val veteranBase64 = Base64
            .getEncoder()
            .encodeToString(veteranBytes)

        val similarityDto = SimilarityDto()
        similarityDto.faces.add(ImageBase64Dto().also {
            it.type = "son"
            it.base64String = userBase64
            it.chatId = chatId
        })
        similarityDto.faces.add(ImageBase64Dto().also {
            it.type = "grandpa"
            it.base64String = veteranBase64
            it.chatId = chatId
        })

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val request = HttpEntity(similarityDto, headers)
        var result = restTemplate.postForObject(SIMILARITY_URI, request, String::class.java)

        if (result == "{\"percent\":-1.0}\n") {
            return -1
        }
        result = result?.replace("{\"percent\":", "")
        result = result?.replace("}\n", "")

        return try {
            val abc = result!!.toDouble()
            return abc.toInt()
        } catch (ex: Exception) {
            0
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
}