# Robot Guía para Personas con Discapacidad Visual

![Robot en acción](https://via.placeholder.com/800x400?text=Robot+Seguidor+de+Línea) <!-- Reemplazar con imagen real -->

Este repositorio contiene el código y documentación para un robot móvil autónomo diseñado para asistir a personas con discapacidad visual en entornos controlados como escuelas u hospitales. El robot sigue líneas pintadas en el suelo, detecta intersecciones y toma decisiones de navegación basadas en destinos seleccionados por el usuario mediante una aplicación Android vía Bluetooth.

## Características Principales

- 🚦 **Seguimiento de línea** con control Proporcional-Derivativo (PD)
- 📍 **Navegación en intersecciones** (giro izquierda/derecha o avance recto)
- 📱 **Interfaz Android accesible** con anuncios de voz
- ⚡ **Comandos por Bluetooth** (inicio/detención y selección de destino)
- 🔊 **Retroalimentación auditiva** mediante auriculares Bluetooth
- 🤖 **Hardware de bajo costo** (ESP32-CAM, sensores IR, motores DC)

## Diagrama de Hardware

```mermaid
graph TD
    subgraph Robot
        A[ESP32-CAM] --> B[Sensores IR]
        A --> C[Driver L298N]
        C --> D[Motor Izquierdo]
        C --> E[Motor Derecho]
        A --> F[Bluetooth]
    end
    
    subgraph Usuario
        G[Aplicación] --> F
        H[Auriculares Bluetooth] --> F
    end
