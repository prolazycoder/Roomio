package com.hostelapp.core.controller;

import com.hostelapp.core.entity.Room;
import com.hostelapp.core.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomService.createRoom(room));
    }

    @PostMapping("/{id}/book-atomic")
    public ResponseEntity<String> bookAtomic(@PathVariable Long id) {
        boolean success = roomService.bookBedAtomic(id);
        if (success) {
            return ResponseEntity.ok("Bed booked successfully using atomic update.");
        } else {
            return ResponseEntity.badRequest().body("Failed to book bed: no vacancies available.");
        }
    }

    @PostMapping("/{id}/book-locked")
    public ResponseEntity<String> bookLocked(@PathVariable Long id) {
        boolean success = roomService.bookBedPessimistic(id);
        if (success) {
            return ResponseEntity.ok("Bed booked successfully using pessimistic lock.");
        } else {
            return ResponseEntity.badRequest().body("Failed to book bed: no vacancies available.");
        }
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<String> releaseBed(@PathVariable Long id) {
        boolean success = roomService.releaseBedAtomic(id);
        if (success) {
            return ResponseEntity.ok("Bed released successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to release bed: vacancy is already at capacity.");
        }
    }
}
