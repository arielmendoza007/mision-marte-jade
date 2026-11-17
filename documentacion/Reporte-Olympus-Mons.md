# Expedición Olympus Mons — evaluación del tercer parcial

| Dato de identificación | Información |
| --- | --- |
| Institución / curso | UAEH, ICBI / Optativa I: Sistemas Multiagentes, grupo 1 |
| Proyecto | Expedición autónoma al borde de la caldera de Olympus Mons |
| Equipo | Angel Ariel Mendoza Lopez; Brandon Torres de Paz; Leonardo Manriquez Ramos; Miguel Islas Reyes; Jesus Osvaldo Garcia Damian |
| Docente / fecha de entrega | COMPLETAR / 18 de noviembre de 2026 |

## Autoevaluación del equipo

El equipo propone una autoevaluación individual de **10/10** para cada integrante por el trabajo colectivo de diseñar, desarrollar, probar y preparar la defensa de la simulación. Esta es la valoración propuesta por el equipo; no sustituye la calificación de la docente ni acredita una prueba distribuida que todavía no se ha documentado.

| Integrante | Autoevaluación |
| --- | --- |
| Angel Ariel Mendoza Lopez | 10/10 |
| Brandon Torres de Paz | 10/10 |
| Leonardo Manriquez Ramos | 10/10 |
| Miguel Islas Reyes | 10/10 |
| Jesus Osvaldo Garcia Damian | 10/10 |

**Justificación conjunta:** se prepararon tres agentes con responsabilidades distintas, reglas de sistema experto, comunicación ACL, ontología con instancias anidadas, interfaz, diagramas, tres ensayos reproducibles y medidas registradas. Cada integrante debe estudiar y demostrar el funcionamiento completo ante la docente. **Pendientes para sustentar la entrega:** ejecutar y registrar la conexión entre dos laptops y comprobar que los cinco pueden explicar y solucionar un fallo de su implementación. No atribuir tareas individuales ni errores corregidos a una persona sin confirmarlo con el equipo.

## Introducción

Un rover simulado asciende desde un campamento de la falda hasta el borde de la caldera del volcán Olympus Mons. Su objetivo colectivo es obtener una muestra geológica confirmada y llevarla de vuelta; el interés individual del Rover es no emprender una acción que impida regresar. Para eso recibe lecturas del Entorno, el Coordinador propone acciones mediante reglas explícitas y el Rover negocia su ejecución mediante ACL. Es una simulación didáctica de estados de peligro y consumo: las lecturas, distancias y unidades de energía son inventadas y reproducibles. Olympus Mons es un volcán en escudo con caldera, de pendientes suaves a gran escala; el mapa es un perfil esquemático, no una cartografía científica [3, 4].

## Desarrollo — 1. Definición del sistema (0.5)

**a) Dominio y problemática.** La exploración autónoma enfrenta lecturas inciertas, obstáculos, polvo y energía limitada. El operador sólo elige una condición inicial de ensayo; no introduce manualmente polvo ni mineral. El sistema completa una sola expedición por pulsación, luego espera una nueva orden.

**b) Tipo de SMA y agentes.** Sistema experto distribuido: el Coordinador usa R1 a R5 y el Rover aplica una política local de reserva energética y límite de espera. Entorno mantiene el estado de la simulación, responde lecturas, integra interfaz y transmite resultados. Coordinador clasifica cada observación, propone acciones y calcula métricas. Rover solicita lecturas, decide aceptar o rechazar propuestas, ejecuta y regresa. Se ejecutan tres agentes JADE.

**c) Arquitectura.** Principal: `Entorno` + `Coordinador` + interfaz Swing. Remoto: `Rover` sin ventana propia. Ambos contenedores pertenecen a una plataforma JADE y comparten ontología `MisionMarte`. Ver `Arquitectura.pdf` y la secuencia `03-interaccion.pdf` en esta carpeta.

**d) Mensajes ACL.** Entorno → Rover: `REQUEST Exploracion` para iniciar, `REQUEST EstadoRover` para consultar, `INFORM Observacion` en respuesta a lecturas. Rover → Entorno: `QUERY_REF Exploracion`, `INFORM EstadoRover` y `INFORM Resultado`. Rover → Coordinador: `REQUEST Observacion`, `ACCEPT_PROPOSAL` o `REJECT_PROPOSAL` de una `Propuesta`. Coordinador → Rover: `PROPOSE Propuesta`; Coordinador → Entorno: `INFORM Propuesta` y `INFORM Metricas`; Entorno → Coordinador: `INFORM Resultado`. Todos emplean `conversation-id` M-n cuando corresponda.

