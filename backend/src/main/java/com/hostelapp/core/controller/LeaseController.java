package com.hostelapp.core.controller;

import com.hostelapp.core.entity.Lease;
import com.hostelapp.core.service.LeaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/leases")
@RequiredArgsConstructor
public class LeaseController {

    private final LeaseService leaseService;

    @PostMapping
    public ResponseEntity<Lease> createLease(@RequestBody Lease lease) {
        return ResponseEntity.ok(leaseService.createLease(lease));
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<Lease> terminateLease(@PathVariable Long id) {
        return ResponseEntity.ok(leaseService.terminateLease(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Lease>> getActiveLeases() {
        return ResponseEntity.ok(leaseService.getActiveLeases());
    }
}
