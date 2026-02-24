package pesco.example.withdraw_service.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController {

    @RequestMapping(value = "/access-denied", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public ResponseEntity<String> accessDenied() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ACCESS DENIED.");
    }
}
