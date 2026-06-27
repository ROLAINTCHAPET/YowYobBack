package com.yowyob.search.infrastructure.adapter.in.web;

import com.yowyob.search.repository.ProductSearchRepository;
import com.yowyob.search.service.EmbeddingClient;
import com.yowyob.search.document.ProductDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Migration controller adapter — one-time utility endpoint,
 * placed in the infrastructure layer as it directly uses Elasticsearch internals.
 */
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Migration", description = "One-time data migration utilities")
public class MigrationControllerAdapter {

    private final ProductSearchRepository searchRepository;
    private final EmbeddingClient embeddingClient;

    @PostMapping("/embeddings")
    @Operation(summary = "Migrate embeddings", description = "Add semantic text vectors to existing products (run once after deployment)")
    public Mono<String> migrateEmbeddings() {
        log.info("Starting embeddings migration process for existing products...");

        return searchRepository.findAll()
                .filter(product -> product.getTextVector() == null || product.getTextVector().length == 0)
                .flatMap(product -> {
                    String textToEmbed = String.format("%s %s %s",
                            product.getTitle() != null ? product.getTitle() : "",
                            product.getCategory() != null ? product.getCategory() : "",
                            product.getDescription() != null ? product.getDescription() : ""
                    ).trim();

                    if (!textToEmbed.isEmpty()) {
                        return embeddingClient.generateEmbedding(textToEmbed)
                                .flatMap(vectorList -> {
                                    if (vectorList != null && !vectorList.isEmpty()) {
                                        float[] vectorArray = new float[vectorList.size()];
                                        for (int i = 0; i < vectorList.size(); i++) {
                                            vectorArray[i] = vectorList.get(i);
                                        }
                                        product.setTextVector(vectorArray);
                                        return searchRepository.save(product)
                                                .doOnSuccess(p -> log.info("Migrated product: {}", p.getId()));
                                    }
                                    return Mono.just(product);
                                })
                                .onErrorResume(e -> {
                                    log.error("Failed to migrate product {}: {}", product.getId(), e.getMessage());
                                    return Mono.just(product);
                                });
                    }
                    return Mono.just(product);
                }, 5)
                .count()
                .map(count -> "Migration process triggered successfully. Processed products count: " + count);
    }
}
