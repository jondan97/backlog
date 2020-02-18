package gr.university.thesis.service;

import gr.university.thesis.entity.Role;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.RoleEnum;
import gr.university.thesis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * this class helps with the management of users, fetches users from the repository, deletes or creates them
 */
@Service
public class UserService {

    UserRepository userRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userRepository: repository that has access to all the users of the system
     */
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * this method allows an admin to fetch all users with a certain role from the repo
     *
     * @param roleStr: String role that the user(admin) is looking for
     * @return: returns the list of all the users with that certain role
     */
    public Set<User> findUsersByRole(String roleStr) {
        return userRepository.findByUserRole(roleStr);
    }

    /**
     * this method allows the creation of a user, and is stored in the repository
     *
     * @param email:     email of the new user
     * @param password:  password that the new user is required to login with
     * @param firstName: first name of the user
     * @param lastName:  last name of the user
     */
    public void createUser(String email, String password, String firstName, String lastName) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(4);
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(email, encodedPassword, firstName, lastName);
        Set<Role> roles = new HashSet<>();
        Role role = new Role(RoleEnum.USER.getRepositoryId(), RoleEnum.USER.getRoleName());
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
    }

    /**
     * this method updates a user in the repository (cannot update ID)
     *
     * @param userId:    the user id that is needed in order for the user to be found in the repository
     * @param email:     the email that the admin has possibly updated
     * @param firstName: the first name that the admin has possibly updated
     * @param lastName:  the last name that the admin has possibly updated
     */
    public void updateUser(long userId, String email, String firstName, String lastName) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEmail(email);
            user.setFirstName(firstName);
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
