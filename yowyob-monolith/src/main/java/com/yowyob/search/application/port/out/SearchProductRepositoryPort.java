package com.yowyob.search.application.port.out;

import com.yowyob.search.domain.model.SearchProduct;
import java.util.List;
import java.util.Optional;

public interface SearchProductRepositoryPort {
    List<SearchProduct> search(String query, String category, String city);
    Optional<SearchProduct> findById(String id);
    SearchProduct save(SearchProduct product);
    void deleteById(String id);
}
