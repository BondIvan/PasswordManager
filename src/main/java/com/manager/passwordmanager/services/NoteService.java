package com.manager.passwordmanager.services;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.exceptions.NotFoundException;
import com.manager.passwordmanager.repositories.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<Note> getAllNote() {
        return noteRepository.findAll();
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note with id = " + id + " not found"));
    }

    public void addNewNote(Note note) {
        noteRepository.save(note);
    }

    public Note getNoteByServiceName(String serviceName) {
        return null;
    }

    public void updateNote(Note note) {

    }

}
