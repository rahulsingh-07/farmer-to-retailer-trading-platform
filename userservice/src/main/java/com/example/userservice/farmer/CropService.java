package com.example.userservice.farmer;

import com.example.userservice.entity.Crops;
import com.example.userservice.exception.CropNotFoundException;
import com.example.userservice.repository.CropRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CropService {
    private final CropRepository cropRepository;


    public Page<Crops> findAvailable(Pageable pageable, String category, String location, String variety) {
        LocalDateTime now = LocalDateTime.now();

        if ((category != null && !category.trim().isEmpty()) ||
                (location != null && !location.trim().isEmpty()) ||
                (variety != null && !variety.trim().isEmpty())) {

            return cropRepository.findAvailableFiltered(
                    pageable, now, category, location, variety);
        }

        return cropRepository.findAvailableCrops(pageable, now);
    }

    public Crops getCropDetail(UUID id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new CropNotFoundException("Crop not found"));
    }
}