## Desarrollo — 2. Diagramas AUML (0.5)

Anexos vectoriales, cada uno en una página legible: `01-casos-de-uso.pdf` (actor operador y acciones), `02-clases.pdf` (agentes y objetos anidados), `03-interaccion.pdf` (mensajes, aceptación y rechazo). Los `.puml` son la versión editable. La comunicación entre agentes y la estructura de contenidos son distintas: una flecha `INFORM` no implica que se haya transferido una clase Java serializada.

## Desarrollo — 3. Interfaz gráfica (2)

La pantalla principal ofrece iniciar la expedición y consultar el Rover. El menú Ensayos permite repetir las tres condiciones iniciales. La bitácora del Entorno se amplía arrastrando el divisor o pulsando Ampliar bitácora. Las respuestas directas entre Coordinador y Rover se comprueban en la consola del Coordinador o con el Sniffer de JADE. El mapa lateral Swing representa la ruta desde el campamento en la falda hasta la caldera, el obstáculo y el aviso de polvo alto. El dibujo es esquemático y no está a escala. Avanzar y regresar desplazan al rover; rodear muestra un desvío; esperar, escanear y tomar muestra lo mantienen en su tramo con la acción indicada. Un aviso de acción en curso inicia el dibujo. El destino y la entrega se confirman al recibir el resultado del Rover. El panel conserva la última lectura, la batería, la propuesta del Coordinador, la respuesta del Rover y la duración total.

## Desarrollo — 4. Implementación de agentes (0.5)

**Entorno:** conserva lecturas deterministas por `conversation-id`, obstáculo en ladera hasta el desvío, tormenta en caldera hasta que se espera una vez en NORMAL/BATERIA y permanente en TORMENTA. Una confirmación vuelve a medir en el mismo tramo. **Coordinador:** aplica en orden R1 polvo >=70 → ESPERAR; R2 obstáculo → RODEAR; R3 caldera con señal >=65 sin confirmar → ESCANEAR; R4 caldera con señal confirmada >=65 → MUESTRA; R5 tramo seguro → AVANZAR. **Rover:** comienza con 80 o con 50 en BATERIA; paga 8 por AVANZAR, 12 por RODEAR, 1 por ESPERAR, 5 por ESCANEAR, 15 por MUESTRA y 20 por REGRESAR. Acepta una acción si puede pagarla y conservar 20 para el regreso; tras dos esperas rechaza otra. Tras muestrear regresa sin nueva orden humana. No hay recarga automática durante una misión.

## Desarrollo — 5. ACL, performativas y plantillas (2)

Se usan **seis performativas**: `REQUEST`, `QUERY_REF`, `INFORM`, `PROPOSE`, `ACCEPT_PROPOSAL`, `REJECT_PROPOSAL`. `Entorno.Escuchar`, `Coordinador.Escuchar` y `Rover.Escuchar` combinan `MessageTemplate.MatchSender`, `MatchPerformative` y `MatchOntology` para consumir sólo mensajes correspondientes; el identificador M-n relaciona su contenido con una expedición. Ejemplo normal: `QUERY_REF Exploracion(Caldera)` → `INFORM Observacion(Lectura(Caldera))` → `REQUEST Observacion` → `PROPOSE Propuesta(ESCANEAR)` → `ACCEPT_PROPOSAL` → `INFORM Resultado`. En BATERIA la respuesta al escaneo es `REJECT_PROPOSAL` y el Rover produce un `Resultado` final de regreso.

## Desarrollo — 6. Comportamientos (0.5)

`CyclicBehaviour`: escucha permanente en los tres agentes. `SimpleBehaviour`: `Coordinador.Evaluar` procesa exactamente una lectura y declara `done()`. `OneShotBehaviour`: `Entorno` inicia y consulta; `Rover.Presentarse` informa su estado inicial. `TickerBehaviour`: `Rover.Telemetria` informa estado cada cinco segundos. `WakerBehaviour`: `EjecutarAccion` completa la acción a los 3200 ms. El regreso dispone de 4200 ms y Entorno controla un plazo de 45 s. La espera no bloquea el buzón del agente.

## Desarrollo — 7. Ontología e instancias (2)

