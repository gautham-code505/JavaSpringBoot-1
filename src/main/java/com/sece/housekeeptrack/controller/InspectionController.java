package com.sece.housekeeptrack.controller;

import com.sece.housekeeptrack.entity.Inspection;
import com.sece.housekeeptrack.entity.Room;
import com.sece.housekeeptrack.repository.InspectionRepository;
import com.sece.housekeeptrack.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @GetMapping
    public List<Inspection> getAllInspections() {
        return inspectionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inspection> getInspectionById(@PathVariable("id") Long id) {
        Optional<Inspection> inspection = inspectionRepository.findById(id);
        return inspection.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Inspection> createInspection(@RequestBody Inspection inspection) {
        if (inspection.getRoom() == null || inspection.getRoom().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Room> room = roomRepository.findById(inspection.getRoom().getId());
        if (room.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        inspection.setRoom(room.get());
        return ResponseEntity.ok(inspectionRepository.save(inspection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inspection> updateInspection(@PathVariable("id") Long id, @RequestBody Inspection inspectionDetails) {
        Optional<Inspection> inspectionOptional = inspectionRepository.findById(id);
        
        if (inspectionOptional.isPresent()) {
            Inspection inspection = inspectionOptional.get();

            if (inspectionDetails.getRoom() != null && inspectionDetails.getRoom().getId() != null) {
                Optional<Room> room = roomRepository.findById(inspectionDetails.getRoom().getId());
                room.ifPresent(inspection::setRoom);
            }

            inspection.setSupervisorName(inspectionDetails.getSupervisorName());
            inspection.setPassed(inspectionDetails.isPassed());
            inspection.setRemarks(inspectionDetails.getRemarks());
            inspection.setInspectionTime(inspectionDetails.getInspectionTime());

            return ResponseEntity.ok(inspectionRepository.save(inspection));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInspection(@PathVariable("id") Long id) {
        if (inspectionRepository.existsById(id)) {
            inspectionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
