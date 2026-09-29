package com.sece.housekeeptrack.service;

import com.sece.housekeeptrack.entity.CleaningTask;
import com.sece.housekeeptrack.entity.Housekeeper;
import com.sece.housekeeptrack.entity.Inspection;
import com.sece.housekeeptrack.entity.Room;
import com.sece.housekeeptrack.enums.CleaningTaskStatus;
import com.sece.housekeeptrack.enums.RoomStatus;
import com.sece.housekeeptrack.exception.BusinessException;
import com.sece.housekeeptrack.exception.ResourceNotFoundException;
import com.sece.housekeeptrack.repository.CleaningTaskRepository;
import com.sece.housekeeptrack.repository.HousekeeperRepository;
import com.sece.housekeeptrack.repository.InspectionRepository;
import com.sece.housekeeptrack.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HousekeepingService {

    private final RoomRepository roomRepository;
    private final HousekeeperRepository housekeeperRepository;
    private final CleaningTaskRepository cleaningTaskRepository;
    private final InspectionRepository inspectionRepository;

    public HousekeepingService(
            RoomRepository roomRepository,
            HousekeeperRepository housekeeperRepository,
            CleaningTaskRepository cleaningTaskRepository,
            InspectionRepository inspectionRepository) {
        this.roomRepository = roomRepository;
        this.housekeeperRepository = housekeeperRepository;
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.inspectionRepository = inspectionRepository;
    }

    @Transactional
    public CleaningTask checkoutRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (room.getStatus() != RoomStatus.READY) {
            throw new BusinessException("Room is not READY. Cannot checkout.");
        }

        room.setStatus(RoomStatus.DIRTY);
        roomRepository.save(room);

        Housekeeper housekeeper = housekeeperRepository.findFirstByAvailableTrue()
                .orElseThrow(() -> new BusinessException("No available housekeeper to assign"));

        housekeeper.setAvailable(false);
        housekeeperRepository.save(housekeeper);

        CleaningTask task = new CleaningTask(room, housekeeper, CleaningTaskStatus.ASSIGNED);
        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public CleaningTask startCleaning(Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() != CleaningTaskStatus.ASSIGNED) {
            throw new BusinessException("Cleaning task is not ASSIGNED");
        }

        Room room = task.getRoom();
        if (room.getStatus() != RoomStatus.DIRTY) {
            throw new BusinessException("Room is not DIRTY");
        }

        task.setStatus(CleaningTaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());
        cleaningTaskRepository.save(task);

        room.setStatus(RoomStatus.CLEANING);
        roomRepository.save(room);

        return task;
    }

    @Transactional
    public CleaningTask completeCleaning(Long taskId) {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() != CleaningTaskStatus.IN_PROGRESS) {
            throw new BusinessException("Cleaning task is not IN_PROGRESS");
        }

        task.setStatus(CleaningTaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        cleaningTaskRepository.save(task);

        Housekeeper housekeeper = task.getHousekeeper();
        housekeeper.setAvailable(true);
        housekeeperRepository.save(housekeeper);

        return task;
    }

    @Transactional
    public Room inspectRoom(Long roomId, Inspection inspectionData) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (room.getStatus() != RoomStatus.CLEANING) {
            throw new BusinessException("Room must be CLEANING to be inspected");
        }

        CleaningTask task = cleaningTaskRepository.findFirstByRoomIdAndStatusOrderByCompletedAtDesc(roomId, CleaningTaskStatus.COMPLETED)
                .orElseThrow(() -> new BusinessException("No valid completed cleaning task found for this room"));

        Inspection inspection = new Inspection(room, inspectionData.getSupervisorName(), inspectionData.isPassed(), inspectionData.getRemarks());
        if (inspectionData.getInspectionTime() != null) {
            inspection.setInspectionTime(inspectionData.getInspectionTime());
        } else {
            inspection.setInspectionTime(LocalDateTime.now());
        }
        
        inspectionRepository.save(inspection);

        if (inspection.isPassed()) {
            room.setStatus(RoomStatus.READY);
        } else {
            // Fails inspection, goes back to DIRTY (or remains CLEANING, rules said CLEANING -> CLEANING)
            // The prompt says: "If passed == false: 2. Room remains/returns to CLEANING. 3. It must NOT become READY."
            room.setStatus(RoomStatus.CLEANING);
        }
        
        return roomRepository.save(room);
    }

    @Transactional
    public Room assignRoomToGuest(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (room.getStatus() != RoomStatus.READY) {
            throw new BusinessException("Room is not READY. Cannot assign to guest.");
        }

        return room;
    }

    public List<Map<String, Object>> getHousekeeperWorkload() {
        List<Housekeeper> housekeepers = housekeeperRepository.findAll();
        List<CleaningTask> allTasks = cleaningTaskRepository.findAll();
        
        List<Map<String, Object>> workload = new ArrayList<>();
        
        for (Housekeeper h : housekeepers) {
            long active = 0;
            long completed = 0;
            
            for (CleaningTask t : allTasks) {
                if (t.getHousekeeper().getId().equals(h.getId())) {
                    if (t.getStatus() == CleaningTaskStatus.COMPLETED) {
                        completed++;
                    } else {
                        active++;
                    }
                }
            }
            
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("housekeeperId", h.getId());
            map.put("name", h.getName());
            map.put("activeTasks", active);
            map.put("completedTasks", completed);
            map.put("available", h.isAvailable());
            workload.add(map);
        }
        
        return workload;
    }

    public Map<String, Object> getAverageTurnaroundTime() {
        Double avg = cleaningTaskRepository.calculateAverageTurnaroundTimeMinutes();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("averageTurnaroundMinutes", avg != null ? avg : 0.0);
        return res;
    }
}
