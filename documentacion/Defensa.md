# Ensayo de defensa del equipo

En cada ronda una persona opera la consola y otra elige al azar quién explica la lectura, la regla o el mensaje. Cambien los papeles hasta que los cinco hayan seguido una misión completa desde las dos laptops.

| Pregunta | Dónde comprobarla |
| --- | --- |
| ¿Quién mide el polvo y quién decide esperar? | Entorno.medir devuelve la lectura simulada. Coordinador.Evaluar prueba primero polvo ≥ 70. Rover.Escuchar comprueba batería y número de esperas. |
| ¿Por qué propone escanear y termina regresando? | En BATERIA el Rover tiene 21 unidades y necesita 5 + 20. Responde REJECT_PROPOSAL y vuelve. Llega con 1. |
| ¿Qué relación tiene esto con Casa y Persona? | En la práctica una Casa contenía Persona. En el proyecto una Observacion contiene Lectura y ésta contiene Sector. Abran Mensajes y localicen fillContent y extractContent. |
| ¿Cómo sabe quién debe recibir cada mensaje? | addReceiver indica el destinatario. En el buzón, MatchSender, MatchPerformative y MatchOntology filtran los mensajes. El conversation-id permite seguir M-1. |
| ¿Qué diferencia hay entre Waker y Ticker? | EjecutarAccion termina una acción al despertar una vez. Rover.Telemetria envía repetidamente el estado cada cinco segundos. Busquen también Simple, Cyclic y OneShot y expliquen cuándo terminan. |
| ¿Cómo demuestras que un rechazo es comunicación efectiva? | Comparen las respuestas y propuestas en BATERIA. El Coordinador cuenta el rechazo y mide el tiempo transcurrido con su propio reloj. La batería, las muestras y el tiempo total se registran por separado. |
| Si deja de responder el Rover, ¿qué revisas? | Intenten Consultar rover, revisen el contenedor en RMA y localicen la última conversación en ambas terminales. Verifiquen el puerto principal y la dirección anunciada por la segunda laptop. |
| ¿Por qué se mueve el paisaje? | Rover acepta la acción y envía un Resultado de inicio. Entorno lo recibe y Swing dibuja durante esa acción. Al llegar el resultado final se dibuja la base. |

## Antes de imprimir

Verifiquen los cinco nombres y el 10/10 anotado para cada integrante; completen el nombre de la docente. Anoten IP, contenedor, misión, muestras, batería y las dos medidas de la prueba física en la sección 8. Adjunten las capturas reales de ambas laptops. Regeneren el PDF y revisen una hoja horizontal al tamaño real. El documento se entrega paginado y engrapado.
