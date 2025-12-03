package org.example.backend.foodpick.domain.user.repository;

import java.util.List;

public interface UserQueryRepository {
    long countAllUsers();                // totalUsers
    List<Long> findAllUserData();        // allUserData
}