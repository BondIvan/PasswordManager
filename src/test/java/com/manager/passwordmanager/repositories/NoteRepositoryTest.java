package com.manager.passwordmanager.repositories;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NoteRepositoryTest {

    @Autowired
    private NoteRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    // Custom methods from noteRepository

}