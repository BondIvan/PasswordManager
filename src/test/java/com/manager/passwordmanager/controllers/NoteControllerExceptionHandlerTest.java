package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.exceptions.DuplicateNoteException;
import com.manager.passwordmanager.services.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@WebMvcTest(NoteController.class)
class NoteControllerExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Test
    void handleDuplicateNoteException() throws Exception {

        // given
        String exceptionMessage = "Note with the same service name and login already exists";
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "test123_PasswordGoogle"
        );

        // when
        doThrow(new DuplicateNoteException(exceptionMessage)).when(noteService).addNewNote(note);

        // then
        mockMvc.perform(post("/notes/add")
                        .sessionAttr("authenticated", true)
                        .flashAttr("newNote", note)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes"))
                .andExpect(flash().attribute("duplicateException", exceptionMessage))
                .andExpect(flash().attributeExists("newNote")
                );

    }
}