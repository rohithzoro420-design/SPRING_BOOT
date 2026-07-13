package com.example.catalog;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BookRepository {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong();

    public BookRepository() {
        seed();
    }

    private void seed() {
        add(new Book(null, "Dune", "Frank Herbert", "Sci-Fi", 412));
        add(new Book(null, "The Hobbit", "J.R.R. Tolkien", "Fantasy", 310));
        add(new Book(null, "Atomic Habits", "James Clear", "Self-Help", 320));
        add(new Book(null, "Project Hail Mary", "Andy Weir", "Sci-Fi", 476));
        add(new Book(null, "Sapiens", "Yuval Noah Harari", "Non-Fiction", 443));
    }

    public List<Book> findAll() {
        return List.copyOf(books.values());
    }

    public Book findById(Long id) {
        return books.get(id);
    }

    public List<Book> search(String query) {
        String q = query.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q)
                        || b.getAuthor().toLowerCase().contains(q)
                        || b.getGenre().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Book> findByGenre(String genre) {
        return books.values().stream()
                .filter(b -> b.getGenre().equalsIgnoreCase(genre))
                .collect(Collectors.toList());
    }

    public Book add(Book book) {
        long id = idCounter.incrementAndGet();
        book.setId(id);
        books.put(id, book);
        return book;
    }
}
