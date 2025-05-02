package org.codenova.groupwareback.repository;

import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.entity.NoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteStatusRepository extends JpaRepository<NoteStatus, Long> {

    public List<NoteStatus> findAllByReceiver(Employee receiver);
}
