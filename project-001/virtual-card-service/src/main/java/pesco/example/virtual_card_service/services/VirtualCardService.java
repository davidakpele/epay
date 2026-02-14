package pesco.example.virtual_card_service.services;

import org.springframework.stereotype.Service;
import pesco.example.virtual_card_service.clients.UserServiceClient;
import pesco.example.virtual_card_service.repository.VirtualCardRepository;

@Service
public class VirtualCardService {

    
    private final VirtualCardRepository virtualCardRepository;
    private final UserServiceClient userServiceClient;

    public VirtualCardService(VirtualCardRepository virtualCardRepository, UserServiceClient userServiceClient) {
        this.virtualCardRepository = virtualCardRepository;
        this.userServiceClient = userServiceClient;
    }

    
}
