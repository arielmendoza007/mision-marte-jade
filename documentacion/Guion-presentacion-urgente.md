# Guion para defender Olympus Mons

## Presentación oral (4 minutos)

«Simulamos una expedición de un rover desde un campamento en la falda de Olympus Mons hasta el borde de la caldera. La meta colectiva es confirmar una señal mineral, tomar una muestra y devolverla a base. Hay un conflicto útil para un sistema multiagente: el Coordinador quiere continuar la misión, pero el Rover sólo acepta acciones si conserva energía para volver. No usamos datos reales de Marte; los sensores del Entorno son valores programados para repetir los tres ensayos.

Usamos JADE y tres agentes. Entorno simula lecturas y mantiene la ventana. Coordinador aplica cinco reglas ordenadas: R1 polvo alto implica esperar; R2 obstáculo implica rodear; R3 señal en la caldera sin confirmar implica escanear; R4 señal confirmada implica tomar muestra; R5 camino seguro implica avanzar. Rover conserva su propia batería, posición y cantidad de esperas; pide lecturas y acepta o rechaza propuestas. Por eso el tipo elegido es sistema experto basado en reglas, con autonomía local del ejecutor.

Los agentes hablan mediante mensajes ACL. Por ejemplo, Entorno envía REQUEST para iniciar; Rover pide un dato mediante QUERY_REF; Entorno devuelve INFORM Observacion; Rover pide al Coordinador una decisión mediante REQUEST; Coordinador responde PROPOSE; Rover contesta ACCEPT_PROPOSAL o REJECT_PROPOSAL. El identificador M-1 relaciona los mensajes de una misma misión. Cada receptor usa MessageTemplate para seleccionar destinatario, tipo de mensaje y ontología.

La ontología MisionMarte establece los objetos que pueden viajar en esos mensajes, codificados mediante FIPA-SL. Sector está dentro de Lectura y Lectura está dentro de Observacion, Propuesta o Resultado. No mandamos una simple cadena con el nombre de la acción: mandamos objetos con campos que el receptor reconstruye y usa. Las clases del modelo siguen el mismo patrón de Concept, Predicate y propiedades get/set que practicamos durante el semestre.

Cada agente tiene comportamientos JADE que evitan detener su buzón: Cyclic escucha mensajes, OneShot inicia o consulta, Simple evalúa una lectura y termina, Ticker envía telemetría cada cinco segundos y Waker completa una acción tras un tiempo o vence una misión. El tiempo de ejecución simulado permite ver la propuesta, la decisión y el regreso en la interfaz.

Demostramos tres escenarios reproducibles: normal termina con una muestra y batería 11; tormenta persistente provoca un rechazo tras dos esperas y vuelve con batería 30; batería limitada impide escanear porque quedan 21 unidades y escanear más regresar necesita 25, así que vuelve con 1. Registramos respuestas/propuestas y latencia media entre propuesta y aceptación o rechazo; son indicadores que definimos para este sistema, inspirados en características de los agentes tratadas en el artículo, no fórmulas copiadas del artículo.

En la prueba distribuida el contenedor principal ejecuta Entorno y Coordinador; una segunda laptop ejecuta Rover. Hamachi proporciona las direcciones para que JADE se comunique por ambos sentidos. En la interfaz principal vemos el recorrido, estado, telemetría y bitácora; en la laptop remota se ve la terminal del Rover. En JADE RMA debe verse Rover bajo el segundo contenedor. El equipo conectó previamente la versión anterior; repetimos la demostración con este ZIP para acreditar específicamente esta revisión».

## Qué abrir cuando la profesora elija un tema

| Pregunta | Archivo | Qué enseñar |
| --- | --- | --- |
| ¿Dónde inicia la app? | `Iniciar-Principal.ps1`, `Entorno.java` | JADE arranca Entorno y Coordinador; `setup()` registra ontología, instala EscucharEntorno y abre Swing. |
| ¿Dónde están las reglas? | `comportamientos/coordinador/EvaluarLectura.java` | Las cinco condiciones están en orden; sólo la primera aplicable crea Propuesta. |
| ¿Quién decide finalmente? | `comportamientos/rover/EscucharRover.java` | Calcula costo + 20; comprueba esperas; acepta o rechaza y ejecuta o vuelve. |
| ¿De dónde salen polvo y mineral? | `agentes/Entorno.java`, método `medir()` | Son lecturas deterministas según Falda, Ladera, Caldera, escenario, espera y desvío. |
| ¿Cómo circulan los mensajes? | `modelo/Mensajes.java` | `crear()` agrega receptor, conversación, codec, ontología y `fillContent`; `leer()` usa `extractContent`. |
| ¿Qué es la ontología? | `modelo/OntologiaMarte.java`, `modelo/Lectura.java`, `modelo/Sector.java` | Registra tipos compartidos; Lectura contiene Sector; getters/setters permiten a BeanOntology leer propiedades. |
| ¿Qué es una plantilla? | `comportamientos/*/Escuchar*.java` | `MessageTemplate.MatchSender`, `MatchPerformative`, `MatchOntology` filtrados con `and`/`or`; `block()` espera sin consumir CPU en vacío. |
| ¿Cómo se mide? | `agentes/Coordinador.java` | Marca tiempo con `System.nanoTime()` al proponer, cuenta respuesta y calcula promedio en microsegundos; Entorno registra duración total aparte. |
| ¿Por qué hay 11 clases nuevas? | `src/marte/comportamientos/` | Comportamientos separados por agente para mostrar qué hace cada uno. Siguen siendo tres agentes, no catorce. |
| ¿Por qué la pantalla se mueve? | `gui/ConsolaMision.java`, `gui/MapaMarte.java` | Entorno recibe Resultado y pasa la etapa a la vista Swing; el dibujo no es sensor ni agente. |

