package com.backend.bilanko.services.person;

import com.backend.bilanko.DTO.auth.AuthResponse;
import com.backend.bilanko.DTO.auth.RegisterRequest;
import com.backend.bilanko.models.person.Role;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.repository.UserRepository;
import com.backend.bilanko.services.auth.JWTServices;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SuperAdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTServices jwtService;
    @Value("${SUPER_ADMIN_PASS}")
    private String reallySuperPass;
    private static final int ITERATIONS = 310_000;

    @Value("${SUPER_ADMIN_PASS_TOKEN_CRYPT}")
    private String token;



    private static SecretKey deriveKey(String token, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(token.toCharArray(), salt, ITERATIONS, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private String decrypt(String payload) throws Exception {
        final int SALT_LEN = 16;
        final int IV_LEN = 12;
        final int TAG_BITS = 128;
        byte[] data = Base64.getDecoder().decode(payload);
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.get(salt);
        buf.get(iv);
        byte[] cipherText = new byte[buf.remaining()];
        buf.get(cipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(token, salt), new GCMParameterSpec(TAG_BITS, iv));
        // Lance AEADBadTagException si le token est faux ou les données altérées
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }
    private boolean isReallySuperAdmin(String superAdminPassword) throws Exception {
        return Objects.equals(decrypt(superAdminPassword),reallySuperPass);
    }
    public AuthResponse createAdmin(RegisterRequest request,String superAdminPassword) throws Exception {
        if (!isReallySuperAdmin(superAdminPassword)){
            throw new IllegalArgumentException("Vous n'etes pas superadmin");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        User user = User.builder()
                .name(request.name())
                .subname(request.subname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

}
