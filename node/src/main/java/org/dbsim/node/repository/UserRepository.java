package org.dbsim.node.repository;
import org.dbsim.node.model.user.User;
public interface UserRepository {
    User findByUsername(String username);

    User save(User user);

    void delete(String username);

}
