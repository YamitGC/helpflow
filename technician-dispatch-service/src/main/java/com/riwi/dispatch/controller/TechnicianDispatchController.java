package com.riwi.dispatch.controller;

import com.riwi.dispatch.dto.DispatchRequest;
import com.riwi.dispatch.dto.DispatchResponse;
import com.riwi.dispatch.service.TechnicianDispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/technician-dispatch")
@RequiredArgsConstructor
public class TechnicianDispatchController {

    private final TechnicianDispatchService dispatchService;

    @PostMapping
    public ResponseEntity<DispatchResponse> dispatch(@Valid @RequestBody DispatchRequest request) {
        DispatchResponse response = dispatchService.dispatch(request);
        return ResponseEntity.ok(response);
    }
}

