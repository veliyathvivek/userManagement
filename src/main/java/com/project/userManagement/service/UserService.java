package com.project.userManagement.service;

import com.project.userManagement.domain.User;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User register(String firstName, String lastName, String username, String email) throws MessagingException;

    List<User> getUsers();

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImg) throws IOException;

    User updateUser(String currentUsername, String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImg) throws IOException;

    void deleteUser(long id);

    void resetPassword(String email);

    User updateProfileImage(String username, MultipartFile img) throws IOException;
}