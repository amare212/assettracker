package com.def.assettracker.repository;

import com.def.assettracker.entity.Asset;
import com.def.assettracker.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    boolean existsBySerialNumber(String serialNumber);

    List<Asset> findByStatus(AssetStatus status);

    List<Asset> findByCategoryId(Long categoryId);
}