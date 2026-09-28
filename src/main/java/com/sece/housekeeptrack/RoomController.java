package com.sece.housekeeptrack;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

    @GetMapping("/room")
    public String getRoom() {
        return "Room API working!";
    }

    @PostMapping("/room")
    public String addRoom() {
        return "Room added successfully!";
    }

    @PutMapping("/room")
    public String updateRoom() {
        return "Room updated successfully!";
    }

    @DeleteMapping("/room")
    public String deleteRoom() {
        return "Room deleted successfully!";
    }
}
