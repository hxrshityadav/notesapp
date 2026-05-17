package com.harshit.notesapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareNoteRequest {

    @NotBlank(message = "Email is required to share a note")
    @Email(message = "Invalid email format")
    private String shareWithEmail;
}
