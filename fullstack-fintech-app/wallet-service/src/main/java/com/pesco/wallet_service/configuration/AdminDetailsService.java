package com.pesco.wallet_service.configuration;

import com.pesco.wallet_service.client.AdminServiceClient;
import com.pesco.wallet_service.dtos.AdminDTO;
import com.pesco.wallet_service.exceptions.UserClientNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service("adminDetailsService")
public class AdminDetailsService implements UserDetailsService {

    private final AdminServiceClient adminServiceClient;

    public AdminDetailsService(AdminServiceClient adminServiceClient) {
        this.adminServiceClient = adminServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            AdminDTO admin = adminServiceClient.findAdminByUsername(username, "");

            if (admin == null) {
                throw new UsernameNotFoundException("Admin not found: " + username);
            }

            List<SimpleGrantedAuthority> authorities = admin.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(
                            role.startsWith("ROLE_") ? role : "ROLE_" + role))
                    .toList();

            return User.builder()
                    .username(admin.getUsername())
                    .password("")
                    .authorities(authorities)
                    .build();

        } catch (UserClientNotFoundException e) {
            throw new UsernameNotFoundException("Admin not found: " + username);
        }
    }
}
