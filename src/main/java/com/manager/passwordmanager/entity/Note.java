package com.manager.passwordmanager.entity;

import com.manager.passwordmanager.validation.Password;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "Note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name")
    @NotBlank(message = "The field must not be empty")
    private String serviceName;

    @Column(name = "service_url")
    @NotBlank(message = "The field must not be empty")
    private String url;

    @Column(name = "service_login")
    @NotBlank(message = "The field must not be empty")
    private String login;

    @Column(name = "hash_password")
    @Password
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Override
    public String toString() {
        return "Note [id = " + id + ", " +
                "serviceName = " + serviceName + ", " +
                "url = " + url + ", " +
                "password = "+password + "]";
    }
}
