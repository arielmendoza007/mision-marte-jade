# Guía breve para defender la expedición Olympus Mons

**Todos explican la misión completa.** Repartir exposiciones entre los cinco integrantes sirve para organizar la presentación; la profesora puede preguntar cualquier parte a cualquiera. No memoricen solamente nombres de clases: prueben cada caso y sigan su `M-n` en la bitácora.

## Guion de demostración (menos de diez minutos)

1. Enseñar la arquitectura impresa: Entorno y Coordinador en la principal; Rover en la segunda laptop. Mostrar `rover` en la GUI RMA de JADE y la terminal de cada computadora. Si sólo tienen una máquina, aclarar que esa prueba es local.
2. Pulsar **Consultar rover**. Seguir en la bitácora `REQUEST EstadoRover` y `INFORM EstadoRover`; explicar que el botón consulta a un agente y no modifica la lectura.
3. Seleccionar **Ascenso normal** y pulsar una vez. Explicar Falda, Ladera, Caldera, R1 a R5, la confirmación en Caldera y el regreso con una muestra. Leer M-n en las dos terminales.
4. Repetir TORMENTA y BATERIA, mostrando las causas distintas del `REJECT_PROPOSAL`. En la consola revisar el final, tiempo, respuestas/propuestas y latencia.
5. Mostrar los anexos de casos de uso, clases y secuencia por separado en hojas horizontales; luego señalar `Observacion(Lectura(Sector))` en la ontología.

## Cinco focos de estudio para repartirse el ensayo

| Integrante | Exposición principal | Debe poder demostrar |
| --- | --- | --- |
| 1 | Dominio y GUI | Cómo el operador elige el escenario y por qué los sensores pertenecen a Entorno |
| 2 | Reglas del Coordinador | Orden R1–R5 y ejemplo de ESCANEAR antes de MUESTRA |
| 3 | Autonomía del Rover | `costo + 20`, dos esperas, rechazo, regreso sin una nueva orden humana |
| 4 | ACL y ontología | Un `MessageTemplate`, `conversation-id` y objetos `Observacion → Lectura → Sector` |
| 5 | Infraestructura y medidas | Comandos, RMA, IPs, respuestas/propuestas y latencia, diferencia entre ensayo local y prueba remota |

## Preguntas que deben saber resolver todos

- **¿Por qué tres agentes?** El Entorno mantiene el mundo y muestra resultados; Coordinador propone con reglas; Rover guarda su estado y decide si ejecuta. Cada uno tiene objetivo y responsabilidad propios.
- **¿Dónde está el sistema experto?** En `EvaluarLectura`: reglas ordenadas sobre polvo, obstáculo, tramo, mineral y confirmación. El resultado es una `Propuesta`, no un movimiento obligatorio.
- **¿Dónde está la autonomía?** El Rover inicia nuevas lecturas, conserva batería y esperas, rechaza una orden insegura y regresa por su cuenta tras muestrear.
- **¿Qué se manda realmente?** JADE ACL con performativa, remitente/destinatario, ontología `MisionMarte`, lenguaje `fipa-sl`, `conversation-id` y contenido codificado con `fillContent`. No se manda texto escrito a mano como sustituto de la ontología.
- **¿Qué filtra `MessageTemplate`?** Remitente, performativa y ontología; identifica qué mensajes puede atender un comportamiento. La misión `M-n` se valida al procesar su contenido.
- **¿Qué significa 100 % de respuestas?** Todas las propuestas recibieron aceptación o rechazo; no significa que se hayan aceptado todas ni que todas las misiones lograran muestra.
- **¿Por qué microsegundos?** La negociación ACL local puede tardar menos de un milisegundo; se toma `System.nanoTime` en Coordinador antes de `PROPOSE` y al recibir la respuesta. Tiempo total de expedición se mide aparte en ms.
- **¿Por qué el paisaje no prueba ciencia marciana?** Ruta, consumo y lecturas están fijados para comparar decisiones; la referencia NASA/ESA sólo justifica el lugar y el perfil general del volcán.
- **¿Cómo diagnosticar un fallo remoto?** Confirmar IPv4 de ambos equipos y puerto 1099; después si el contenedor aparece en RMA; después `Consultar rover` y que regresen mensajes. Si JADE une pero no responde, comprobar IP anunciada y puerto local 1100, firewall y que ambos ejecuten la misma versión del código.

## Un recorrido normal con números

Inicio 80 → AVANZAR (72) → RODEAR (60) → AVANZAR (52) → ESPERAR (51) → ESCANEAR (46) → MUESTRA (31) → REGRESAR (11). Caldera se mide dos veces para confirmar antes de tomar la muestra. En TORMENTA se rechaza la tercera espera aun con energía. En BATERIA se llega a 21, pero escanear cuesta 5 y el regreso previsto 20: 21 < 25; se rechaza.

## Antes de imprimir y presentar

Verificar los cinco nombres y la autoevaluación anotada; completar el nombre de la docente. Probar dos laptops, apuntar IPs, hora, resultado y latencia observados, adjuntar capturas auténticas; revisar todas las páginas a tamaño de impresión, paginar y engrapar. Si no hubo prueba remota, no afirmar que sí la hubo.
