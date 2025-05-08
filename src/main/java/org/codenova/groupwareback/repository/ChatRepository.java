package org.codenova.groupwareback.repository;

import org.codenova.groupwareback.entity.Chat;
import org.codenova.groupwareback.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    public List<Chat> findAllByDepartmentOrderById(Department department);

}
