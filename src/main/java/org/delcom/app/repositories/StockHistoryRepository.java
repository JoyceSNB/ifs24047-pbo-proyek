package org.delcom.app.repositories;

import org.delcom.app.entities.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, UUID> {
    List<StockHistory> findByFlowerIdOrderByRecordedAtDesc(UUID flowerId);
}

