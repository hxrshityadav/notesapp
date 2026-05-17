package com.harshit.notesapp.controller;

import com.harshit.notesapp.dto.CreateNoteRequest;
import com.harshit.notesapp.dto.UpdateNoteRequest;
import com.harshit.notesapp.dto.ShareNoteRequest;
import com.harshit.notesapp.entity.Note;
import com.harshit.notesapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody CreateNoteRequest request) {
        Note createdNote = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getUserNotes();
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request
    ) {
        Note updatedNote = noteService.updateNote(id, request);
        return ResponseEntity.ok(updatedNote);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<java.util.Map<String, String>> shareNote(
            @PathVariable Long id,
            @Valid @RequestBody ShareNoteRequest request
    ) {
        noteService.shareNote(id, request);
        return ResponseEntity.ok(java.util.Map.of("message", "Note shared successfully"));
    }

    @PutMapping("/{id}/star")
    public ResponseEntity<Note> toggleStar(@PathVariable Long id) {
        Note updatedNote = noteService.toggleStar(id);
        return ResponseEntity.ok(updatedNote);
    }
}
