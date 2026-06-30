package com.hostelapp.core.controller;

import com.hostelapp.core.entity.Lease;
import com.hostelapp.core.service.LeaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leases")
@RequiredArgsConstructor
@Tag(name = "Leases", description = "Lease lifecycle management — create, terminate, and list active leases")
@SecurityRequirement(name = "bearerAuth")
public class LeaseController {

    private final LeaseService leaseService;

    @PostMapping
    @Operation(summary = "Create a lease", description = "Creates a new lease binding a tenant to a room")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lease created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Lease> createLease(@RequestBody Lease lease) {
        return ResponseEntity.ok(leaseService.createLease(lease));
    }

    @PostMapping("/{id}/terminate")
    @Operation(summary = "Terminate a lease", description = "Marks the lease as TERMINATED and releases the bed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lease terminated"),
            @ApiResponse(responseCode = "404", description = "Lease not found")
    })
    public ResponseEntity<Lease> terminateLease(@PathVariable Long id) {
        return ResponseEntity.ok(leaseService.terminateLease(id));
    }

    @GetMapping("/active")
    @Operation(summary = "List active leases", description = "Returns all active leases for the current workspace")
    @ApiResponse(responseCode = "200", description = "List of active leases")
    public ResponseEntity<List<Lease>> getActiveLeases() {
        return ResponseEntity.ok(leaseService.getActiveLeases());
    }
}

