package com.hostelapp.core.service.impl;

import com.hostelapp.core.entity.Lease;
import com.hostelapp.core.entity.LeaseStatus;
import com.hostelapp.core.entity.Room;
import com.hostelapp.core.repository.LeaseRepository;
import com.hostelapp.core.repository.RoomRepository;
import com.hostelapp.core.service.LeaseService;
import com.hostelapp.core.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaseServiceImpl implements LeaseService {

    private final LeaseRepository leaseRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @Override
    @Transactional
    public Lease createLease(Lease lease) {
        Room room = roomRepository.findById(lease.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + lease.getRoom().getId()));
        
        boolean bookingSuccess = roomService.bookBedAtomic(room.getId());
        if (!bookingSuccess) {
            throw new IllegalStateException("Cannot create lease: No vacant beds available in Room " + room.getRoomNumber());
        }

        lease.setRoom(room);
        lease.setRentAmount(room.getPricePerMonth());
        return leaseRepository.save(lease);
    }

    @Override
    @Transactional
    public Lease terminateLease(Long leaseId) {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found: " + leaseId));

        if (lease.getStatus() != LeaseStatus.ACTIVE) {
            throw new IllegalStateException("Lease is not active, current status: " + lease.getStatus());
        }

        roomService.releaseBedAtomic(lease.getRoom().getId());

        lease.setStatus(LeaseStatus.TERMINATED);
        return leaseRepository.save(lease);
    }

    @Override
    public List<Lease> getActiveLeases() {
        return leaseRepository.findByStatus(LeaseStatus.ACTIVE);
    }
}
