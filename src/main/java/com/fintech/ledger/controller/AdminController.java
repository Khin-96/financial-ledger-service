package com.fintech.ledger.controller;

import com.fintech.ledger.service.ProjectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {
    
    private final ProjectionService projectionService;
    
    @PostMapping("/projections/rebuild")
    @Operation(summary = "Rebuild read model from event store")
    public ResponseEntity<String> rebuildProjections() {
        projectionService.rebuildAllProjections();
        return ResponseEntity.ok("Projections rebuilt successfully");
    }
}
