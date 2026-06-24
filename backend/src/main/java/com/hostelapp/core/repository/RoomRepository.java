package com.hostelapp.core.repository;

import com.hostelapp.core.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Concurrency Guardrail 1: Atomic Query Decrement
    @Modifying
    @Query("UPDATE Room r SET r.vacantBeds = r.vacantBeds - 1 WHERE r.id = :id AND r.vacantBeds > 0")
    int decrementVacantBedsAtomic(@Param("id") Long id);

    // Atomic Query Increment (for release / cancellation)
    @Modifying
    @Query("UPDATE Room r SET r.vacantBeds = r.vacantBeds + 1 WHERE r.id = :id AND r.vacantBeds < r.totalBeds")
    int incrementVacantBedsAtomic(@Param("id") Long id);

    // Concurrency Guardrail 2: Pessimistic Locking
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
