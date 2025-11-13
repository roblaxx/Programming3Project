package com.uniduna.programming3.controller;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.uniduna.programming3.model.UserInvited;
import com.uniduna.programming3.model.Chat;
import com.uniduna.programming3.model.Users;
import com.uniduna.programming3.services.ServicesRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
// Controller used by Admin interface
public class WebController {

    @Resource
    private ServicesRequest servicesRequest;

    @RequestMapping(value = "/")
    // Redirects to login page
    public String login() {
        return "login";
    }

    @RequestMapping(value = "/logout")
    // Déconnecte de la session et redirige vers la page d'accueil
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @RequestMapping(value = "/addUser")
    // Returns the view to create an User
    public String addUser() {
        return "addUser";
    }

    @RequestMapping(value = "/homepage")
    // Returns to homepage
    public String homepage() {
        return "homepage";
    }

    @PostMapping(value = "/createUserAdmin")
    // Create a User in database
    public String createUserAdmin(String firstName,String lastName,String mail, String password,Boolean isAdmin, Boolean isDisabled) {
        Users user = new Users();
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setMail(mail);

        // Hashing the password
        String hashPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        user.setPassword(hashPassword);

        user.setIsAdmin(isAdmin != null);
        user.setIsDisabled(isDisabled != null);
        servicesRequest.createOrUpdateUser(user);
        return "redirect:/users";
    }

    @PostMapping(value = "/createChatAdmin")
    // Creation of a Chat
    public String createChatAdmin(String mail, String startDate, String validityDate, String title, String description, Boolean isDisabled,String invitedMail, RedirectAttributes redirectAttributes){
        Chat chat = new Chat();

        // Check if the user exists in the database
        Optional<Users> user = servicesRequest.getUserByMail(mail);
        if (user.isPresent()) {

            // Formatting of the date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk:mm",Locale.FRENCH);
            LocalDateTime startingDate = LocalDateTime.parse(startDate, formatter);
            LocalDateTime endingDate = LocalDateTime.parse(validityDate,formatter);

            // Check if the starting and ending dates are coherent
            if (startingDate.isBefore(endingDate) && startingDate.isAfter(LocalDateTime.now())){
                chat.setDate(startingDate);
                chat.setDescription(description);
                chat.setUserId(user.get().getUserId());
                chat.setValidatyDuration(endingDate);
                chat.setTitle(title);

                // The Boolean isDisabled is True if it has a value, False if not (due to the check-button in HTML)
                chat.setIsDisabled(isDisabled != null);

                // Creation of the Chat in the database
                servicesRequest.createOrUpdateChat(chat);

                // Get the id of the created Chat
                int chatId = chat.getChatId();

                // Check if the User has invited other Users
                if (!invitedMail.isEmpty()) {
                    String[] mailingList = invitedMail.split(",");
                    for (String email : mailingList) {
                        UserInvited ui = new UserInvited();
                        ui.setChatId(chatId);

                        // Check if the user exists in the database
                        Optional<Users> userInvited = servicesRequest.getUserByMail(email);
                        if (userInvited.isPresent()) {
                            ui.setUserId(userInvited.get().getUserId());

                            // Setting a not accepted flag (did not have time to finalize the acceptance part)
                            ui.setIsAccepted(false);
                            servicesRequest.createOrUpdateUserInvited(ui);
                        }
                    }
                }
            }
            // Directing you to the global Chats Page
            return "redirect:/chats";
        }
        // Pop-up informing the User that the mail is not in the database
        redirectAttributes.addFlashAttribute("message","Non existing email in database");
        return "redirect:/addChat";
    }

