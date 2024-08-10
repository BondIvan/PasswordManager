package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.services.NoteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MainController {

    private final NoteService noteService;

    public MainController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public String getAllNotes(Model model) {
        List<Note> notes = noteService.getAllNote();
        model.addAttribute("notes", notes);
        model.addAttribute("newNote", new Note());
        return "index";
    }

    @PostMapping("/add")
    public String addNode(@ModelAttribute("newNote") Note note) {
        noteService.addNewNote(note);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable("id") Long id) {
        noteService.deleteNoteById(id);
        return "redirect:/";
    }

}