## La secuencia NORMAL, con números

80 al iniciar → AVANZAR Falda cuesta 8 (72) → RODEAR obstáculo Ladera cuesta 12 (60) → AVANZAR Ladera cuesta 8 (52) → ESPERAR por polvo Caldera cuesta 1 (51) → ESCANEAR cuesta 5 (46) → MUESTRA cuesta 15 (31) → REGRESAR cuesta 20 (11). El mineral inicial de Caldera es 85; confirmar reduce esa señal a 82, aún mayor que el umbral 65. Durante una acción aparece `EN CURSO` y luego su `Resultado` parcial. La misión sólo termina cuando Rover informa que volvió a base.

## Escenarios de riesgo

TORMENTA: después de dos esperas, el Entorno sigue dando polvo 85; Coordinador vuelve a proponer ESPERAR y Rover responde REJECT_PROPOSAL; regresa desde 50 y queda en 30. BATERIA: se inicia en 50; después de avanzar, rodear, avanzar y esperar queda en 21; rechaza ESCANEAR porque 5 + 20 = 25; gasta 20 al regresar y termina con 1. Rechazar la propuesta también cuenta como respuesta en la métrica, aunque no significa cumplir el objetivo.

## Cómo leer los anexos

- Arquitectura: dos contenedores JADE; Entorno y Coordinador en principal, Rover en el remoto. Hay comunicación en ambos sentidos; ambas máquinas necesitan el mismo modelo y JAR de JADE.
- Casos de uso: el operador sólo inicia, consulta y observa. No decide el polvo, la acción ni la muestra.
- Clases y ontología: arriba hay tres agentes y sus 11 comportamientos; abajo, los predicados que contienen Lectura y el concepto Sector. `02-clases.puml` ofrece el detalle editable de las asociaciones.
- Interacción AUML: leer de arriba abajo las flechas etiquetadas REQUEST, QUERY_REF, INFORM, PROPOSE, ACCEPT_PROPOSAL y REJECT_PROPOSAL; los tramos se repiten hasta la muestra o el regreso preventivo.

## Demostración en dos laptops (misma versión)

1. Cerrar los procesos JADE anteriores. Descomprimir el ZIP actualizado en las dos máquinas. Abrir PowerShell dentro de la carpeta `Olympus-Mons` de cada una.
2. Principal (sustituir IP_PRINCIPAL por su IPv4 actual): `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Principal.ps1 -Puerto 1099 -IpLocal IP_PRINCIPAL -JadeGui`.
3. Rover (sustituir IP_PRINCIPAL e IP_ROVER por las IPv4 actuales): `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Rover.ps1 -HostPrincipal IP_PRINCIPAL -Puerto 1099 -IpLocal IP_ROVER -PuertoLocal 1100`.
4. Comprobar en RMA `Main-Container` (entorno/coordinador) y `Container-1` (rover). Pulsar Consultar rover; iniciar NORMAL. Mostrar el mismo M-1 y la respuesta desde ambas laptops; al final mostrar la muestra y las métricas.
5. Si cambiaron las IP, sustituirlas por las IPv4 que Hamachi muestre hoy. Si hay error al unirse, comprobar `Test-NetConnection IP_PRINCIPAL -Port 1099` en el remoto y `Test-NetConnection IP_ROVER -Port 1100` en el principal mientras el Rover está intentando conectar; revisar el firewall en ambos sentidos. No confundir dos contenedores en una sola PC con dos máquinas físicas.

## Tres preguntas para que se interroguen entre compañeros

1. ¿Por qué en la tormenta el Coordinador insiste en esperar pero Rover se vuelve? Explicar diferencia entre proponer y ejecutar, y el límite local de dos esperas.
2. ¿Cómo sabemos que la lectura viajó como instancia anidada? Abrir `Observacion.java`, `Lectura.java`, `Sector.java`, `Mensajes.java` y `PruebaOntologia.java`.
3. ¿Qué harías si la ventana queda en «esperando telemetría»? Confirmar que Rover aparece en RMA, mirar su consola, usar Consultar rover, comprobar IP anunciada y puertos 1099/1100. Si ya está unido, mirar las plantillas y ontología.

**Honestidad al defender:** compilación y tres escenarios probados en un proceso y en dos procesos locales; la conexión física fue lograda por el equipo con la versión anterior. Registrar una misión nueva entre laptops para poder afirmar que esta revisión también está probada remotamente. El uso de IA está autorizado según lo comunicado por el equipo, con la condición de entender y poder reparar lo entregado.
