package com.riachuelo.product.service;

import com.riachuelo.product.dto.ProductCreatedEvent;
import com.riachuelo.product.dto.ProductDeletedEvent;
import com.riachuelo.product.dto.ProductRequest;
import com.riachuelo.product.dto.ProductResponse;
import com.riachuelo.product.entity.ProductEntity;
import com.riachuelo.product.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMessageProducer messageProducer;

    public ProductService(ProductRepository productRepository, ProductMessageProducer messageProducer) {
        this.productRepository = productRepository;
        this.messageProducer = messageProducer;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        ProductEntity product = new ProductEntity(
                request.name(),
                request.description(),
                request.price()
        );
        ProductEntity savedProduct = productRepository.save(product);
        messageProducer.publishProductCreated(toCreatedEvent(savedProduct));
        return toResponse(savedProduct);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return toResponse(findProduct(id));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        ProductEntity product = findProduct(id);
        product.update(request.name(), request.description(), request.price());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findProduct(id));
        // Garante que a exclusao foi aceita pelo banco antes de avisar o estoque.
        productRepository.flush();
        messageProducer.publishProductDeleted(new ProductDeletedEvent(id));
    }

    private ProductEntity findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product " + id + " was not found."
                ));
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    private ProductCreatedEvent toCreatedEvent(ProductEntity product) {
        return new ProductCreatedEvent(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }
}
