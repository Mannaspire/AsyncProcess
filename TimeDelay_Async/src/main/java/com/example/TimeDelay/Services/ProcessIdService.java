package com.example.TimeDelay.Services;

import com.example.TimeDelay.Interface.TimeDelayInterface;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class ProcessIdService implements TimeDelayInterface {

    private final Set<UUID> completedProcessIds;

    public ProcessIdService() {
        completedProcessIds = new HashSet<>();
    }

    @Override
    public boolean isProcessCompleted(UUID process_id) {
            return completedProcessIds.contains(process_id);
    }

    @Override
    public void markProcessCompleted(UUID process_id) {
            completedProcessIds.add(process_id);
    }

}
