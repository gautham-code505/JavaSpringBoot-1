package com.sece.housekeeptrack.controller;

import com.sece.housekeeptrack.entity.Housekeeper;
import com.sece.housekeeptrack.repository.HousekeeperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/housekeepers")
public class HousekeeperController {

    @Autowired
    private HousekeeperRepository housekeeperRepository;

    @GetMapping
    public List<Housekeeper> getAllHousekeepers() {
        return housekeeperRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Housekeeper> getHousekeeperById(@PathVariable Long id) {
        Optional<Housekeeper> housekeeper = housekeeperRepository.findById(id);
        return housekeeper.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Housekeeper createHousekeeper(@Valid @RequestBody Housekeeper housekeeper) {
        return housekeeperRepository.save(housekeeper);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Housekeeper> updateHousekeeper(@PathVariable Long id, @Valid @RequestBody Housekeeper housekeeperDetails) {
        Optional<Housekeeper> housekeeperOptional = housekeeperRepository.findById(id);
        if (housekeeperOptional.isPresent()) {
            Housekeeper housekeeper = housekeeperOptional.get();
            housekeeper.setName(housekeeperDetails.getName());
            housekeeper.setPhone(housekeeperDetails.getPhone());
            housekeeper.setAvailable(housekeeperDetails.isAvailable());
            return ResponseEntity.ok(housekeeperRepository.save(housekeeper));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHousekeeper(@PathVariable Long id) {
        Optional<Housekeeper> housekeeperOptional = housekeeperRepository.findById(id);
        if (housekeeperOptional.isPresent()) {
            housekeeperRepository.delete(housekeeperOptional.get());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Autowired
    private com.sece.housekeeptrack.service.HousekeepingService housekeepingService;

    @GetMapping("/workload")
    public ResponseEntity<?> getHousekeeperWorkload() {
        return ResponseEntity.ok(housekeepingService.getHousekeeperWorkload());
    }
}
