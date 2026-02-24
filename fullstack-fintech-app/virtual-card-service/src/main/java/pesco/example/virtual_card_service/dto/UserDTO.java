package pesco.example.virtual_card_service.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private Long id;
    private String email;
    private String username;
    private boolean enabled;
    private List<UserRecordDTO> records;

    public UserDTO() {
    }

    public UserDTO(Long id, String email, String username, boolean enabled, List<UserRecordDTO> records) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.enabled = enabled;
        this.records = records;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<UserRecordDTO> getRecords() {
        return this.records;
    }

    public void setRecords(List<UserRecordDTO> records) {
        this.records = records;
    }

}
