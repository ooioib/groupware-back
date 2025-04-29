package org.codenova.groupwareback.repository;

import org.codenova.groupwareback.entity.Serial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerialRepository extends JpaRepository<Serial, Integer> {

    public Optional<Serial> findByRef(String ref);

}
