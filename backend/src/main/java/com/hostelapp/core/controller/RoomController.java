package com.hostelapp.core.controller;

import com.hostelapp.core.entity.Room;
import com.hostelapp.core.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management — create rooms and manage bed bookings")
@SecurityRequirement(name = "bearerAuth")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a room", description = "Creates a new room in the current tenant's workspace")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Room created"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.createRoom(room));
    }

    @PostMapping("/{id}/book-atomic")
    @Operation(summary = "Book a bed (atomic)", description = "Books a bed using an atomic UPDATE query to prevent race conditions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bed booked successfully"),
            @ApiResponse(responseCode = "400", description = "No vacancies available")
    })
    public ResponseEntity<String> bookAtomic(@PathVariable Long id) {
        boolean success = roomService.bookBedAtomic(id);
        if (success) {
            return ResponseEntity.ok("Bed booked successfully using atomic update.");
        } else {
            return ResponseEntity.badRequest().body("Failed to book bed: no vacancies available.");
        }
    }

    @PostMapping("/{id}/book-locked")
    @Operation(summary = "Book a bed (pessimistic lock)", description = "Books a bed using a pessimistic database lock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bed booked successfully"),
            @ApiResponse(responseCode = "400", description = "No vacancies available")
    })
    public ResponseEntity<String> bookLocked(@PathVariable Long id) {
        boolean success = roomService.bookBedPessimistic(id);
        if (success) {
            return ResponseEntity.ok("Bed booked successfully using pessimistic lock.");
        } else {
            return ResponseEntity.badRequest().body("Failed to book bed: no vacancies available.");
        }
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Release a bed", description = "Increments vacant beds count when a tenant vacates")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bed released successfully"),
            @ApiResponse(responseCode = "400", description = "Vacancy already at capacity")
    })
    public ResponseEntity<String> releaseBed(@PathVariable Long id) {
        boolean success = roomService.releaseBedAtomic(id);
        if (success) {
            return ResponseEntity.ok("Bed released successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to release bed: vacancy is already at capacity.");
        }
    }
}

