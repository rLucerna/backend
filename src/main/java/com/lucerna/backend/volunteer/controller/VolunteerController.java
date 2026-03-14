package com.lucerna.backend.volunteer.controller;

import com.lucerna.backend.volunteer.dto.VolunteerRequestDto;
import com.lucerna.backend.volunteer.dto.VolunteerResponseDto;
import com.lucerna.backend.volunteer.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @GetMapping
    public ResponseEntity<VolunteerResponseDto > getVolunteers(
            VolunteerRequestDto request) {
        VolunteerResponseDto  response = volunteerService.getVolunteers(request);
        return ResponseEntity.ok(response);
    }
}