package com.uniduna.programming3.repository;

import com.uniduna.programming3.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Integer> {
    @Query("SELECT c FROM Chat c WHERE c.userId = :userId")
    List<Chat> findAllChatsOwnedByUser(@Param("userId") Integer userId);

    @Query("SELECT DISTINCT c FROM Chat c LEFT JOIN UserInvited ui ON ui.chatId = c.chatId LEFT JOIN Users u ON u.userId = ui.userId")
    List<Chat> findAllChats();
}
