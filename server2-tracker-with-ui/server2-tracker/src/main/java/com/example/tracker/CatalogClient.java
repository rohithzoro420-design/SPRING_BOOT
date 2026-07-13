package com.example.tracker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * This class is the bridge between Server 2 and Server 1.
 * Every call here goes out over HTTP to the Catalog Server.
 */
@Component
public class CatalogClient {

    private final RestTemplate restTemplate;

    @Value("${catalog.server.url}")
    private String catalogBaseUrl;

    public CatalogClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BookDto getBook(Long bookId) {
        String url = catalogBaseUrl + "/books/" + bookId;
        return restTemplate.getForObject(url, BookDto.class);
    }

    public List<BookDto> searchByGenre(String genre) {
        String url = catalogBaseUrl + "/books?genre=" + genre;
        BookDto[] result = restTemplate.getForObject(url, BookDto[].class);
        return result != null ? Arrays.asList(result) : List.of();
    }

    public List<BookDto> getAllBooks() {
        String url = catalogBaseUrl + "/books";
        BookDto[] result = restTemplate.getForObject(url, BookDto[].class);
        return result != null ? Arrays.asList(result) : List.of();
    }
}
