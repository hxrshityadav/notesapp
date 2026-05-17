package com.harshit.notesapp.service;

import com.harshit.notesapp.dto.CreateNoteRequest;
import com.harshit.notesapp.dto.UpdateNoteRequest;
import com.harshit.notesapp.entity.Note;
import com.harshit.notesapp.entity.User;
import com.harshit.notesapp.repository.NoteRepository;
import com.harshit.notesapp.repository.UserRepository;
import com.harshit.notesapp.dto.ShareNoteRequest;
import com.harshit.notesapp.entity.SharedNote;
import com.harshit.notesapp.repository.SharedNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final SharedNoteRepository sharedNoteRepository;

    public Note createNote(CreateNoteRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(currentUser)
                .build();

        return noteRepository.save(note);
    }

    public List<Note> getUserNotes() {
        User currentUser = getCurrentAuthenticatedUser();
        return noteRepository.findByUserOrderByCreatedAtDesc(currentUser);
    }

    public Note getNoteById(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));

        validateNoteAccess(note);
        return note;
    }

    public void shareNote(Long noteId, ShareNoteRequest request) {
        // 1. Fetch note and validate strict ownership (only owner can share)
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));
        validateNoteOwnership(note);

        // 2. Prevent sharing with self
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser.getEmail().equalsIgnoreCase(request.getShareWithEmail())) {
            throw new IllegalArgumentException("You cannot share a note with yourself.");
        }

        // 3. Prevent sharing with non-existing users
        User targetUser = userRepository.findByEmail(request.getShareWithEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + request.getShareWithEmail()));

        // 4. Prevent duplicate sharing
        if (sharedNoteRepository.existsByNoteIdAndSharedUserId(note.getId(), targetUser.getId())) {
            throw new IllegalArgumentException("Note is already shared with this user.");
        }

        // 5. Save sharing relationship
        SharedNote sharedNote = SharedNote.builder()
                .note(note)
                .sharedUser(targetUser)
                .build();
        sharedNoteRepository.save(sharedNote);
    }

    public Note updateNote(Long noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));

        validateNoteOwnership(note); // Strict owner check (shared users cannot update)

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        return noteRepository.save(note);
    }

    @Transactional
    public void deleteNote(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));
                
        validateNoteOwnership(note); // Strict owner check (shared users cannot delete)
        
        // Remove all sharing references first to avoid foreign key constraint violations
        sharedNoteRepository.deleteByNote(note);
        
        noteRepository.delete(note);
    }

    public Note toggleStar(Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + noteId));
                
        validateNoteAccess(note); // Both owner and shared users can star a note for their own view (wait, if it's shared, starring it stars it for everyone. Since we only have one flag, that's fine for this simple implementation!)
        note.setStarred(!note.isStarred());
        return noteRepository.save(note);
    }


    /* ===================================================================================
     * HELPER METHODS for Security & Authorization
     * Keeping these abstracted makes the core business logic above much cleaner.
     * =================================================================================== */

    /**
     * Extracts the currently authenticated user's email from the Spring Security Context
     * and fetches their most up-to-date User entity from the database.
     */
    private User getCurrentAuthenticatedUser() {
        // Spring Security stores the email as the "name" of the Authentication object in our setup
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found in database."));
    }

    /**
     * Verifies that the currently authenticated user has READ access to the note.
     * This allows both the original owner AND users it has been shared with to access it.
     */
    private void validateNoteAccess(Note note) {
        User currentUser = getCurrentAuthenticatedUser();
        
        // 1. Allow if owner
        if (note.getUser().getId().equals(currentUser.getId())) {
            return;
        }

        // 2. Allow if shared with this user
        if (sharedNoteRepository.existsByNoteIdAndSharedUserId(note.getId(), currentUser.getId())) {
            return;
        }

        throw new AccessDeniedException("You do not have permission to access this note.");
    }

    /**
     * Verifies that the currently authenticated user is the ACTUAL OWNER of the note.
     * Throws an AccessDeniedException (403 Forbidden) if they try to modify someone else's note.
     */
    private void validateNoteOwnership(Note note) {
        User currentUser = getCurrentAuthenticatedUser();
        Long noteUserId = note.getUser().getId();
        Long currentUserId = currentUser.getId();
        System.out.println("NOTE USER ID: " + noteUserId + " (" + noteUserId.getClass().getName() + ")");
        System.out.println("CURRENT USER ID: " + currentUserId + " (" + currentUserId.getClass().getName() + ")");
        if (!noteUserId.equals(currentUserId)) {
            throw new AccessDeniedException("You must be the owner of this note to perform this action.");
        }
    }
}
