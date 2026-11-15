# Misi�n Marte: selecci�n segura de muestras

Simulaci�n JADE con tres agentes: `entorno`, `coordinador` y `rover`.
El operador elige un sector. El Rover pide al Entorno la lectura simulada del siguiente punto y la remite al Coordinador. El coordinador propone
`ESPERAR`, `ESCANEAR` o `MUESTRA`; el rover acepta si puede gastar la energ�a
y conservar una reserva de 20 unidades. Si no puede, rechaza y regresa.

## Reglas del sistema experto

Se aplican en este orden (umbrales did�cticos, no datos cient�ficos de Marte):

1. `polvo >= 70`  `ESPERAR`.
2. Si hay obst�culo  `ESCANEAR`.
3. `mineral >= 65`  `MUESTRA`.
4. En otro caso  `ESCANEAR`.
5. El rover acepta solo si `bater�a >= costo + 20`; si rechaza por energ�a,
   ejecuta `REGRESAR`. Costos: muestra 15, escaneo 5, espera 1, regreso 8.

Cada sector tiene tres puntos con lecturas fijas. Una nueva visita al mismo
sector pasa al punto siguiente y luego vuelve al primero. Un escaneo registra
el estado del punto y orienta la visita siguiente. No elimina obst�culos ni
modifica las lecturas. Las condiciones son inventadas para la simulaci�n.

## Ejecutar

En la primera computadora, desde esta carpeta:

```powershell
.\Iniciar-Principal.ps1 -Puerto 1099 -IpLocal 192.168.1.10
```

En la segunda computadora, con la misma carpeta completa (incluido `lib/jade.jar`):

```powershell
.\Iniciar-Rover.ps1 -HostPrincipal 192.168.1.10 -Puerto 1099 -IpLocal 192.168.1.11
```

Sustituir ambas IP por las direcciones reales de cada m�quina. Para una prueba
en una sola computadora, iniciar el principal sin `-IpLocal` y luego el rover
con `-HostPrincipal localhost`. Hay que probar primero la conexi�n y las reglas
del firewall en la red real de la exposici�n.

El par�metro `-Local` del principal crea el Rover en la **misma** m�quina y
sirve para practicar. Para demostrar el apartado distribuido, inicia el
principal sin `-Local` y ejecuta `Iniciar-Rover.ps1` en la otra computadora.
Usa un puerto fijo, por ejemplo 1099. `-Puerto 0` asigna un puerto aleatorio
y por ello no sirve como direcci�n estable para el segundo equipo.

### Comprobaci�n presencial de dos equipos

1. En ambos equipos ejecuta `ipconfig` y apunta la direcci�n IPv4 de la red
   compartida. Evita `127.0.0.1`, que solo identifica a la propia computadora.
2. Copia `rover-marte` y la carpeta hermana `lib` con `jade.jar` a ambos
   equipos. Usa el mismo c�digo en ambas copias.
3. En la primera laptop ejecuta `Iniciar-Principal.ps1 -Puerto 1099
   -IpLocal IP_PRIMERA`. Deben aparecer Entorno y Coordinador.
4. En la segunda comprueba `Test-NetConnection IP_PRIMERA -Port 1099` y
   ejecuta `Iniciar-Rover.ps1 -HostPrincipal IP_PRIMERA -Puerto 1099
   -IpLocal IP_SEGUNDA`. En la ventana del contenedor debe aparecer Rover.
5. En la consola pulsa `Consultar rover`. Debe responder bater�a 80. Despu�s
   lanza B y comprueba el mismo folio `M-1` en las dos terminales. Guarda
   una captura de cada equipo y el tiempo que aparece en la consola.

Si el puerto es accesible pero JADE no une los contenedores, comprueba que
`-IpLocal` coincida con la direcci�n IPv4 de cada equipo y que el firewall
permita Java en esa red. Algunas redes universitarias a�slan equipos entre s�.
En ese caso pod�is conectar ambas laptops a una red privada compartida y
repetir la prueba antes de la exposici�n.

## Mensajes principales

| Origen  destino | Performativa | Contenido |
| --- | --- | --- |
| Entorno  Rover | `REQUEST` | Explorar un sector |
| Rover  Entorno | `QUERY_REF` | Medir ese sector |
| Entorno  Rover | `INFORM` | Observaci�n del terreno |
| Rover  Coordinador | `REQUEST` | `Observacion` contiene una `Lectura` con su `Sector` |
| Coordinador  Rover | `PROPOSE` | `Propuesta` con `Lectura` anidada |
| Rover  Coordinador | `ACCEPT_PROPOSAL` / `REJECT_PROPOSAL` | La propuesta evaluada |
| Rover  Entorno y Coordinador | `INFORM` | `Resultado` con `Lectura` anidada |
| Entorno  Rover | `REQUEST` | Consulta de estado |
| Rover  Entorno | `INFORM` | `EstadoRover` |

Todos registran `OntologiaMarte` y `SLCodec`. Cada misi�n tiene un
`conversation-id` como `M-1`. La consola muestra los pasos y el tiempo desde
la petici�n hasta el resultado. Las cifras para el reporte deber�n proceder
de pruebas realmente ejecutadas.
## Primer ejercicio en una laptop

