package com.hostelapp.core.repository;

import com.hostelapp.core.entity.Lease;
import com.hostelapp.core.entity.LeaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {
    List<Lease> findByStatus(LeaseStatus status);
}
