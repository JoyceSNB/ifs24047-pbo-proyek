package org.delcom.app.repositories;

import org.delcom.app.entities.Flower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface FlowerRepository extends JpaRepository<Flower, UUID> {
    List<Flower> findAllByUserIdOrderByCreatedAtAsc(UUID userId);

    List<Flower> findByUserIdAndFlowerNameContainingIgnoreCaseOrderByCreatedAtAsc(UUID userId, String flowerName);
}