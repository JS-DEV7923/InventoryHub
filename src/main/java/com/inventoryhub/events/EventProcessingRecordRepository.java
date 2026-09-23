package com.inventoryhub.events;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventProcessingRecordRepository extends JpaRepository<EventProcessingRecord, EventProcessingRecordId> {
}
