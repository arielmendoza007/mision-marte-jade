# Reorganización de comportamientos — Olympus Mons

Se extrajeron los 11 comportamientos que antes estaban como clases internas o anónimas de los agentes.
Se conservaron los mismos nombres locales de JADE: **entorno**, **coordinador**, **rover**.

```text
src/marte/
├── agentes/
│   ├── Entorno.java
│   ├── Coordinador.java
│   └── Rover.java
├── comportamientos/
│   ├── entorno/
│   │   ├── EscucharEntorno.java       (Cyclic)
│   │   ├── IniciarMision.java        (OneShot)
│   │   ├── ConsultarRover.java       (OneShot)
│   │   └── VigilarMision.java        (Waker)
│   ├── coordinador/
│   │   ├── EscucharCoordinador.java  (Cyclic)
│   │   └── EvaluarLectura.java       (Simple)
│   └── rover/
│       ├── PresentarseRover.java    (OneShot)
│       ├── EscucharRover.java        (Cyclic)
│       ├── TelemetriaRover.java      (Ticker)
│       ├── EjecutarAccion.java       (Waker)
│       └── RegresarBase.java        (Waker)
├── modelo/                      (sin cambios funcionales)
└── gui/                         (sin cambios funcionales)
```

Cada agente mantiene su estado privado (batería, lecturas, métricas). Los comportamientos
reciben la referencia al agente por constructor y llaman métodos públicos concretos
para interactuar con ese estado, sin duplicar las variables. Las reglas, tiempos y mensajes ACL
del proyecto original se preservaron.

Para ejecución local, los nombres de agentes deben ser exactamente:

```
entorno:marte.agentes.Entorno;coordinador:marte.agentes.Coordinador;rover:marte.agentes.Rover
```

**Importante:** Los PDF que venían en el ZIP original se conservaron como materiales
originales y no fueron regenerados automáticamente. Si se entrega la documentación
impresa, conviene regenerar los diagramas PDF y verificar las referencias a las clases.
La rúbrica requiere autorización expresa y declaración del uso de IA antes de presentar
código o documentación asistidos por IA; confirmen esto con la docente.
