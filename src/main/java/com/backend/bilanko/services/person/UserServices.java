package com.backend.bilanko.services.person;

import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;

public interface UserServices {
    public User findUserByEmail(String email);
    public boolean confirmRoleByEmail(Role role,String email);
}
