package com.example.userservice.farmerService;

import com.example.userservice.entity.Crops;
import com.example.userservice.exception.CropNotFoundException;
import com.example.userservice.repository.CropRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CropServiceTest {

    @Mock
    private CropRepository cropRepository;

    @InjectMocks
    private CropService cropService;

    @Test
    void findAvailable_shouldCallFilteredMethod_whenAnyFilterProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Crops> page = new PageImpl<>(List.of(new Crops()));

        String category = "Grain";
        String location = "Delhi";
        String variety = null;

        when(cropRepository.findAvailableFiltered(
                eq(pageable), any(), eq(category), eq(location), eq(variety)))
                .thenReturn(page);

        Page<Crops> result = cropService.findAvailable(pageable, category, location, variety);

        assertEquals(1, result.getTotalElements());
        verify(cropRepository, times(1))
                .findAvailableFiltered(eq(pageable), any(), eq(category), eq(location), eq(variety));
        verify(cropRepository, never()).findAvailableCrops(any(), any());
    }

    @Test
    void findAvailable_shouldCallBasicMethod_whenNoFilterProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Crops> page = new PageImpl<>(List.of(new Crops()));

        String category = "  "; // empty after trim
        String location = null;
        String variety = "";

        when(cropRepository.findAvailableCrops(eq(pageable), any()))
                .thenReturn(page);

        Page<Crops> result = cropService.findAvailable(pageable, category, location, variety);

        assertEquals(1, result.getTotalElements());
        verify(cropRepository, times(1))
                .findAvailableCrops(eq(pageable), any());
        verify(cropRepository, never())
                .findAvailableFiltered(any(), any(), any(), any(), any());
    }

    @Test
    void getCropDetail_shouldReturnCrop_whenFound() {
        UUID id = UUID.randomUUID();
        Crops crop = new Crops();
        crop.setId(id);

        when(cropRepository.findById(id)).thenReturn(Optional.of(crop));

        Crops result = cropService.getCropDetail(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(cropRepository, times(1)).findById(id);
    }

    @Test
    void getCropDetail_shouldThrowException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(cropRepository.findById(id)).thenReturn(Optional.empty());

        CropNotFoundException ex = assertThrows(
                CropNotFoundException.class,
                () -> cropService.getCropDetail(id)
        );

        assertEquals("Crop not found", ex.getMessage());
        verify(cropRepository, times(1)).findById(id);
    }
}