    @PostMapping(value = "/loginAdmin")
    // Login to verify the user
    public String loginAdmin(String mail, String password, HttpSession session, RedirectAttributes redirectAttributes) {
        // Search the user via email
        Optional<Users> userOpt = servicesRequest.getUserByMail(mail);

        if (userOpt.isPresent()) {
            Users user = userOpt.get();

            // Check if the user has admin rights or is not disabled
            if (!user.getIsAdmin() || user.getIsDisabled()) {
                redirectAttributes.addFlashAttribute("message","User does not have the rights");
                return "redirect:/";
            }

            // Verify the hashed password
            BCrypt.Result result = BCrypt.verifyer().verify(
                    password.toCharArray(),
                    user.getPassword()
            );

            if (result.verified) {
                // Create a DTO representing the User without the password
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("firstname", user.getFirstname());
                userInfo.put("lastname", user.getLastname());
                userInfo.put("mail", user.getMail());
                userInfo.put("isAdmin", user.getIsAdmin());
                session.setAttribute("user",userInfo);
                return "homepage";
            }
//             In case of incorrect password
            else {
                redirectAttributes.addFlashAttribute("message","Incorrect password");
                return "redirect:/";
            }
        }
        // If the user does not exist
        redirectAttributes.addFlashAttribute("message","Non existing user");
        return "redirect:/";
    }

    @RequestMapping(value = "/users")
    // Returns the lists of all users and the view
    public String users(Model model) {
        model.addAttribute("users",servicesRequest.getAllUsers());
        return "users";
    }

    @RequestMapping(value = "/users/{id}")
    // Request to modify a User
    public String getUserInfo(Model model,RedirectAttributes redirectAttributes,@PathVariable int id) {

        // Check if the User exists
        Optional<Users> userOpt =  servicesRequest.getUser(id);
        if (userOpt.isPresent()) {
            // Getting the User and passing it to the view
            Users user = userOpt.get();
            model.addAttribute("user",user);

            // Returns the view of editing an User
            return "editUser";
        }

        // Returns an error message if the user does not exists
        redirectAttributes.addFlashAttribute("message", "Non existing User");
        return "redirect:/users";
    }