Desde la carpeta rover-marte, ejecuta en PowerShell:

    .\Iniciar-Principal.ps1 -Local -IpLocal 127.0.0.1

Si Windows restringe scripts locales, desde esa carpeta usa:

    powershell -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Principal.ps1 -Local -IpLocal 127.0.0.1

La consola inicia con 80 unidades. Pulsa Consultar rover. Despu�s ejecuta un turno por sector en este orden: A, C, B, B, B, B. Anota la propuesta y la acci�n final. En el �ltimo turno el rover llega a base con 21 y se recarga a 80. Para repetir desde el principio det�n JADE con Ctrl+C y vuelve a iniciar.

## Defensa en equipo

Cada integrante hace el recorrido completo. En la siguiente ronda cambien qui�n opera, qui�n predice, qui�n se�ala el mensaje, qui�n ubica la regla en Java y qui�n diagnostica un fallo.

1. Visita C tres veces desde el inicio. Con bater�a suficiente, el polvo pasa de 85 a 72 y luego a 55. Explica por qu� solo la tercera visita permite tomar muestra.
2. Explora A dos veces. En el segundo punto hay se�al 70 y un obst�culo. Explica por qu� R2 tiene prioridad sobre R3.
3. �Por qu� puede cambiar el resultado si se repite B? La se�al de los tres puntos de B supera el umbral, pero la bater�a pertenece al Rover. Con 35 puede tomar muestra y conservar 20. Con 34 debe regresar.
4. Localiza Observacion  Lectura  Sector en modelo/. Sector y Lectura son conceptos anidables. Observacion es el predicado que viaja en el mensaje. Codificaci�n y lectura est�n en Mensajes.java.
5. Desconecta el proceso Rover en el ensayo con dos equipos. Inicia una misi�n. A los ocho segundos Entorno libera el bot�n. Comprueba host, puerto y conexi�n del contenedor, vuelve a iniciar Rover, pulsa Consultar rover y lanza otra misi�n. El reinicio restablece sus recursos.
6. �Qu� significa el tiempo de la consola? Desde enviar REQUEST hasta recibir el resultado con el mismo identificador, medido en Entorno. Incluye los 700 ms de operaci�n simulada. Un cron�metro en cada m�quina introducir�a diferencias de reloj.

## D�a de la prueba de red

Conecta ambas laptops a la misma red y clona el repositorio rover-marte, que incluye lib/jade.jar a cada una. En el principal usa ipconfig para obtener su IPv4, inicia con -IpLocal y esa IPv4. En el segundo equipo usa Test-NetConnection IP_DEL_PRINCIPAL -Port 1099 y despu�s Iniciar-Rover.ps1. Si aparece un aviso de Windows para Java en su red privada, revisen la autorizaci�n de red de ambas laptops. Confirmen bater�a con la consulta directa antes de la primera misi�n. Anoten equipo, hora, sector, acci�n y tiempo, y tomen evidencia del contenedor remoto y la consola con el mismo identificador de misi�n.


## Ensayo remoto con Hamachi

Ambos integrantes instalan Hamachi en sus computadoras y encienden el cliente. Uno crea una red mesh en **Network > Create a new network**, elige un ID y una contrase�a y se los comunica al compa�ero por privado. El compa�ero usa **Network > Join an existing network** con esos datos. Cada uno copia su IPv4 virtual de Hamachi, de la forma 25.x.x.x.

Ambos clonan este repositorio y necesitan un JDK. La carpeta `lib/jade.jar` ya est� incluida. Detengan cualquier sesi�n local anterior con Ctrl+C. En la computadora de �ngel (sustituyendo la IP por la suya de Hamachi):

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Principal.ps1 -Puerto 1099 -IpLocal 25.A.A.A -JadeGui
```

Se abren la consola de misi�n y la administraci�n de JADE. El compa�ero, en su computadora, comprueba el puerto y lanza el contenedor perif�rico (sustituyendo las dos IP):

```powershell
Test-NetConnection 25.A.A.A -Port 1099
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\Iniciar-Rover.ps1 -HostPrincipal 25.A.A.A -Puerto 1099 -IpLocal 25.B.B.B -PuertoLocal 1100
```

La computadora remota muestra `[rover] Listo` en su terminal. La consola Swing solo aparece en la computadora principal. La administraci�n de JADE de �ngel debe listar el contenedor perif�rico y `rover`. Pulsar **Consultar rover** debe devolver bater�a 80. Iniciar B debe producir el mismo folio `M-1` en las dos terminales y una muestra con bater�a 65. Capturen ambas pantallas y el tiempo de la consola para la evidencia distribuida.

No usen `-Local` en el principal ni `-Puerto 0` en esta prueba. `127.0.0.1` apunta a cada propia computadora. Si falla `Test-NetConnection`, revisen que Hamachi muestre a ambos conectados, las IP y el firewall de Java para el puerto 1099. Si conecta pero los mensajes no vuelven, comprueben tambi�n el puerto 1100 y que `-IpLocal` del Rover sea su IP Hamachi. Eviten desactivar completamente el firewall.

JADE 4.6.0 est� disponible bajo LGPL. Se incluye su archivo `lib/jade.jar` para la pr�ctica. Documentaci�n y proyecto original: https://jade.tilab.com/
