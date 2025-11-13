package com.uniduna.programming3.repository;

import com.uniduna.programming3.model.UserInvited;
import com.uniduna.programming3.model.Users;
import com.uniduna.programming3.model.Chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInvitedRepository extends JpaRepository<UserInvited, Integer> {

    // Getting all the UserId from each User invited to a Chat
    @Query("SELECT DISTINCT userId FROM UserInvited WHERE chatId = :chatId")
    Optional<List<Integer>> findAllInvitedUsersByChat(@Param("chatId") Integer chatId);

    // Getting all invitations of a Chat
    @Query("SELECT ui FROM UserInvited ui WHERE ui.chatId = :chatId")
    Optional<List<UserInvited>> findAllInvitationsByChat(@Param("chatId") Integer chatId);

    // Get the acceptance state of a User regarding a Chat
    @Query("SELECT ui.isAccepted FROM UserInvited ui WHERE ui.chatId = :chatId AND ui.userId = :userId")
    Optional<Boolean> findAcceptationStateByChatByUser(@Param("chatId") Integer chatId,@Param("userId") Integer userId);

    // Updating the acceptance state of a User regarding a Chat
    @Modifying
    @Query("UPDATE UserInvited ui SET ui.isAccepted = :isAccepted WHERE ui.chatId = :chatId AND ui.userId = :userId")
    void updateAcceptationStateByChatByUser(@Param("chatId") Integer chatId,@Param("userId") Integer userId, @Param("isAccepted") Boolean isAccepted);

    // Deleting a invitation of a User to a Chat
    @Modifying
    @Query("DELETE FROM UserInvited ui WHERE ui.chatId = :chatId AND ui.userId = :userId")
    void removeInvitationByChatByUser(@Param("chatId") Integer chatId,@Param("userId") Integer userId);
}