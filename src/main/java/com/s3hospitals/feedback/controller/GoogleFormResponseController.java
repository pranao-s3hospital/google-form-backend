package com.s3hospitals.feedback.controller;

import com.s3hospitals.feedback.model.RequestParams;
import com.s3hospitals.feedback.service.GoogleFormResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api")
public class GoogleFormResponseController {

    @Autowired
    private GoogleFormResponseService googleFormResponseService;

    @PostMapping("/getFeedbackResponse")
    public ResponseEntity<byte[]> getFeedbackResponse(@RequestBody RequestParams requestParams) throws GeneralSecurityException, IOException {
        byte[] output = googleFormResponseService.readGoogleSheet(requestParams);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.xlsx");
        headers.set(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return new ResponseEntity<>(output, headers, HttpStatus.OK);
    }
}

