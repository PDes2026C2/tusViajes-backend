package ar.edu.unq.tusViajes.validator;

import org.springframework.stereotype.Component;

import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.repository.UserRepository;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A user already exists with email " + email);
        }
    }
}
