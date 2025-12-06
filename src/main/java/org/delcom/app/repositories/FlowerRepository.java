package org.delcom.app.repositories;

import org.delcom.app.entities.Flower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface FlowerRepository extends JpaRepository<Flower, UUID> {
    List<Flower> findAllByUserId(UUID userId);
}