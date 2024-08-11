package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.services.NoteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/notes")
public class MainController {

    private final NoteService noteService;

    public MainController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public String getAllNotes(Model model) {
        List<Note> notes = noteService.getAllNote();
        model.addAttribute("notes", notes);

        if (!model.containsAttribute("newNote")) {
            model.addAttribute("newNote", new Note());
        }
        return "index";
    }

    @PostMapping("/add")
    public String addNode(@Valid @ModelAttribute("newNote") Note note, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // Managing form data and error information for redirects
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newNote", bindingResult);
            redirectAttributes.addFlashAttribute("newNote", note);
            return "redirect:/notes";
        }

        noteService.addNewNote(note);
        return "redirect:/notes";
    }


    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable("id") Long id) {
        noteService.deleteNoteById(id);
        return "redirect:/notes";
    }

}
