package com.sece.housekeeptrack.controller;

import com.sece.housekeeptrack.entity.Room;
import com.sece.housekeeptrack.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomRepository.findById(id);
        return room.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Room createRoom(@Valid @RequestBody Room room) {
        return roomRepository.save(room);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody Room roomDetails) {
        Optional<Room> roomOptional = roomRepository.findById(id);
        if (roomOptional.isPresent()) {
            Room room = roomOptional.get();
            room.setRoomNumber(roomDetails.getRoomNumber());
            room.setRoomType(roomDetails.getRoomType());
            room.setStatus(roomDetails.getStatus());
            return ResponseEntity.ok(roomRepository.save(room));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        Optional<Room> roomOptional = roomRepository.findById(id);
        if (roomOptional.isPresent()) {
            roomRepository.delete(roomOptional.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Autowired
    private com.sece.housekeeptrack.service.HousekeepingService housekeepingService;

    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkoutRoom(@PathVariable("id") Long id) {
        return ResponseEntity.ok(housekeepingService.checkoutRoom(id));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignRoomToGuest(@PathVariable("id") Long id) {
        return ResponseEntity.ok(housekeepingService.assignRoomToGuest(id));
    }

    @PostMapping("/{id}/inspection")
    public ResponseEntity<?> inspectRoom(@PathVariable("id") Long id, @RequestBody com.sece.housekeeptrack.entity.Inspection inspection) {
        return ResponseEntity.ok(housekeepingService.inspectRoom(id, inspection));
    }

    @GetMapping("/turnaround-time")
    public ResponseEntity<?> getAverageTurnaroundTime() {
        return ResponseEntity.ok(housekeepingService.getAverageTurnaroundTime());
    }
}
