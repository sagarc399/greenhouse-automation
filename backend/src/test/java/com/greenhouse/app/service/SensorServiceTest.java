package com.greenhouse.app.service;

import com.greenhouse.app.dto.SensorDto;
import com.greenhouse.app.entity.Sensor;
import com.greenhouse.app.entity.Sensor.SensorType;
import com.greenhouse.app.entity.Zone;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.SensorRepository;
import com.greenhouse.app.repository.ZoneRepository;
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
 * Unit tests for {@link SensorService}.
 */
@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private SensorService service;

    private Sensor sampleSensor;
    private Zone sampleZone;

    /**
     * Sets up sample entities before each test.
     */
    @BeforeEach
    void setUp() {
        sampleZone = new Zone();
        sampleZone.setId(1L);
        sampleZone.setName("Zone A");

        sampleSensor = new Sensor();
        sampleSensor.setId(1L);
        sampleSensor.setName("Temp Sensor 1");
        sampleSensor.setType(SensorType.TEMPERATURA);
        sampleSensor.setActive(true);
        sampleSensor.setZone(sampleZone);
        sampleSensor.setCreatedAt(LocalDateTime.now());
        sampleSensor.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Verifies that findAll returns all sensors as DTOs.
     */
    @Test
    @DisplayName("findAll returns sensor DTOs")
    void findAll_returnsDtos() {
        when(sensorRepository.findAll()).thenReturn(List.of(sampleSensor));

        List<SensorDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(SensorType.TEMPERATURA);
    }

    /**
     * Verifies that create saves a sensor when zone exists.
     */
    @Test
    @DisplayName("create saves sensor given valid zone")
    void create_validZone_savesSensor() {
        SensorDto dto = new SensorDto(null, "New Sensor", SensorType.HUMEDAD_AMBIENTAL,
                null, true, 1L, null, null, null);
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(sampleZone));
        when(sensorRepository.save(any(Sensor.class))).thenReturn(sampleSensor);

        SensorDto result = service.create(dto);

        verify(sensorRepository).save(any(Sensor.class));
        assertThat(result).isNotNull();
    }

    /**
     * Verifies that create throws ResourceNotFoundException when zone is missing.
     */
    @Test
    @DisplayName("create throws when zone not found")
    void create_missingZone_throwsNotFound() {
        SensorDto dto = new SensorDto(null, "New Sensor", SensorType.LUZ,
                null, true, 99L, null, null, null);
        when(zoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * Verifies that delete throws ResourceNotFoundException for a missing sensor.
     */
    @Test
    @DisplayName("delete throws for non-existent sensor")
    void delete_missingId_throwsNotFound() {
        when(sensorRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
