package com.harshit.notesapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequest {

    @NotBlank(message = "Title is required")
    private String title;

    // Content is typically allowed to be empty in Note apps, 
    // but if it's strictly required, you can add @NotBlank here as well.
    private String content;
}
