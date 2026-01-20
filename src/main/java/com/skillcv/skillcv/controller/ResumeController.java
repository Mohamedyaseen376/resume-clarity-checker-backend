package com.skillcv.skillcv.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.skillcv.skillcv.service.GeminiService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ResumeController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/analyze")
    public String analyzeResume(@RequestBody String resumeText) {
        return geminiService.analyzeResume(resumeText);
    }
}
