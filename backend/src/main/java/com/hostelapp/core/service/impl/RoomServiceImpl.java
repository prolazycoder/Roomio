package com.hostelapp.core.service.impl;

import com.hostelapp.core.entity.Room;
import com.hostelapp.core.repository.RoomRepository;
import com.hostelapp.core.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public Room createRoom(Room room) {
        if (room.getVacantBeds() == null) {
            room.setVacantBeds(room.getTotalBeds());
        }
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public boolean bookBedAtomic(Long roomId) {
        int updatedRows = roomRepository.decrementVacantBedsAtomic(roomId);
        return updatedRows > 0;
    }

    @Override
    @Transactional
    public boolean bookBedPessimistic(Long roomId) {
        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

        if (room.getVacantBeds() > 0) {
            room.setVacantBeds(room.getVacantBeds() - 1);
            roomRepository.save(room);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean releaseBedAtomic(Long roomId) {
        int updatedRows = roomRepository.incrementVacantBedsAtomic(roomId);
        return updatedRows > 0;
    }
}
