package org.codenova.groupwareback.repository;

import org.codenova.groupwareback.entity.Employee;
import org.codenova.groupwareback.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    public List<Note> findAllBySender(Employee sender);

}
