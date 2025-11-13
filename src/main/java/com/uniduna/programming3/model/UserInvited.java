package com.uniduna.programming3.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name="user_invited",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chat_id"}))
public class UserInvited {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int userInvitedId;

    @Column(name="user_id")
    private int userId;

    @Column(name="chat_id")
    private int chatId;

    @Column(name="is_accepted")
    private Boolean isAccepted;

    @Column(name = "invitation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date invitationDate;

    // Getters and setters
    @SuppressWarnings("unused")
    public int getId() {
        return userInvitedId;
    }

    @SuppressWarnings("unused")
    public void setId(int id) {
        this.userInvitedId = id;
    }

    @SuppressWarnings("unused")
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    @SuppressWarnings("unused")
    public int getChatId() {
        return chatId;
    }

    public void setIsAccepted(Boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    @SuppressWarnings("unused")
    public Boolean getIsAccepted() {
        return isAccepted;
    }

    @SuppressWarnings("unused")
    public Date getInvitationDate() {
        return invitationDate;
    }

    @SuppressWarnings("unused")
    public void setInvitationDate(Date invitationDate) {
        this.invitationDate = invitationDate;
    }

    @Override
    public String toString() {
        return "UserInvited{" +
                "id=" + userInvitedId +
                ", chatId=" + chatId +
                ", userId=" + userId +
                ", isAccepted=" + isAccepted + '\'' +
                ", invitationDate=" + invitationDate +
                '}';
    }
}