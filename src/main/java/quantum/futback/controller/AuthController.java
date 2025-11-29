package quantum.futback.controller;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import quantum.futback.config.security.JwtToken.JwtTokenProvider;
import quantum.futback.config.security.JwtToken.UserPrincipal;
import quantum.futback.entity.DTO.JwtResponse;
import quantum.futback.entity.DTO.LoginRequest;
import quantum.futback.entity.DTO.RefreshTokenRequest;
import quantum.futback.entity.RefreshToken;
import quantum.futback.entity.User;
import quantum.futback.services.UserService;

import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOpt = userService.findById(userPrincipal.getId());

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Usuario no encontrado (debería ser imposible aquí)
        }

        User user = userOpt.get();

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateAndSaveRefreshToken(user);

        // 4. Devolver la respuesta al cliente
        JwtResponse jwtResponse = new JwtResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole().getName()
        );

        return ResponseEntity.ok(jwtResponse);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getToken();

        // Buscar y validar el refresh token en la BD
        Optional<RefreshToken> refreshTokenOpt = tokenProvider.findByRefreshToken(requestRefreshToken);

        if (refreshTokenOpt.isPresent()) {
            RefreshToken refreshToken = refreshTokenOpt.get();
            User user = refreshToken.getUser();

            // 1. Crear un objeto Authentication para el usuario
            Authentication authentication = userService.getAuthenticationFromUser(user);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 2. Generar nuevos tokens
            String newAccessToken = tokenProvider.generateAccessToken(authentication);
            String newRefreshToken = tokenProvider.generateAndSaveRefreshToken(user); // Genera un nuevo token y elimina el viejo

            // 3. Devolver la respuesta
            JwtResponse jwtResponse = new JwtResponse(
                    newAccessToken,
                    newRefreshToken,
                    user.getId(),
                    user.getEmail(),
                    user.getRole().getName()
            );

            return ResponseEntity.ok(jwtResponse);

        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido o expirado.");
        }
    }


    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logoutUser(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        Optional<RefreshToken> refreshTokenOpt = tokenProvider.findByRefreshToken(refreshTokenRequest.getToken());

        if (refreshTokenOpt.isPresent()) {
            tokenProvider.deleteRefreshToken(refreshTokenOpt.get());
            return ResponseEntity.ok("Logout exitoso. Token invalidado.");
        } else {
            return ResponseEntity.badRequest().body("Token ya invalidado o no encontrado.");
        }
    }
}