package tanalista.adaptadores.http;

public record Problem(String type, String title, int status, String detail, String instance) { }
