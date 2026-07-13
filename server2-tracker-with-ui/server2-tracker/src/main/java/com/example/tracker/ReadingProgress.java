package com.example.tracker;

public class ReadingProgress {
    private Long bookId;
    private String title;   // cached from catalog server
    private String genre;   // cached from catalog server
    private int totalPages; // cached from catalog server
    private int pagesRead;
    private String status;  // READING, FINISHED
    private Integer rating; // 1-5, set on finish

    public ReadingProgress() {
    }

    public ReadingProgress(Long bookId, String title, String genre, int totalPages) {
        this.bookId = bookId;
        this.title = title;
        this.genre = genre;
        this.totalPages = totalPages;
        this.pagesRead = 0;
        this.status = "READING";
    }

    public double getPercentComplete() {
        if (totalPages == 0) return 0;
        return Math.min(100.0, (pagesRead * 100.0) / totalPages);
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPagesRead() {
        return pagesRead;
    }

    public void setPagesRead(int pagesRead) {
        this.pagesRead = pagesRead;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
