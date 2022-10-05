package dev.graciano.cloudbatch.domain;

import java.io.Serializable;

public record Rental(long id, String title, String isbn, String user) implements Serializable {
}
