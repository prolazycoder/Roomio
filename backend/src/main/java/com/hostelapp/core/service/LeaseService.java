package com.hostelapp.core.service;

import com.hostelapp.core.entity.Lease;
import java.util.List;

public interface LeaseService {
    Lease createLease(Lease lease);
    Lease terminateLease(Long leaseId);
    List<Lease> getActiveLeases();
}
