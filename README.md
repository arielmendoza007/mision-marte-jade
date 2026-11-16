# Expedición Olympus Mons — JADE 4.6.0

Proyecto del tercer parcial de Sistemas Multiagentes. Es una **simulación didáctica** de una expedición desde una base en la falda de Olympus Mons hasta el **borde de su caldera**, con retorno a base. No representa distancias, alturas, datos de sensores ni capacidades reales de un rover. Olympus Mons es un volcán en escudo de pendientes suaves; la caldera es la depresión próxima a su cumbre. El perfil en pantalla es esquemático.

## Qué se hace en la interfaz

El operador pulsa **Iniciar expedición**. En el menú **Ensayos** puede repetir una tormenta persistente o un caso con batería limitada. La cámara frontal representa las acciones que el rover comunica por ACL. Entorno genera lecturas reproducibles de Falda, Ladera y Caldera; Coordinador aplica las reglas del sistema experto; Rover decide si puede ejecutar cada propuesta y solicita por sí mismo la siguiente lectura. El viaje termina con muestra entregada o regreso preventivo. **Consultar rover** pide su estado actual por ACL.

| Escenario | Suceso relevante | Resultado esperado |
| --- | --- | --- |
| Normal | Rodea un obstáculo; espera a que baje el polvo; confirma la señal | Toma una muestra y regresa; batería 11/100 |
| Tormenta persistente | El polvo sigue alto tras dos esperas | Rechaza una tercera espera y regresa; batería 30/100 |
| Batería limitada | Empieza con 50/100; no alcanza para escanear y reservar el regreso | Rechaza el escaneo y regresa; batería 1/100 |

La batería se inicializa **por escenario al iniciar una nueva expedición**. No hay recarga automática durante el recorrido. La energía restante de 1 al final del tercer caso significa que se gastaron 20 unidades simuladas en el regreso; las unidades no son una estimación de consumo real.

## Responsabilidades y protocolo

- **Entorno:** mantiene lecturas y cambios por escenario, muestra la ventana, responde `QUERY_REF` y comunica resultados al Coordinador. Se ejecuta en la computadora principal.
- **Coordinador:** aplica R1 polvo >= 70 → ESPERAR; R2 obstáculo → RODEAR; R3 señal >= 65 sin confirmar en Caldera → ESCANEAR; R4 señal confirmada >= 65 → MUESTRA; R5 tramo seguro → AVANZAR. Propone, registra respuestas y calcula medidas.
- **Rover:** conserva batería, posición, número de esperas y muestras. Solicita lecturas, acepta o rechaza cada propuesta según energía >= coste de acción + 20 para volver. Rechaza una tercera espera. Tras una muestra regresa por iniciativa propia. Puede ejecutarse en otra computadora.

Intercambian `REQUEST`, `QUERY_REF`, `INFORM`, `PROPOSE`, `ACCEPT_PROPOSAL` y `REJECT_PROPOSAL`; `conversation-id` agrupa todos los mensajes de una expedición. Los tres pares de agentes intercambian mensajes en ambos sentidos. `MessageTemplate` filtra por remitente, performativa y ontología. `Mensajes.crear` codifica objetos con FIPA-SL; `Mensajes.leer` los recupera. `OntologiaMarte` registra `Sector` (Concept), `Lectura` (Concept), `Exploracion`, `Observacion`, `Propuesta`, `Resultado`, `EstadoRover` y `Metricas` (Predicate). Ejemplo de objetos anidados: `Observacion → Lectura → Sector`.

Comportamientos usados: `CyclicBehaviour` (buzones), `SimpleBehaviour` (evaluar una lectura), `OneShotBehaviour` (inicio y consulta), `TickerBehaviour` (telemetría), `WakerBehaviour` (acciones temporizadas y plazo de misión). Las clases concretas están en `src/marte/agentes/`.

## Medición

El Coordinador envía en `Metricas` por misión: **tasa de respuesta** = respuestas a propuestas / propuestas enviadas y **latencia media de respuesta** = promedio en microsegundos entre cada `PROPOSE` y su `ACCEPT_PROPOSAL` o `REJECT_PROPOSAL`. El Entorno muestra ambas medidas en la bitácora. El reloj del Entorno además mide desde la solicitud inicial hasta el regreso en ms. Son operacionalizaciones propias inspiradas por las características de habilidad social, autonomía y proactividad estudiadas por Soza Pollman (2014), **no fórmulas atribuidas al artículo**. Un rechazo informado cuenta como respuesta; el éxito del objetivo se informa aparte. La latencia incluye mensajería JADE y planificación local y puede variar en otra computadora.