**a) Definición.** `OntologiaMarte` extiende `BeanOntology`; registra `Sector` y `Lectura` como `Concept`, y `Exploracion`, `Observacion`, `Propuesta`, `Resultado`, `EstadoRover`, `Metricas` como `Predicate`. Se registra con `SLCodec` en los tres agentes. **b) Intercambio.** `Mensajes.crear` usa `ContentManager.fillContent`; `Mensajes.leer` usa `extractContent`. Se envían instancias independientes (`EstadoRover`) y anidadas (`Observacion → Lectura → Sector`, `Propuesta → Lectura → Sector`, `Resultado → Lectura → Sector`). `PruebaOntologia` reconstruye el contenido y comprueba el nombre del sector dentro de una propuesta.

## Desarrollo — 8. Distribución, evaluación y sistema experto (2)

**a y b) Dos equipos y comunicación efectiva.** Principal ejecuta Entorno y Coordinador; equipo remoto ejecuta Rover. `Iniciar-Principal.ps1 -Puerto 1099 -IpLocal IP_PRINCIPAL -JadeGui` y `Iniciar-Rover.ps1 -HostPrincipal IP_PRINCIPAL -Puerto 1099 -IpLocal IP_ROVER -PuertoLocal 1100`. En red común o Hamachi se usan IP reales o virtuales, jamás `127.0.0.1`. Verificar `Test-NetConnection`, Rover en RMA, `Consultar rover`, mismo M-n en ambos logs, regreso y tiempos. **Pendiente: insertar aquí capturas y resultados realmente obtenidos en dos computadoras.**

**c) Dos medidas de evaluación aplicadas.** El artículo de Soza Pollman [2] estudia habilidad social, autonomía y proactividad; adaptamos dos indicadores observables al intercambio de este proyecto sin atribuirle nuestras fórmulas: (1) tasa de respuesta a propuestas = respuestas (`ACCEPT` + `REJECT`) / propuestas × 100; (2) latencia media de respuesta = suma de tiempos entre `PROPOSE` y respuesta / número de respuestas, en microsegundos. `Coordinador` registra ambas por M-n y envía `Metricas` por ACL al Entorno. Además Entorno mide tiempo total hasta regreso, y `ensayo-local.csv` registra éxito del objetivo por escenario. Los valores siguientes proceden del CSV generado al ensayar la versión que se imprime en una máquina. La tasa se obtiene dividiendo los dos conteos. Un rechazo informado cuenta como respuesta.

[MEDICIONES]

Registrar por separado los resultados entre laptops.

**d) Tipo de agente de clase.** El Coordinador es un agente basado en reglas de **sistema experto**: una cadena ordenada R1–R5 asigna acciones según polvo, obstáculo, posición, señal y confirmación. La política energética del Rover puede rechazar esas acciones; esa negociación demuestra que el Coordinador no controla completamente al Rover.

## Evidencias, discusión y límites

Registro de laboratorio local: `ensayo-local.csv`, `metricas-local.txt`, `captura-consola-local.png`. La simulación fija el entorno para repetir y explicar causas; no usa sensores físicos, predicción meteorológica ni rutas geográficas reales. No se puede inferir viabilidad de una misión real por superar los tres ensayos. La red entre laptops, especialmente la ruta de retorno y reglas de firewall, debe ensayarse con equipos del grupo antes de entregar.

## Conclusiones

Tres agentes autónomos colaboran y negocian decisiones mediante JADE y una ontología compartida. El recorrido tiene meta y final verificables: completar muestra y retorno o abortar por seguridad. La interfaz deja ver la regla aplicada, el trayecto, los mensajes y las medidas; los tres escenarios locales ayudan a defender tanto el acierto como las limitaciones. Completar la conclusión del equipo con lo ocurrido en la prueba distribuida una vez realizada.

## Referencias bibliográficas

1. Telecom Italia Lab. JADE 4.6.0. https://jade.tilab.com/
2. Soza Pollman, H. (2014). Evaluando características del agente software. *Ingeniare. Revista chilena de ingeniería*, 22(3), 435–444. https://doi.org/10.4067/S0718-33052014000300014
3. NASA. A Traverse Across the Summit of Olympus Mons. https://science.nasa.gov/photojournal/a-traverse-across-the-summit-of-olympus-mons/
4. ESA. Olympus Mons — the caldera in close-up. https://www.esa.int/articles/about-us/directorates/science/mars-express/olympus-mons-the-caldera-in-close-up

**Antes de imprimir:** completar el nombre de la docente; verificar con los cinco integrantes la escritura de sus nombres y la autoevaluación; anexar evidencia distribuida real, revisar paginado y legibilidad; engrapar. La rúbrica solicita recuadro de identificación sin portada.
