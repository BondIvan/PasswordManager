package com.manager.passwordmanager.controllers;

import com.manager.passwordmanager.services.MPService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MPController.class)
class MPControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MPService mpService;

    @Test
    void showPageForCreateMasterPassword_ShouldReturnCreateMPView() throws Exception {

        // given

        // when

        // then
        mockMvc.perform(get("/master-password/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create_master_password")
                );

    }

    @Test
    void canCreateMP() throws Exception {

        // given
        String secret = "MPassword";

        // when

        // then
        mockMvc.perform(post("/master-password/create")
                .param("secret", secret)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/master-password")
                );

        verify(mpService, times(1)).createMP(secret);
    }

    @Test
    void showPageWithMasterPasswordFieldWhenMPExists_ShouldReturnMPView() throws Exception {

        // given

        // when
        when(mpService.mpExists()).thenReturn(true);

        // then
        mockMvc.perform(get("/master-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("master_password")
        );

        verify(mpService, times(1)).mpExists();
    }

    @Test
    void showPageWithMasterPasswordFieldWhenMPDoesNotExists() throws Exception {

        // given

        // when
        when(mpService.mpExists()).thenReturn(false);

        // then
        mockMvc.perform(get("/master-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/master-password/create"));

        verify(mpService, times(1)).mpExists();

    }

    @Test
    void checkMasterPasswordWhenMPIsCorrect() throws Exception {

        // given
        String secret = "MPassword";

        // when
        when(mpService.checkMP(secret)).thenReturn(true);

        // then
        mockMvc.perform(post("/master-password")
                .param("secret", secret)
        )
                .andExpect(request().sessionAttribute("authenticated", true))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes"));

        verify(mpService, times(1)).checkMP(secret);
    }

    @Test
    void checkMasterPasswordWhenMPIsIncorrect() throws Exception {

        // given
        String secret = "MPassword";

        // when
        when(mpService.checkMP(secret)).thenReturn(false);

        // then
        mockMvc.perform(post("/master-password")
                .param("secret", secret)
        )
                .andExpect(flash().attribute("error", "master password is wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/master-password"))
                .andExpect(request().sessionAttributeDoesNotExist("authenticated"));

        verify(mpService, times(1)).checkMP(secret);
    }
}