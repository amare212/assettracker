package com.def.assettracker.service;

import com.def.assettracker.entity.Asset;
import com.def.assettracker.entity.AssetStatus;
import com.def.assettracker.entity.Category;
import com.def.assettracker.repository.AssetRepository;
import com.def.assettracker.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final CategoryRepository categoryRepository;

    public AssetService(
            AssetRepository assetRepository,
            CategoryRepository categoryRepository) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Asset> getAll() {
        return assetRepository.findAll();
    }

    public Asset getById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Asset not found"));
    }

    public List<Asset> getByStatus(AssetStatus status) {
        return assetRepository.findByStatus(status);
    }

    public List<Asset> getByCategory(Long categoryId) {
        return assetRepository.findByCategoryId(categoryId);
    }

    public Asset create(Asset asset) {
        validateAsset(asset);

        if (assetRepository.existsBySerialNumber(asset.getSerialNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Serial number already exists");
        }

        Category category = categoryRepository.findById(
                asset.getCategory().getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Category not found"));

        asset.setCategory(category);

        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.AVAILABLE);
        }

        return assetRepository.save(asset);
    }

    public Asset update(Long id, Asset input) {
        Asset existing = getById(id);
        validateAsset(input);

        boolean serialUsed = assetRepository.existsBySerialNumber(
                input.getSerialNumber());

        if (serialUsed &&
                !existing.getSerialNumber().equals(input.getSerialNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Serial number already exists");
        }

        if (input.getCategory() == null ||
                input.getCategory().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Category ID is required");
        }

        Category category = categoryRepository.findById(
                input.getCategory().getId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Category not found"));

        existing.setName(input.getName());
        existing.setSerialNumber(input.getSerialNumber());
        existing.setManufacturer(input.getManufacturer());
        existing.setPurchaseDate(input.getPurchaseDate());
        existing.setNotes(input.getNotes());
        existing.setCategory(category);

        if (input.getStatus() != null) {
            existing.setStatus(input.getStatus());
        }

        return assetRepository.save(existing);
    }

    public void delete(Long id) {
        Asset asset = getById(id);
        assetRepository.delete(asset);
    }

    private void validateAsset(Asset asset) {
        if (asset.getName() == null || asset.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Asset name is required");
        }

        if (asset.getSerialNumber() == null ||
                asset.getSerialNumber().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Serial number is required");
        }

        if (asset.getCategory() == null ||
                asset.getCategory().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Category ID is required");
        }
    }
}