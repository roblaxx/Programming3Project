package com.uniduna.programming3.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_id")
    private int chatId;

    @Column(name="user_id")
    private int userId;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;

    @Column(name="date")
    private LocalDateTime date;

    @Column(name="validaty_duration")
    private LocalDateTime validatyDuration;

    @Column(name="is_disabled")
    private Boolean isDisabled;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }
    public int getChatId() {
        return chatId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public LocalDateTime getDate() {
        return date;
    }

    public void setValidatyDuration(LocalDateTime validatyDuration) {
        this.validatyDuration = validatyDuration;
    }
    public LocalDateTime getValidatyDuration() {
        return validatyDuration;
    }

    public void setIsDisabled(Boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public Boolean getIsDisabled() {
        return isDisabled;
    }
}
