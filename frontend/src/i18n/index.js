import { createI18n } from 'vue-i18n'

const messages = {
  en: {
    nav: {
      dashboard: 'Dashboard',
      greenhouses: 'Greenhouses',
      zones: 'Zones',
      sensors: 'Sensors',
      actuators: 'Actuators',
      rules: 'Automation Rules',
      alerts: 'Alerts',
      readings: 'Sensor Readings',
      logout: 'Logout'
    },
    dashboard: {
      title: 'Dashboard',
      greenhouses: 'Greenhouses',
      zones: 'Zones',
      activeSensors: 'Active Sensors',
      runningActuators: 'Running Actuators',
      activeRules: 'Active Rules',
      pendingAlerts: 'Pending Alerts',
      criticalAlerts: 'Critical Alerts'
    },
    common: {
      name: 'Name',
      description: 'Description',
      location: 'Location',
      type: 'Type',
      status: 'Status',
      actions: 'Actions',
      save: 'Save',
      cancel: 'Cancel',
      delete: 'Delete',
      edit: 'Edit',
      create: 'Create',
      confirm: 'Confirm',
      loading: 'Loading...',
      noData: 'No data available',
      createdAt: 'Created At',
      updatedAt: 'Updated At',
      zone: 'Zone',
      zones: 'Zones',
      active: 'Active',
      none: 'None'
    },
    greenhouse: {
      title: 'Greenhouses',
      new: 'New Greenhouse',
      edit: 'Edit Greenhouse',
      deleteConfirm: 'Delete this greenhouse and all its zones?'
    },
    zone: {
      title: 'Zones',
      new: 'New Zone',
      edit: 'Edit Zone',
      greenhouse: 'Greenhouse',
      deleteConfirm: 'Delete this zone and all its sensors and actuators?'
    },
    sensor: {
      title: 'Sensors',
      new: 'New Sensor',
      edit: 'Edit Sensor',
      zone: 'Zone',
      model: 'Model',
      active: 'Active',
      deleteConfirm: 'Delete this sensor and all its readings?',
      types: {
        TEMPERATURA: 'Temperature',
        HUMEDAD_AMBIENTAL: 'Air Humidity',
        HUMEDAD_SUELO: 'Soil Moisture',
        LUZ: 'Light',
        PH: 'pH'
      }
    },
    actuator: {
      title: 'Actuators',
      new: 'New Actuator',
      edit: 'Edit Actuator',
      state: 'State',
      deleteConfirm: 'Delete this actuator?',
      types: {
        RIEGO: 'Irrigation',
        VENTILACION: 'Ventilation',
        ILUMINACION: 'Lighting',
        CALEFACCION: 'Heating'
      },
      states: {
        ENCENDIDO: 'On',
        APAGADO: 'Off'
      }
    },
    rule: {
      title: 'Automation Rules',
      new: 'New Rule',
      edit: 'Edit Rule',
      sensor: 'Sensor',
      operator: 'Operator',
      threshold: 'Threshold',
      targetActuator: 'Target Actuator',
      targetState: 'Target State',
      condition: 'Condition',
      actuator: 'Actuator',
      active: 'Active',
      deleteConfirm: 'Delete this automation rule?',
      operators: {
        MAYOR_QUE: 'Greater than',
        MENOR_QUE: 'Less than',
        IGUAL_QUE: 'Equal to'
      }
    },
    alert: {
      title: 'Alerts',
      severity: 'Severity',
      message: 'Message',
      triggerValue: 'Trigger Value',
      markAttended: 'Mark as Attended',
      pendingOnly: 'Pending only',
      sensor: 'Sensor',
      severities: {
        BAJA: 'Low',
        MEDIA: 'Medium',
        ALTA: 'High',
        CRITICA: 'Critical'
      },
      statuses: {
        PENDIENTE: 'Pending',
        ATENDIDA: 'Attended'
      }
    },
    reading: {
      title: 'Sensor Readings',
      new: 'New Reading',
      value: 'Value',
      unit: 'Unit',
      recordedAt: 'Recorded At',
      selectSensor: 'Select a sensor',
      deleteConfirm: 'Delete this reading?'
    },
    auth: {
      login: 'Sign in',
      loginWith: 'Sign in with Google',
      loginDesc: 'Greenhouse Management System',
      welcome: 'Welcome back!'
    }
  },
  es: {
    nav: {
      dashboard: 'Panel',
      greenhouses: 'Invernaderos',
      zones: 'Zonas',
      sensors: 'Sensores',
      actuators: 'Actuadores',
      rules: 'Reglas de Automatización',
      alerts: 'Alertas',
      readings: 'Lecturas de Sensores',
      logout: 'Cerrar sesión'
    },
    dashboard: {
      title: 'Panel de Control',
      greenhouses: 'Invernaderos',
      zones: 'Zonas',
      activeSensors: 'Sensores Activos',
      runningActuators: 'Actuadores Encendidos',
      activeRules: 'Reglas Activas',
      pendingAlerts: 'Alertas Pendientes',
      criticalAlerts: 'Alertas Críticas'
    },
    common: {
      name: 'Nombre',
      description: 'Descripción',
      location: 'Ubicación',
      type: 'Tipo',
      status: 'Estado',
      actions: 'Acciones',
      save: 'Guardar',
      cancel: 'Cancelar',
      delete: 'Eliminar',
      edit: 'Editar',
      create: 'Crear',
      confirm: 'Confirmar',
      loading: 'Cargando...',
      noData: 'Sin datos disponibles',
      createdAt: 'Creado el',
      updatedAt: 'Actualizado el',
      zone: 'Zona',
      zones: 'Zonas',
      active: 'Activo',
      none: 'Ninguno'
    },
    greenhouse: {
      title: 'Invernaderos',
      new: 'Nuevo Invernadero',
      edit: 'Editar Invernadero',
      deleteConfirm: '¿Eliminar este invernadero y todas sus zonas?'
    },
    zone: {
      title: 'Zonas',
      new: 'Nueva Zona',
      edit: 'Editar Zona',
      greenhouse: 'Invernadero',
      deleteConfirm: '¿Eliminar esta zona y todos sus sensores y actuadores?'
    },
    sensor: {
      title: 'Sensores',
      new: 'Nuevo Sensor',
      edit: 'Editar Sensor',
      zone: 'Zona',
      model: 'Modelo',
      active: 'Activo',
      deleteConfirm: '¿Eliminar este sensor y todas sus lecturas?',
      types: {
        TEMPERATURA: 'Temperatura',
        HUMEDAD_AMBIENTAL: 'Humedad Ambiental',
        HUMEDAD_SUELO: 'Humedad de Suelo',
        LUZ: 'Luz',
        PH: 'pH'
      }
    },
    actuator: {
      title: 'Actuadores',
      new: 'Nuevo Actuador',
      edit: 'Editar Actuador',
      state: 'Estado',
      deleteConfirm: '¿Eliminar este actuador?',
      types: {
        RIEGO: 'Riego',
        VENTILACION: 'Ventilación',
        ILUMINACION: 'Iluminación',
        CALEFACCION: 'Calefacción'
      },
      states: {
        ENCENDIDO: 'Encendido',
        APAGADO: 'Apagado'
      }
    },
    rule: {
      title: 'Reglas de Automatización',
      new: 'Nueva Regla',
      edit: 'Editar Regla',
      sensor: 'Sensor',
      operator: 'Operador',
      threshold: 'Umbral',
      targetActuator: 'Actuador Objetivo',
      targetState: 'Estado Objetivo',
      condition: 'Condición',
      actuator: 'Actuador',
      active: 'Activo',
      deleteConfirm: '¿Eliminar esta regla de automatización?',
      operators: {
        MAYOR_QUE: 'Mayor que',
        MENOR_QUE: 'Menor que',
        IGUAL_QUE: 'Igual a'
      }
    },
    alert: {
      title: 'Alertas',
      severity: 'Severidad',
      message: 'Mensaje',
      triggerValue: 'Valor Disparador',
      markAttended: 'Marcar como Atendida',
      pendingOnly: 'Solo pendientes',
      sensor: 'Sensor',
      severities: {
        BAJA: 'Baja',
        MEDIA: 'Media',
        ALTA: 'Alta',
        CRITICA: 'Crítica'
      },
      statuses: {
        PENDIENTE: 'Pendiente',
        ATENDIDA: 'Atendida'
      }
    },
    reading: {
      title: 'Lecturas de Sensores',
      new: 'Nueva Lectura',
      value: 'Valor',
      unit: 'Unidad',
      recordedAt: 'Registrado el',
      selectSensor: 'Seleccionar sensor',
      deleteConfirm: '¿Eliminar esta lectura?'
    },
    auth: {
      login: 'Iniciar sesión',
      loginWith: 'Iniciar sesión con Google',
      loginDesc: 'Sistema de Gestión de Invernaderos',
      welcome: '¡Bienvenido!'
    }
  }
}

export default createI18n({
  legacy: false,
  locale: localStorage.getItem('locale') || 'es',
  fallbackLocale: 'en',
  messages
})
