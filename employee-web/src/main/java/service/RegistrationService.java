package service;

import com.example.employeeweb.model.AppUser;
import com.example.employeeweb.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository appUserRepository,
                               PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String rawPassword) {
        String normalizedUsername = username.trim();

        if (appUserRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập này đã được sử dụng");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(normalizedUsername);
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        appUser.setRole("USER");

        appUserRepository.save(appUser);
    }
}