## Iniciar en una computadora

Descomprime el ZIP completo. Abre PowerShell **dentro de esta carpeta `rover-marte`**. Requiere JDK (`java` y `javac`); los scripts también buscan el instalado por la extensión Java de VS Code. El ZIP contiene `lib/jade.jar`.

El repositorio público contiene el código y los diagramas editables; para ejecutar una clonación, copia `jade.jar` del ZIP a `lib/jade.jar` o descarga JADE 4.6.0 desde el sitio oficial y coloca su JAR allí. El paquete Olympus-Mons de Actions incluye el PDF y las mediciones del ensayo automático. Desde un ZIP de código fuente, ejecuta Ensayar-Local.ps1 antes de generar los PDF; `documentacion/generar_pdf.py` permite regenerarlos en Windows con Python, `reportlab` y `pypdf`.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Principal.ps1 -Local -IpLocal 127.0.0.1 -Puerto 0
```

Ensayo automático local con los tres escenarios, CSV de resultados, métricas y captura de la consola:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Ensayar-Local.ps1
```

Para ver contenedores distintos en **una misma laptop**, abre dos ventanas PowerShell dentro de esta carpeta. En la primera:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Principal.ps1 -Puerto 1099 -IpLocal 127.0.0.1 -JadeGui
```

En la segunda:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Rover.ps1 -HostPrincipal 127.0.0.1 -Puerto 1099 -IpLocal 127.0.0.1 -PuertoLocal 1100
```

El contenedor principal abre la ventana y la GUI de JADE. El Rover **no tiene segunda interfaz gráfica**: su terminal muestra decisiones y su contenedor aparece en la GUI de JADE. Esta prueba local no demuestra la distribución entre máquinas.

## Prueba presencial en dos laptops

1. Copien la carpeta entera `rover-marte` en ambos equipos. Conecten los dos a la misma red o a la misma red virtual Hamachi y obtengan las IPv4 de ambos. Si usan Hamachi, utilicen la IPv4 que muestra Hamachi en cada laptop (por ejemplo, `25.x.x.x`); **no** `127.0.0.1`.
2. En el equipo principal, desde esta carpeta, inicien el contenedor principal:

   ```powershell
   powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Principal.ps1 -Puerto 1099 -IpLocal IP_PRINCIPAL -JadeGui
   ```

3. En la laptop del Rover, comprueben la ruta de conexión y únanse:

   ```powershell
   Test-NetConnection IP_PRINCIPAL -Port 1099
   powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Rover.ps1 -HostPrincipal IP_PRINCIPAL -Puerto 1099 -IpLocal IP_ROVER -PuertoLocal 1100
   ```

4. En JADE RMA verifiquen que `rover` esté en el segundo contenedor. En la consola pulsen **Consultar rover**, ejecuten el escenario normal y comprueben M-1 en ambas terminales, `REGRESAR`, muestra = 1 y las métricas. Guarden capturas de los dos equipos, RMA y tiempos observados en la sección 8 del reporte.
5. Si falla el puerto 1099, revisen IP, Hamachi y regla de firewall específica para el proceso Java y puerto 1099. Si se une el contenedor pero no vuelven mensajes, comprueben la IPv4 anunciada por el Rover, el puerto local 1100 y las reglas aplicables; eviten desactivar todo el firewall. Dejen ambas terminales abiertas.

El entorno de ejecución aislado donde se preparó esta entrega no permite verificar la conexión entre dos máquinas; regístrenla con sus mediciones reales, no con los datos locales del CSV.

## Documentación y materiales

Para preparar la defensa, abran [Guía de defensa](documentacion/Defensa.md). El PDF combinado se llama `documentacion/Entrega-imprimible.pdf`. Tras editar el recuadro de identificación, la autoevaluación y la evidencia física, regeneren el PDF con `python documentacion/generar_pdf.py`.


`documentacion/` contiene los tres diagramas AUML y el reporte para imprimir. El texto impreso requiere completar nombres, autoevaluación y evidencia presencial; comprueben que los diagramas quedan legibles en las hojas. `src/marte/PruebaOntologia.java` verifica extracción de mensajes con instancias anidadas.

Referencias: [JADE](https://jade.tilab.com/); [Soza Pollman (2014)](https://www.scielo.cl/scielo.php?script=sci_arttext&pid=S0718-33052014000300014); [NASA, Olympus Mons](https://science.nasa.gov/photojournal/a-traverse-across-the-summit-of-olympus-mons/); [ESA, caldera](https://www.esa.int/articles/about-us/directorates/science/mars-express/olympus-mons-the-caldera-in-close-up).
