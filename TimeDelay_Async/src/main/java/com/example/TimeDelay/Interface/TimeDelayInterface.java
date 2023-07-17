package com.example.TimeDelay.Interface;

import java.util.Set;
import java.util.UUID;

public interface TimeDelayInterface {

    boolean isProcessCompleted(UUID process_id);

    void markProcessCompleted(UUID process_id);

}
