package com.example.catalog;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Book> listBooks(@RequestParam(required = false) String genre) {
        if (genre != null) {
            return repository.findByGenre(genre);
        }
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        Book book = repository.findById(id);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String q) {
        return repository.search(q);
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        return repository.add(book);
    }
}
