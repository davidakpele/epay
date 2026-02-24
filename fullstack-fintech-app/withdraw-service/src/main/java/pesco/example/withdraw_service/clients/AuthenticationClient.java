package pesco.example.withdraw_service.clients;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import pesco.example.withdraw_service.dtos.UserDTO;
import reactor.core.publisher.Mono;

@HttpExchange
public interface AuthenticationClient {

    @GetExchange("/user/{id}")
    Mono<ResponseEntity<?>> findById(@PathVariable Long id);

    @GetExchange("/user/username/{username}")
    Mono<ResponseEntity<UserDTO>> findByUsername(String username);

}
