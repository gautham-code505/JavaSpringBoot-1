package com.sece.housekeeptrack.controller;

import com.sece.housekeeptrack.entity.CleaningTask;
import com.sece.housekeeptrack.entity.Housekeeper;
import com.sece.housekeeptrack.entity.Room;
import com.sece.housekeeptrack.repository.CleaningTaskRepository;
import com.sece.housekeeptrack.repository.HousekeeperRepository;
import com.sece.housekeeptrack.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cleaning-tasks")
public class CleaningTaskController {

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HousekeeperRepository housekeeperRepository;

    @GetMapping
    public List<CleaningTask> getAllCleaningTasks() {
        return cleaningTaskRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CleaningTask> getCleaningTaskById(@PathVariable Long id) {
        Optional<CleaningTask> cleaningTask = cleaningTaskRepository.findById(id);
        return cleaningTask.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CleaningTask> createCleaningTask(@RequestBody CleaningTask cleaningTask) {
        // Resolve Room and Housekeeper to ensure they exist and are attached
        if (cleaningTask.getRoom() == null || cleaningTask.getHousekeeper() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Room> room = roomRepository.findById(cleaningTask.getRoom().getId());
        Optional<Housekeeper> housekeeper = housekeeperRepository.findById(cleaningTask.getHousekeeper().getId());

        if (room.isEmpty() || housekeeper.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        cleaningTask.setRoom(room.get());
        cleaningTask.setHousekeeper(housekeeper.get());

        return ResponseEntity.ok(cleaningTaskRepository.save(cleaningTask));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CleaningTask> updateCleaningTask(@PathVariable Long id, @RequestBody CleaningTask taskDetails) {
        Optional<CleaningTask> taskOptional = cleaningTaskRepository.findById(id);
        if (taskOptional.isPresent()) {
            CleaningTask task = taskOptional.get();
            
            if (taskDetails.getRoom() != null) {
                Optional<Room> room = roomRepository.findById(taskDetails.getRoom().getId());
                room.ifPresent(task::setRoom);
            }
            if (taskDetails.getHousekeeper() != null) {
                Optional<Housekeeper> housekeeper = housekeeperRepository.findById(taskDetails.getHousekeeper().getId());
                housekeeper.ifPresent(task::setHousekeeper);
            }

            task.setStatus(taskDetails.getStatus());
            task.setCreatedAt(taskDetails.getCreatedAt());
            task.setStartedAt(taskDetails.getStartedAt());
            task.setCompletedAt(taskDetails.getCompletedAt());

            return ResponseEntity.ok(cleaningTaskRepository.save(task));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCleaningTask(@PathVariable("id") Long id) {
        if (cleaningTaskRepository.existsById(id)) {
            cleaningTaskRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Autowired
    private com.sece.housekeeptrack.service.HousekeepingService housekeepingService;

    @PutMapping("/{id}/start")
    public ResponseEntity<?> startCleaning(@PathVariable("id") Long id) {
        return ResponseEntity.ok(housekeepingService.startCleaning(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeCleaning(@PathVariable("id") Long id) {
        return ResponseEntity.ok(housekeepingService.completeCleaning(id));
    }
}
