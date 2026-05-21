package com.university.oms.controller;

import com.university.oms.common.ApiResponse;
import com.university.oms.dto.TravelReimburseRequest;
import com.university.oms.dto.TravelRequest;
import com.university.oms.model.Travel;
import com.university.oms.service.TravelService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/travels")
public class TravelController {
    private final TravelService service;

    public TravelController(TravelService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Travel>> list() {
        return ApiResponse.ok(service.list());
    }

    @PostMapping
    public ApiResponse<Travel> create(@Valid @RequestBody TravelRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PostMapping("/{id}/reimburse")
    public ApiResponse<Travel> reimburse(@PathVariable Long id, @Valid @RequestBody TravelReimburseRequest request) {
        return ApiResponse.ok(service.reimburse(id, request));
    }
}
