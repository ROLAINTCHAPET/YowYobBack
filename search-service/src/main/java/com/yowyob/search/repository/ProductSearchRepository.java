package com.yowyob.search.repository;

import com.yowyob.search.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductSearchRepository extends ReactiveElasticsearchRepository<ProductDocument, String> {
    Flux<ProductDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}