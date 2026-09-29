package com.sece.housekeeptrack.repository;

import com.sece.housekeeptrack.entity.CleaningTask;
import com.sece.housekeeptrack.enums.CleaningTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {
    Optional<CleaningTask> findFirstByRoomIdAndStatusOrderByCompletedAtDesc(Long roomId, CleaningTaskStatus status);
    
    List<CleaningTask> findByStatus(CleaningTaskStatus status);
    List<CleaningTask> findByHousekeeperId(Long housekeeperId);

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, c.created_at, i.inspection_time)) " +
                   "FROM cleaning_tasks c " +
                   "JOIN inspections i ON c.room_id = i.room_id " +
                   "WHERE i.passed = true AND c.status = 'COMPLETED' " +
                   "AND i.inspection_time >= c.completed_at", nativeQuery = true)
    Double calculateAverageTurnaroundTimeMinutes();
}
