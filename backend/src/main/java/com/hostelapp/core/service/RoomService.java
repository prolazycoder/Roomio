package com.hostelapp.core.service;

import com.hostelapp.core.entity.Room;

public interface RoomService {
    Room createRoom(Room room);
    boolean bookBedAtomic(Long roomId);
    boolean bookBedPessimistic(Long roomId);
    boolean releaseBedAtomic(Long roomId);
}
