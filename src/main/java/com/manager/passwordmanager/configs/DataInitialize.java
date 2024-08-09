package com.manager.passwordmanager.configs;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.repositories.NoteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitialize {

    private final NoteRepository noteRepository;

    public DataInitialize(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @PostConstruct
    public void init() {

        Note note1 = new Note();
        note1.setServiceName("Google");
        note1.setUrl("Google.com");
        note1.setLogin("Google@gmail.com");
        note1.setPassword("hash-google-password");

        Note note2 = new Note();
        note2.setServiceName("Yandex");
        note2.setUrl("Yandex.ru");
        note2.setLogin("Yandex@yandex.ru");
        note2.setPassword("hash-yandex-password");

        Iterable<Note> notes = List.of(note1, note2);

        noteRepository.saveAll(notes);
    }

}
