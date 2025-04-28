package org.codenova.groupwareback.repository;

import org.codenova.groupwareback.entity.Serial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerialRepository extends JpaRepository<Serial, Integer> {
}