    @RequestMapping(value = "/users/delete/{id}")
    // Deleting an User
    public String deleteUser(RedirectAttributes redirectAttributes, @PathVariable int id) {

        // Checks if User exists
        Optional<Users> userOpt =  servicesRequest.getUser(id);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();

            // Deleting from database
            servicesRequest.deleteUser(user.getUserId());

            // Pop-up to show that the User has been deleted in database
            redirectAttributes.addFlashAttribute("message", "Deleted User");

            // Redirecting to all Users page
            return "redirect:/users";
        }
        // Error message if an error occurs
        redirectAttributes.addFlashAttribute("message", "Error in deletion");
        return "redirect:/users";
    }

    @PostMapping(value = "/adminUpdateUser")
    // Updating an User
    public String adminUpdateUser(Integer userId,String firstName,String lastName,String mail,Boolean isAdmin, Boolean isDisabled, RedirectAttributes redirectAttributes) {

        // Check if the User exists in database
        Optional<Users> userOpt = servicesRequest.getUser(userId);
        if (userOpt.isPresent()){
            Users user = userOpt.get();
            user.setFirstname(firstName);
            user.setLastname(lastName);
            user.setMail(mail);
            user.setIsAdmin(isAdmin != null);
            user.setIsDisabled(isDisabled != null);

            // Updating the database
            servicesRequest.createOrUpdateUser(user);

            // Redirecting to all Users page
            return "redirect:/users";
        }

        // Error pop-up
        redirectAttributes.addFlashAttribute("message","User not found in database");
        return "redirect:/users";
    }

    @RequestMapping(value = "/addChat")
    // View for Chat creation
    public String addChat() {
        return "addChat";
    }
    @RequestMapping(value = "/chats")
    // View to show all Chats
    public String chats(Model model) {

        // Getting all the Chats in the database
        List<Chat> chats = servicesRequest.getAllChats();

        // Creation of a Map of the DTO
        List<Map<String, Object>> finalChats = new ArrayList<>();

        // Transforming every Chat into the desired DTO
        for (Chat chat : chats) {
            Map<String, Object> chatInfo = chatToChatInfo(chat,false);

            // Add it to the final list
            finalChats.add(chatInfo);
        }

        // Put the DTO as a view argument
        model.addAttribute("chatsInfo",finalChats);
        return "chats";
    }

    @RequestMapping(value = "/chats/{id}")
    // Modify a Chat
    public String getChatInfo(Model model,RedirectAttributes redirectAttributes,@PathVariable int id) {

        // Check if the Chat exists
        Optional<Chat> chatOpt =  servicesRequest.getChat(id);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();

            // Transforming the Chat in DTO
            Map<String,Object> chatInfo = chatToChatInfo(chat,true);

            // Add the DTO as an argument to the view
            model.addAttribute("chat",chatInfo);
            return "editChat";
        }

        // Error pop-up if the Chat does not exist
        redirectAttributes.addFlashAttribute("message", "Non existing chat");

        // Redirecting to all Chats page
        return "redirect:/chats";
    }

    @RequestMapping(value = "/chats/delete/{id}")
    // Deleting a Chat
    public String deleteChat(RedirectAttributes redirectAttributes, @PathVariable int id) {

        // Check if Chat exists in database
        Optional<Chat> chatOpt =  servicesRequest.getChat(id);
        if (chatOpt.isPresent()) {
            Chat chat = chatOpt.get();

            // Deleting the Chat
            servicesRequest.deleteChat(chat.getChatId());

            // Pop-up to show that the deletion took place
            redirectAttributes.addFlashAttribute("message", "Deleted chat");
            return "redirect:/chats";
        }

        // Error pop-up
        redirectAttributes.addFlashAttribute("message", "Error in deletion");
        return "redirect:/chats";
    }

    @PostMapping(value = "/updateChatAdmin")
    // Update a Chat from a Form
    public String updateChatAdmin(Integer chatId, String mail , String startDate, String validityDate, String title, String description, Boolean isDisabled, String invitedMail, @RequestParam Map<String, String> acceptationStatuses, RedirectAttributes redirectAttributes){

        // Checking if the Chat and the User exists
        Optional<Chat> chatFound = servicesRequest.getChat(chatId);
        Optional<Users> user = servicesRequest.getUserByMail(mail);
        if (chatFound.isPresent() && user.isPresent()) {

            // Checking that the ChatId corresponds to the requested Id
            if (chatFound.get().getChatId() == chatId){
                Chat chat = chatFound.get();

                // Formatting the date
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'kk:mm",Locale.FRENCH);
                LocalDateTime startingDate = LocalDateTime.parse(startDate, formatter);
                LocalDateTime endingDate = LocalDateTime.parse(validityDate,formatter);

                // Checking the coherence of the date
                if (startingDate.isBefore(endingDate)){
                    chat.setDate(startingDate);
                    chat.setDescription(description);
                    chat.setUserId(user.get().getUserId());
                    chat.setValidatyDuration(endingDate);
                    chat.setTitle(title);
                    chat.setIsDisabled(isDisabled != null);

                    // Creating the Chat in database
                    servicesRequest.createOrUpdateChat(chat);

                    // Checking if other Users are invited
                    if (!invitedMail.isEmpty()) {

                        // Check all the Users already invited to the Chat
                        Optional<List<Integer>> allInvitedUsersIdOpt =  servicesRequest.getAllInvitedUsersByChat(chatId);

                        // Creating a list with the id of all Users invited
                        List<Integer> newInvitedUsersIdList = new ArrayList<>();
                        String[] mailingList = invitedMail.split(",");

                        // Loop on all existing emails
                        for (String email : mailingList) {
                            UserInvited ui = new UserInvited();
                            ui.setChatId(chatId);

                            // Check if the mail exists in database
                            Optional<Users> userInvitedOpt = servicesRequest.getUserByMail(email);
                            if (userInvitedOpt.isPresent()) {
                                Users userInvited = userInvitedOpt.get();

                                // Check if the invite is new
                                Optional<Boolean> acceptedStateIfAlreadyInvitedOpt = servicesRequest.getAcceptationStateByChatByUser(chatId,userInvited.getUserId());

                                // Check if the invite was already created
                                if (acceptedStateIfAlreadyInvitedOpt.isPresent()){
                                    // Updating the Boolean
                                    ui.setIsAccepted(acceptationStatuses.get(userInvited.getMail()) != null);
                                    ui.setUserId(userInvited.getUserId());

                                    // Updating the UserInvited object
                                    servicesRequest.updateAcceptationStateByChatByUser(ui);
                                }
                                else {
                                    // Creating a non accepted invite
                                    ui.setIsAccepted(false);
                                    ui.setUserId(userInvited.getUserId());

                                    // Create an UserInvited object in database
                                    servicesRequest.createOrUpdateUserInvited(ui);
                                }
                                // Add the id to the list
                                newInvitedUsersIdList.add(ui.getUserId());
                            }
                            else {
                                // Redirecting with the pop-up message
                                redirectAttributes.addFlashAttribute("message","Problems with this mail : " + email);
                                return "redirect:/chat" + chatId;
                            }
                        }
                        // For all already invited Users
                        if (allInvitedUsersIdOpt.isPresent()) {
                            List<Integer> allInvitedUsers = allInvitedUsersIdOpt.get();

                            // For each of them
                            for (int userId : allInvitedUsers) {

                                // If the User was not in the list this time, the invitation is deleted
                                if (!newInvitedUsersIdList.contains(userId)) {
                                    servicesRequest.deleteInvitationByChatByUser(chatId,userId);
                                }
                            }
                        }
                    }
                }
                // Redirecting to Chats page
                return "redirect:/chats";
            }
        }
        // Error pop-up if the mail does not exist in database
        redirectAttributes.addFlashAttribute("message","Non-existant mail");
        return "redirect:/chat/" + chatId;
    }

    // Transforming a Chat into the DTO object wanted
    public Map<String,Object> chatToChatInfo(Chat chat,Boolean areEmailsWanted) {
        // Creation of a Tuple Map
        Map<String, Object> chatInfo = new HashMap<>();

        // Associate the properties that stayed the same
        chatInfo.put("chatId", chat.getChatId());
        chatInfo.put("title", chat.getTitle());
        chatInfo.put("description", chat.getDescription());
        chatInfo.put("isDisabled", chat.getIsDisabled());

        // Operations to make the date readable by the user
        if (!areEmailsWanted) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy kk:mm", Locale.FRENCH);
            String formattedDate = chat.getDate().format(formatter);
            String formattedValidityDate = chat.getValidatyDuration().format(formatter);
            chatInfo.put("date", formattedDate);
            chatInfo.put("validityDate", formattedValidityDate);
        }
        else {
            chatInfo.put("date",chat.getDate());
            chatInfo.put("validityDate",chat.getValidatyDuration());
        }


        // Searching the first and last name of each person
        Optional<Users> chatOwnerOpt = servicesRequest.getUser(chat.getUserId());
        if (chatOwnerOpt.isPresent()) {
            Users chatOwner = chatOwnerOpt.get();
            if (areEmailsWanted) {
                chatInfo.put("owner", chatOwner.getMail());
            }
            else {
                chatInfo.put("owner", chatOwner.getFirstname() + " " + chatOwner.getLastname());
            }
        }

        // Search first / last name or mail of each person invited to a Chat
        Optional<List<Integer>> userInvitedListOpt = servicesRequest.getAllInvitedUsersByChat(chat.getChatId());

        // Look if the Chat has invited Users
        if (userInvitedListOpt.isPresent()) {
            List<Integer> userInvitedList = userInvitedListOpt.get();

            // Initialize the final list of objects (Id,Invitation status)
            List<Map<String,Boolean>> finalList = new ArrayList<>();
            for (Integer userInvitedId : userInvitedList) {

                // Check if the invited User exists
                Optional<Users> userInvitedOpt = servicesRequest.getUser(userInvitedId);
                if (userInvitedOpt.isPresent()) {
                    Users userInvited = userInvitedOpt.get();

                    // Checking the status of the already existing invitation
                    Optional<Boolean> acceptationStateOpt = servicesRequest.getAcceptationStateByChatByUser(chat.getChatId(),userInvited.getUserId());
                    if (acceptationStateOpt.isPresent()) {
                        Boolean acceptationState = acceptationStateOpt.get();
                        Map<String,Boolean> userInfo = new HashMap<>();

                        // Affecting the mail or first/name to the invitation state
                        if (areEmailsWanted) {
                            userInfo.put(userInvited.getMail(),acceptationState);
                        }
                        else {
                            userInfo.put(userInvited.getFirstname() + " " + userInvited.getLastname(),acceptationState);
                        }

                        // Adding the final object to the invited list
                        finalList.add(userInfo);
                    }
                }
            }
            // Adding the list to the DTO
            chatInfo.put("invitedUsers",finalList);
        }
        // Giving the DTO to the view
        return chatInfo;
    }

}