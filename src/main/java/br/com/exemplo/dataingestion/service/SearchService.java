package br.com.exemplo.dataingestion.service;

import java.util.concurrent.CompletableFuture;

public interface SearchService {
    public CompletableFuture<Boolean> search(String query, String[] accounts);
}
