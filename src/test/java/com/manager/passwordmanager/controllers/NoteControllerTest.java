package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.services.NoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Test
    void getAllNotes_shouldReturnIndexViewWithNotesWhenAttrNewNoteDoesNotExist() throws Exception {

        // given
        Note note1 = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPasswordGoogle"
        );
        Note note2 = new Note(
                2L,
                "Yandex",
                "yandex.ru",
                "yandex@yandex.ru",
                "testPasswordYandex"
        );

        List<Note> notes = List.of(note1, note2);

        // when
        when(noteService.getAllNote()).thenReturn(notes);
        when(noteService.decryptPassword(any(Note.class))).thenReturn("decryptedPassword");

        // then
        mockMvc.perform(get("/notes")
                        .sessionAttr("authenticated", true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("notes"))
                .andExpect(model().attribute("notes", hasSize(2)))
                .andExpect(model().attributeExists("newNote"));

        verify(noteService, times(2)).decryptPassword(any(Note.class));
    }

    @Test
    void getAllNotes_shouldReturnIndexViewWithNotesWhenAttrNewNoteExist() throws Exception {
        // given
        Note note1 = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPasswordGoogle"
        );
        Note note2 = new Note(
                2L,
                "Yandex",
                "yandex.ru",
                "yandex@yandex.ru",
                "testPasswordYandex"
        );

        List<Note> notes = List.of(note1, note2);

        // when
        when(noteService.getAllNote()).thenReturn(notes);
        when(noteService.decryptPassword(any(Note.class))).thenReturn("decryptedPassword");

        // then
        mockMvc.perform(get("/notes")
                        .sessionAttr("authenticated", true)
                        .flashAttr("newNote", new Note())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("notes"))
                .andExpect(model().attribute("notes", hasSize(2)))
                .andExpect(model().attributeExists("newNote")
        );

        verify(noteService, times(2)).decryptPassword(any(Note.class));
    }

    @Test
    void addNote_shouldRedirectToNotesWithErrorsWhenValidationFails() throws Exception {

        // given
        Note note = new Note(
                1L,
                "",
                "",
                "",
                ""
        );

        // when

        // then
        mockMvc.perform(post("/notes/add")
                        .sessionAttr("authenticated", true)
                        .flashAttr("newNote", note)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.newNote"))
                .andExpect(flash().attributeExists("newNote"));

        verify(noteService, never()).addNewNote(note);
    }

    @Test
    void addNote_shouldAddNoteAndRedirectToNotesWhenValidationSucceeds() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "test123-PasswordGoogle"
        );

        // when


        // then
        mockMvc.perform(post("/notes/add")
                    .sessionAttr("authenticated", true)
                    .flashAttr("newNote", note)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes"));

        verify(noteService, times(1)).addNewNote(note);
    }

    @Test
    void deleteNote() throws Exception {

        // given
        Long id = 1L;

        // when


        // then
        mockMvc.perform(post("/notes/delete/{id}", id)
                    .sessionAttr("authenticated", true)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes"));

        verify(noteService).deleteNoteById(id);
    }
}