package com.manager.passwordmanager.repositories;

import com.manager.passwordmanager.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {

}
