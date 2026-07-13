package com.example.tracker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reading")
public class ReadingController {

    private final CatalogClient catalogClient;
    private final Map<Long, ReadingProgress> progressStore = new ConcurrentHashMap<>();

    public ReadingController(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    // Starts tracking a book. Validates it exists by calling Server 1.
    @PostMapping("/start/{bookId}")
    public ResponseEntity<?> startReading(@PathVariable Long bookId) {
        BookDto book;
        try {
            book = catalogClient.getBook(bookId);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.badRequest().body("No such book (id=" + bookId + ") in catalog server");
        }
        if (book == null) {
            return ResponseEntity.badRequest().body("No such book (id=" + bookId + ") in catalog server");
        }

        ReadingProgress progress = new ReadingProgress(book.getId(), book.getTitle(), book.getGenre(), book.getPages());
        progressStore.put(bookId, progress);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/progress/{bookId}")
    public ResponseEntity<?> logProgress(@PathVariable Long bookId, @RequestParam int pagesRead) {
        ReadingProgress progress = progressStore.get(bookId);
        if (progress == null) {
            return ResponseEntity.badRequest().body("You haven't started this book yet. Call /reading/start/" + bookId + " first.");
        }
        progress.setPagesRead(pagesRead);
        if (progress.getPercentComplete() >= 100.0) {
            progress.setStatus("FINISHED");
        }
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/finish/{bookId}")
    public ResponseEntity<?> finishBook(@PathVariable Long bookId, @RequestParam int rating) {
        ReadingProgress progress = progressStore.get(bookId);
        if (progress == null) {
            return ResponseEntity.badRequest().body("You haven't started this book yet.");
        }
        progress.setStatus("FINISHED");
        progress.setPagesRead(progress.getTotalPages());
        progress.setRating(rating);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        List<ReadingProgress> all = List.copyOf(progressStore.values());
        long finishedCount = all.stream().filter(p -> "FINISHED".equals(p.getStatus())).count();
        int totalPagesRead = all.stream().mapToInt(ReadingProgress::getPagesRead).sum();
        double avgRating = all.stream()
                .filter(p -> p.getRating() != null)
                .mapToInt(ReadingProgress::getRating)
                .average()
                .orElse(0.0);

        return Map.of(
                "booksTracked", all.size(),
                "booksFinished", finishedCount,
                "totalPagesRead", totalPagesRead,
                "averageRating", avgRating
        );
    }

    // Recommendation logic: look at genres of finished books, ask Server 1 for more in that genre.
    @GetMapping("/recommendations")
    public List<BookDto> getRecommendations() {
        List<String> favoriteGenres = progressStore.values().stream()
                .filter(p -> "FINISHED".equals(p.getStatus()) && p.getRating() != null && p.getRating() >= 4)
                .map(ReadingProgress::getGenre)
                .distinct()
                .collect(Collectors.toList());

        if (favoriteGenres.isEmpty()) {
            return List.of();
        }

        return favoriteGenres.stream()
                .flatMap(genre -> catalogClient.searchByGenre(genre).stream())
                .filter(book -> !progressStore.containsKey(book.getId()))
                .distinct()
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<ReadingProgress> getAll() {
        return List.copyOf(progressStore.values());
    }

    // Lets the UI show the full catalog (via Server 1) without calling it directly, avoiding CORS.
    @GetMapping("/catalog")
    public List<BookDto> getCatalog() {
        return catalogClient.getAllBooks();
    }
}
