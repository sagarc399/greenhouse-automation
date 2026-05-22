package com.greenhouse.app.service;

import com.greenhouse.app.dto.AutomationRuleDto;
import com.greenhouse.app.entity.*;
import com.greenhouse.app.entity.AutomationRule.RuleOperator;
import com.greenhouse.app.entity.Sensor.SensorType;
import com.greenhouse.app.exception.ResourceNotFoundException;
import com.greenhouse.app.repository.*;
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
 * Unit tests for {@link AutomationRuleService}.
 */
@ExtendWith(MockitoExtension.class)
class AutomationRuleServiceTest {

    @Mock
    private AutomationRuleRepository ruleRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private SensorRepository sensorRepository;
    @Mock
    private ActuatorRepository actuatorRepository;
    @Mock
    private AlertService alertService;

    @InjectMocks
    private AutomationRuleService service;

    private Zone zone;
    private Sensor sensor;
    private AutomationRule rule;

    /**
     * Sets up sample entities before each test.
     */
    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId(1L);
        zone.setName("Zone A");

        sensor = new Sensor();
        sensor.setId(1L);
        sensor.setName("Temp Sensor");
        sensor.setType(SensorType.TEMPERATURA);
        sensor.setZone(zone);

        rule = new AutomationRule();
        rule.setId(1L);
        rule.setName("High Temp Rule");
        rule.setZone(zone);
        rule.setSensor(sensor);
        rule.setOperator(RuleOperator.MAYOR_QUE);
        rule.setThreshold(35.0);
        rule.setActive(true);
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Verifies that findAll returns automation rule DTOs.
     */
    @Test
    @DisplayName("findAll returns rule DTOs")
    void findAll_returnsDtos() {
        when(ruleRepository.findAll()).thenReturn(List.of(rule));

        List<AutomationRuleDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).operator()).isEqualTo(RuleOperator.MAYOR_QUE);
    }

    /**
     * Verifies that a rule with MAYOR_QUE operator fires when value exceeds threshold.
     */
    @Test
    @DisplayName("evaluateRulesForSensor fires rule when condition met")
    void evaluateRules_conditionMet_createsAlert() {
        when(ruleRepository.findBySensorIdAndActive(1L, true)).thenReturn(List.of(rule));

        service.evaluateRulesForSensor(sensor, 40.0);

        verify(alertService).createFromRule(any(), any(), eq(40.0), eq(sensor), eq(rule));
    }

    /**
     * Verifies that a rule does NOT fire when the condition is not met.
     */
    @Test
    @DisplayName("evaluateRulesForSensor does not fire when condition not met")
    void evaluateRules_conditionNotMet_noAlert() {
        when(ruleRepository.findBySensorIdAndActive(1L, true)).thenReturn(List.of(rule));

        service.evaluateRulesForSensor(sensor, 30.0);

        verifyNoInteractions(alertService);
    }

    /**
     * Verifies that delete throws when rule is missing.
     */
    @Test
    @DisplayName("delete throws for non-existent rule")
    void delete_missingId_throwsNotFound() {
        when(ruleRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
