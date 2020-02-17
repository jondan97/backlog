package gr.university.thesis.service;

import gr.university.thesis.entity.Role;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.RoleEnum;
import gr.university.thesis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
    public List<User> findUsersByRole(String roleStr) {
        return userRepository.findByUserRole(roleStr);
    }

    /**
     * this method allows the creation of a user, and is stored in the repository
     *
     * @param email:    input taken from admin from the controller
     * @param password: input taken from admin from the controller
     */
    public void createUser(String email, String password) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(4);
        String encodedPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(email, encodedPassword);
        Set<Role> roles = new HashSet<>();
        Role role = new Role(RoleEnum.USER.getRepositoryId(), RoleEnum.USER.getRoleName());
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
    }
}
