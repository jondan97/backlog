package gr.university.thesis.service;

import gr.university.thesis.Exceptions.UserAlreadyExistsException;
import gr.university.thesis.Exceptions.UserHasEmptyEmailException;
import gr.university.thesis.entity.Role;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.RoleEnum;
import gr.university.thesis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * this class helps with the management of users, fetches users from the repository, deletes or creates them
 */
@Service
public class UserService {

    UserRepository userRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userRepository:        repository that has access to all the users of the system
     * @param bCryptPasswordEncoder: the encoder used when updating a user password or when creating a new user
     */
    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * this method allows an admin to fetch all users with a certain role from the repo
     *
     * @param roleStr: String role that the user(admin) is looking for
     * @return : returns the list of all the users with that certain role
     */
    public Set<User> findUsersByRole(String roleStr) {
        return userRepository.findByUserRole(roleStr);
    }

    /**
     * this method allows an admin to fetch all users from the repository
     *
     * @return : returns the list of all the users
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * this method allows the creation of a user, and is stored in the repository
     *
     * @param email:     email of the new user
     * @param password:  password that the new user is required to login with
     * @param firstName: first name of the user
     * @param lastName:  last name of the user
     * @param roleName   : the role that the user will have (admin/user etc.)
     * @throws UserAlreadyExistsException : if the new email the user tried to update exists, then throw this exception
     * @throws UserHasEmptyEmailException : if the user has no email
     */
    public void createUser(String email, String password, String firstName, String lastName, String roleName)
            throws UserAlreadyExistsException, UserHasEmptyEmailException {
        if (email.isEmpty()) {
            throw new UserHasEmptyEmailException("User cannot be created without an email.");
        }
        email = email.trim();
        if (!userRepository.findFirstByEmail(email).isPresent()) {
            String encodedPassword = bCryptPasswordEncoder.encode(password);
            if (firstName.isEmpty()) {
                firstName = "No first name";
            } else {
                firstName.trim();
            }
            if (lastName.isEmpty()) {
                lastName = "No last name";
            } else {
                lastName.trim();
            }
            User user = new User(email, encodedPassword, firstName, lastName);
            List<Role> roles = new ArrayList<>();
            Role role = new Role((long) RoleEnum.valueOf(roleName).getRepositoryId(), RoleEnum.valueOf(roleName).getName());
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);
        } else {
            throw new UserAlreadyExistsException("E-mail '" + email + "' already exists.");
        }
    }

    /**
     * this method updates a user in the repository (cannot update ID)
     *
     * @param userId:    the user id that is needed in order for the user to be found in the repository
     * @param email:     the email that the admin has possibly updated
     * @param password   : the password that the admin has possibly updated
     * @param firstName: the first name that the admin has possibly updated
     * @param lastName:  the last name that the admin has possibly updated
     * @param userRole:  the role that the admin has possibly changed
     * @throws UserAlreadyExistsException : if the new email the user tried to update exists, then throw this exception
     * @throws UserHasEmptyEmailException : if the user has no email
     */
    public void updateUser(long userId, String email, String password, String firstName, String lastName, String userRole)
            throws UserAlreadyExistsException, UserHasEmptyEmailException {
        if (email.isEmpty()) {
            throw new UserHasEmptyEmailException("User cannot be updated to have no email.");
        }
        Optional<User> userOptional = userRepository.findById(userId);
        email = email.trim();
        Optional<User> userWithThatEmailOptional = userRepository.findFirstByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            //if the user has input a new email and that is already existent in the repository, throw an exception
            if (userWithThatEmailOptional.isPresent()) {
                User userWithThatEmail = userWithThatEmailOptional.get();
                if (!userWithThatEmail.getEmail().equals(user.getEmail())) {
                    throw new UserAlreadyExistsException("User with email '" + userWithThatEmail.getEmail() + "' already exists.");
                }
            }
            user.setEmail(email);
            if (!password.isEmpty()) {
                String encodedPassword = bCryptPasswordEncoder.encode(password);
                user.setPassword(encodedPassword);
            }
            if (!userRole.equals("master_admin")) {
                Role newRole = new Role((long) RoleEnum.valueOf(userRole).getRepositoryId(), RoleEnum.valueOf(userRole).getName());
                user.getRoles().clear();
                user.getRoles().add(newRole);
            }
            if (firstName.isEmpty()) {
                firstName = "No first name";
            } else {
                firstName = firstName.trim();
            }
            user.setFirstName(firstName);
            if (lastName.isEmpty()) {
                lastName = "No last name";
            } else {
                lastName = lastName.trim();
            }
            user.setLastName(lastName);
            userRepository.save(user);
        }
    }

    /**
     * this method deletes a user from the repository
     *
     * @param userId: the user id that is needed in order for the user to be found in the repository and to be deleted
     */
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }
}
