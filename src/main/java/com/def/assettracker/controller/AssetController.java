package com.def.assettracker.controller;

import com.def.assettracker.entity.Asset;
import com.def.assettracker.entity.AssetStatus;
import com.def.assettracker.service.AssetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<Asset> getAll() {
        return assetService.getAll();
    }

    @GetMapping("/{id}")
    public Asset getById(@PathVariable Long id) {
        return assetService.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<Asset> getByStatus(@PathVariable AssetStatus status) {
        return assetService.getByStatus(status);
    }

    @GetMapping("/category/{categoryId}")
    public List<Asset> getByCategory(@PathVariable Long categoryId) {
        return assetService.getByCategory(categoryId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Asset create(@RequestBody Asset asset) {
        return assetService.create(asset);
    }

    @PutMapping("/{id}")
    public Asset update(
            @PathVariable Long id,
            @RequestBody Asset asset) {
        return assetService.update(id, asset);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assetService.delete(id);
    }
}