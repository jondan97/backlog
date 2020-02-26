package gr.university.thesis.service;

import gr.university.thesis.entity.Role;
import gr.university.thesis.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * service that handles everything relating to the roles stored in the repository
 */
@Service
public class RoleService {

    RoleRepository roleRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param roleRepository: repository that has access to all the roles of the system
     */
    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}
