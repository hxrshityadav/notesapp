package com.harshit.notesapp.repository;

import com.harshit.notesapp.entity.Note;
import com.harshit.notesapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    // Retrieves all notes owned by a specific user, sorted by newest first
    List<Note> findByUserOrderByCreatedAtDesc(User user);
    
    // Alternative: Find by just the User ID without needing the full User entity object
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);
}
