package com.project.userManagement.service.impl;

import com.project.userManagement.domain.User;
import com.project.userManagement.domain.UserPrincipal;
import com.project.userManagement.eunms.Role;
import com.project.userManagement.exceptions.EmailExistException;
import com.project.userManagement.exceptions.EmailNotFoundException;
import com.project.userManagement.exceptions.UserNotFoundException;
import com.project.userManagement.exceptions.UsernameExistException;
import com.project.userManagement.repo.UserRepository;
import com.project.userManagement.service.EmailService;
import com.project.userManagement.service.LoginAttempt;
import com.project.userManagement.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.project.userManagement.constants.FileConstant.*;
import static com.project.userManagement.constants.UserImplConstant.*;
import static com.project.userManagement.eunms.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LoginAttempt loginAttempt;
    private final EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttempt loginAttempt, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttempt = loginAttempt;
        this.emailService = emailService;
    }



    @Override
    public User register(String firstName, String lastName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {
        validateNewUsernameAndEmail(username, email);
        User user = new User();
        user.setUserId(generateUserId());
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(firstName, username, password, email);
//        LOGGER.info("New user password: {}", password);
        return user;
    }

    @Override
    public List<User> getUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImg) throws IOException {
        validateNewUsernameAndEmail(username, email);
        User user = new User();
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(encodePassword(password));
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(getRoleEnum(role).name());
        user.setAuthorities(getRoleEnum(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
//        LOGGER.info("New user password: {}", password);
        saveProfileImage(user, profileImg);
        emailService.sendNewPasswordEmail(firstName, username, password, email);
        return user;
    }

    private void saveProfileImage(User user, MultipartFile profileImg) throws IOException {
        if (profileImg != null) {
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));
            Files.copy(profileImg.getInputStream(),
                    userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            userRepository.save(user);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImg.getOriginalFilename());
        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnum(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    @Override
    public User updateUser(String currentUsername, String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile profileImg) throws IOException, NullPointerException {
        User currentUser = findUserByUsername(currentUsername);
        String password = generatePassword();
        if (currentUser != null) {
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setUsername(username);
            currentUser.setEmail(email);
            currentUser.setActive(true);
            currentUser.setNotLocked(true);
            currentUser.setRole(getRoleEnum(role).name());
            currentUser.setAuthorities(getRoleEnum(role).getAuthorities());
            userRepository.save(currentUser);
        }
        saveProfileImage(currentUser, profileImg);
        return currentUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(NO_USER_FOUND_BY_EMAIL + email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        if(!user.isNotLocked()){
            user.setNotLocked(true);
        }
        userRepository.save(user);
        emailService.sendNewPasswordEmail(user.getFirstName(), user.getUsername(), password, user.getEmail());
    }

    @Override
    public User updateProfileImage(String username, MultipartFile img) throws IOException {
        User user = userRepository.findUserByUsername(username);
        saveProfileImage(user, img);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        if (user == null) {
            LOGGER.error(NO_USER_FOUND_BY_USERNAME + "{}", username);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + username);
        } else {
            validateUserLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info(FOUND_USER_BY_USERNAME + "{}", username);
            return userPrincipal;
        }
    }

    private User validateNewUsernameAndEmail(String newUsername, String newEmail) throws UserNotFoundException, UsernameExistException, EmailExistException {
        User existUserSameUsername = findUserByUsername(newUsername);
        User existUserSameEmail = findUserByEmail(newEmail);
        if (StringUtils.isNotBlank(newUsername)) {
            if (existUserSameUsername != null) throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            if (existUserSameEmail != null && !existUserSameEmail.getUsername().equals(newUsername)) throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            return existUserSameUsername;
        }
        if (existUserSameUsername == null) throw new UserNotFoundException(NO_USER_FOUND_BY_USERNAME);
        if (existUserSameEmail != null) throw new EmailExistException(EMAIL_ALREADY_EXISTS);
        return null;
    }

    private void validateUserLoginAttempt(User user) {
        if (user.isNotLocked()) {
            if (loginAttempt.hasExceededAttempts(user.getUsername())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttempt.evictUserFromLoginAttemptCache(user.getUsername());
//            try {
//                throw new AccountLockedException();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
        }
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + username).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }
}