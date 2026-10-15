package tanalista.adaptadores.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NomeRequest(@NotBlank @Size(max = 120) String nome) { }
