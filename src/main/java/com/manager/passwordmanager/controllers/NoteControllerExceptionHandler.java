package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.exceptions.DuplicateNoteException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class NoteControllerExceptionHandler {

    @ExceptionHandler(DuplicateNoteException.class)
    public String handleDuplicateNote(DuplicateNoteException duplicateNoteException, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("duplicateException", duplicateNoteException.getMessage());
        redirectAttributes.addFlashAttribute("newNote", new Note());

        return "redirect:/notes";
    }

}
