package com.manager.passwordmanager.entity;

import jakarta.persistence.*;
import lombok.*;


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

    @Column(name = "service")
    private String serviceName;

    @Column(name = "service_url")
    private String url;

    @Column(name = "hash_password")
    private String password;

    @Override
    public String toString() {
        return "Note [id = " + id + ", " +
                "serviceName = " + serviceName + ", " +
                "url = " + url + ", " +
                "password = "+password + "]";
    }
}
