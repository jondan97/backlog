package gr.university.thesis.service;


import gr.university.thesis.entity.Role;
import gr.university.thesis.entity.SessionUser;
import gr.university.thesis.entity.User;
import gr.university.thesis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * Class that manages current user details loading
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepository userRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userRepository: repository that has access to all the users of the system
     */
    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * searches for user in the repository
     *
     * @param email takes as input an email, which is unique to every user
     * @return returns a User if he is found in the repository
     * @throws UsernameNotFoundException throws this exception if user is not found in the repository
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //these attributes are required by the Spring User Details class
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        //Search for the user within the repository, and if the user doesn't exist, throw an exception
        User repoUser =
                userRepository.findFirstByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        //Map the authority list with the spring security list
        List grantList = new ArrayList();
        for (Role role : repoUser.getRoles()) {
            // ROLE:USER or ROLE:ADMIN or BOTH
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.getRole());
            grantList.add(grantedAuthority);
        }
        SessionUser user = new SessionUser(repoUser.getId(), repoUser.getEmail(), repoUser.getPassword(), enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, grantList);
        return user;
    }
}
