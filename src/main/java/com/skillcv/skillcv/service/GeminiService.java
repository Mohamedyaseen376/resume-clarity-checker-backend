package com.skillcv.skillcv.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Ordered by power → stability
    private static final String[] MODELS = {
            "models/gemini-2.5-flash",
            "models/gemini-2.0-flash",
            "models/gemini-2.0-flash-lite"
    };

    public String analyzeResume(String resumeText) {

        for (String model : MODELS) {
            try {
                return callGemini(model, resumeText);
            } catch (HttpServerErrorException e) {
                if (e.getStatusCode().value() == 503) {
                    System.out.println("Model overloaded, trying next model...");
                } else {
                    throw e;
                }
            }
        }

        return "All AI models are currently busy. Please try again later.";
    }

    private String callGemini(String model, String resumeText) {

        try {
            String url = "https://generativelanguage.googleapis.com/v1/"
                    + model + ":generateContent?key=" + apiKey;

            String prompt = "You are a professional HR resume reviewer. Analyze the resume below and give:\n"
                    + "1. Overall resume score out of 10\n\n"
                    + "Resume:\n" + resumeText;

            String requestBody = """
            {
              "contents": [
                {
                  "parts": [
                    { "text": "%s" }
                  ]
                }
              ]
            }
            """.formatted(prompt.replace("\"", "\\\""));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, entity, String.class);

            return extractPureText(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return "Error while processing AI response.";
        }
    }

    // ✅ Extract only clean feedback text
    private String extractPureText(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            return "Failed to extract AI response.";
        }
    }
}
