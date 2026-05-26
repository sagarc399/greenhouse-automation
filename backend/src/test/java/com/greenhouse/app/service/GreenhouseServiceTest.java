package com.greenhouse.app.service;

import com.greenhouse.app.dto.GreenhouseDto;
import com.greenhouse.app.entity.Greenhouse;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.GreenhouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GreenhouseService}.
 *
 * <p>All repository calls are mocked with Mockito so no database is required.</p>
 */
@ExtendWith(MockitoExtension.class)
class GreenhouseServiceTest {

    @Mock
    private GreenhouseRepository repository;

    @InjectMocks
    private GreenhouseService service;

    private Greenhouse sampleGreenhouse;

    /**
     * Sets up a sample greenhouse entity used across tests.
     */
    @BeforeEach
    void setUp() {
        sampleGreenhouse = new Greenhouse();
        sampleGreenhouse.setId(1L);
        sampleGreenhouse.setName("Test Greenhouse");
        sampleGreenhouse.setLocation("Test Location");
        sampleGreenhouse.setDescription("Test Description");
        sampleGreenhouse.setCreatedAt(LocalDateTime.now());
        sampleGreenhouse.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Verifies that findAll returns a DTO list matching the repository result.
     */
    @Test
    @DisplayName("findAll returns list of greenhouse DTOs")
    void findAll_returnsAllGreenhouses() {
        when(repository.findAll()).thenReturn(List.of(sampleGreenhouse));

        List<GreenhouseDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("FAIL_TEST");
    }

    /**
     * Verifies that findById returns the correct DTO for an existing ID.
     */
    @Test
    @DisplayName("findById returns DTO for existing greenhouse")
    void findById_existingId_returnsDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleGreenhouse));

        GreenhouseDto result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Test Greenhouse");
    }

    /**
     * Verifies that findById throws ResourceNotFoundException for a missing ID.
     */
    @Test
    @DisplayName("findById throws ResourceNotFoundException for missing ID")
    void findById_missingId_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * Verifies that create persists and returns the new greenhouse as a DTO.
     */
    @Test
    @DisplayName("create persists greenhouse and returns DTO")
    void create_validDto_persistsAndReturns() {
        GreenhouseDto dto = new GreenhouseDto(null, "New GH", "Location", "Desc", null, null, null);
        when(repository.save(any(Greenhouse.class))).thenReturn(sampleGreenhouse);

        GreenhouseDto result = service.create(dto);

        verify(repository).save(any(Greenhouse.class));
        assertThat(result.name()).isEqualTo("Test Greenhouse");
    }

    /**
     * Verifies that update applies new values and persists the entity.
     */
    @Test
    @DisplayName("update applies new values and returns updated DTO")
    void update_existingId_updatesAndReturns() {
        GreenhouseDto dto = new GreenhouseDto(1L, "Updated", "New Location", "New Desc", null, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(sampleGreenhouse));
        when(repository.save(any(Greenhouse.class))).thenAnswer(inv -> {
            Greenhouse g = inv.getArgument(0);
            return g;
        });

        GreenhouseDto result = service.update(1L, dto);

        assertThat(result.name()).isEqualTo("Updated");
    }

    /**
     * Verifies that delete calls the repository delete method for a valid ID.
     */
    @Test
    @DisplayName("delete calls repository for existing ID")
    void delete_existingId_callsRepository() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    /**
     * Verifies that delete throws ResourceNotFoundException for a missing ID.
     */
    @Test
    @DisplayName("delete throws ResourceNotFoundException for missing ID")
    void delete_missingId_throwsNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
