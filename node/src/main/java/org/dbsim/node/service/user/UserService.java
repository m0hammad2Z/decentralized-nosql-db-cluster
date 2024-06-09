package org.dbsim.node.service.user;

import org.dbsim.node.dto.user.LoginDTO;
import org.dbsim.node.dto.user.UserDTO;
import org.dbsim.node.enums.DBEventType;
import org.dbsim.node.exception.DBOperationException;
import org.dbsim.node.model.node.MainNode;
import org.dbsim.node.model.user.User;
import org.dbsim.node.repository.UserRepository;
import org.dbsim.node.service.node.service.DBGossipService;
import org.dbsim.node.util.GlobalVar;
import org.dbsim.node.util.validation.Validator;
import org.dbsim.node.util.security.JwtUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private DBGossipService dbGossipService;
    private MainNode mainNode;
    private ModelMapper modelMapper;
    private JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, DBGossipService dbGossipService, MainNode mainNode, ModelMapper modelMapper, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.dbGossipService = dbGossipService;
        this.mainNode = mainNode;
        this.modelMapper = modelMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostConstruct
    public void init() {
        loadAdmin();
    }

    private void loadAdmin() {
        User admin = new User("admin", bCryptPasswordEncoder.encode("admin"), "ADMIN");
        try {
            userRepository.save(admin);
            System.out.println("Admin created");
        } catch (DBOperationException e) {
            System.out.println("Admin already exists");
        }
    }


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(s);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities
        );
    }

    public void delete(String username) {
        Validator.isNotNull(username, "Username must not be null", true);
        userRepository.delete(username);

        dbGossipService.addEvent(DBEventType.USER_DELETED, username);
    }

    public UserDTO save(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);

        Validator.isNotNull(user, "User must not be null", true);
        Validator.isValidString(user.getUsername(), "Username must not be null or empty", true);
        Validator.isValidString(user.getPassword(), "Password must not be null or empty", true);
        Validator.isValidString(user.getRole(), "Role must not be null or empty", true);

        userRepository.save(user);

        dbGossipService.addEvent(DBEventType.USER_CREATED, user);

        return modelMapper.map(user, UserDTO.class);
    }


    public Map<String, String> login(LoginDTO loginDTO) {
        Validator.isNotNull(loginDTO, "LoginDTO must not be null", true);
        Validator.isValidString(loginDTO.getUsername(), "Username must not be null or empty", true);
        Validator.isValidString(loginDTO.getPassword(), "Password must not be null or empty", true);
        Validator.isValidString(loginDTO.getAdminUsername(), "Admin username must not be null or empty", true);
        Validator.isValidString(loginDTO.getAdminPassword(), "Admin password must not be null or empty", true);

        User existingAdmin = userRepository.findByUsername(loginDTO.getAdminUsername());

        if(!bCryptPasswordEncoder.matches(loginDTO.getAdminPassword(), existingAdmin.getPassword())) {
            throw new IllegalArgumentException("Invalid admin password");
        }

        if(!existingAdmin.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("Only admin can login as another user");
        }

        User existingUser = userRepository.findByUsername(loginDTO.getUsername());

        if(bCryptPasswordEncoder.matches(loginDTO.getPassword(), existingUser.getPassword())) {
            String token = jwtUtil.generateToken(existingUser.getUsername(), existingUser.getRole());
            return Map.of("token", token,
                    "username", existingUser.getUsername(),
                    "role", existingUser.getRole(),
                    "nodeId", String.valueOf(mainNode.getId()),
                    "nodeUrl", "http://" + GlobalVar.HOSTNAME + ":" + GlobalVar.PORT);
        } else {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }
}
