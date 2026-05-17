package com.harshit.notesapp.repository;

import com.harshit.notesapp.entity.SharedNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedNoteRepository extends JpaRepository<SharedNote, Long> {

    // Efficiently checks if a sharing relationship already exists without fetching data
    boolean existsByNoteIdAndSharedUserId(Long noteId, Long sharedUserId);

    // Retrieves all records shared WITH a specific user, sorted by most recently shared first
    List<SharedNote> findBySharedUserIdOrderByCreatedAtDesc(Long sharedUserId);

    // Used to retrieve a specific share record (e.g., when the owner wants to revoke/delete access)
    Optional<SharedNote> findByNoteIdAndSharedUserId(Long noteId, Long sharedUserId);

    // Deletes all sharing records when a note is deleted
    void deleteByNote(com.harshit.notesapp.entity.Note note);
}
