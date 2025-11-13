package com.uniduna.programming3.services;

import com.uniduna.programming3.model.UserInvited;
import com.uniduna.programming3.model.Users;
import com.uniduna.programming3.model.Chat;
import com.uniduna.programming3.repository.ChatRepository;
import com.uniduna.programming3.repository.UserRepository;
import com.uniduna.programming3.repository.UserInvitedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Date;

@Repository
@Transactional
public class ServicesRequest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserInvitedRepository userInvitedRepository;

    @Autowired
    ChatRepository chatRepository;

    public void createOrUpdateUser(Users user){
        userRepository.save(user);
    }

    public Optional<Users> getUser(int userId){
        return userRepository.findById(userId);
    }

    public Optional<Users> getUserByMail(String mail){
        return userRepository.findUserByMail(mail);
    }

    public void deleteUser(int userId){
        userRepository.deleteById(userId);
    }

    public List<Users> getAllUsers(){
        return userRepository.findAll();
    }

    public Optional<List<Integer>> getAllInvitedUsersByChat(int chatId){
        return userInvitedRepository.findAllInvitedUsersByChat(chatId);
    }

    public Optional<Boolean> getAcceptationStateByChatByUser(int chatId, int userId){
        return userInvitedRepository.findAcceptationStateByChatByUser(chatId,userId);
    }
    public void updateAcceptationStateByChatByUser(UserInvited ui){
        userInvitedRepository.updateAcceptationStateByChatByUser(ui.getChatId(),ui.getUserId(),ui.getIsAccepted());
    }
    public void deleteInvitationByChatByUser(int chatId,int userId){
        userInvitedRepository.removeInvitationByChatByUser(chatId,userId);
    }
    public void createOrUpdateChat(Chat chat){
        chatRepository.save(chat);
    }

    public void deleteChat(int chatId){
        chatRepository.deleteById(chatId);
    }

    public Optional<Chat> getChat(int chatId){
        return chatRepository.findById(chatId);
    }

    public List<Chat> getAllChats(){ return chatRepository.findAllChats(); }

    public void createOrUpdateUserInvited(UserInvited ui){
        userInvitedRepository.save(ui);
    }

}
