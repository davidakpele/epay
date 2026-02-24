package pesco.example.authentication_service.dtos;

import java.util.List;

import lombok.Data;
import pesco.example.authentication_service.enums.AttemptType;
import pesco.example.authentication_service.models.Users;

@Data
public class UsersFactoryDTO {
    private List<Users> users;
    private AttemptType attemptType;
   
    public UsersFactoryDTO() {
    }

    public UsersFactoryDTO(List<Users> users, AttemptType attemptType) {
        this.users = users;
        this.attemptType = attemptType;
    }

    public List<Users> getUsers() {
        return this.users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

    public AttemptType getAttemptType() {
        return this.attemptType;
    }

    public void setAttemptType(AttemptType attemptType) {
        this.attemptType = attemptType;
    }

}
