# Visual Studio Code — abrir y ejecutar

1. Extrae el ZIP y abre la carpeta **Olympus-Mons-VSCode** con `Archivo > Abrir carpeta`.
2. Instala **Extension Pack for Java** de Microsoft si no lo tienes.
3. Necesitas JDK 17 o superior, y el archivo `lib/jade.jar` ya incluido.
4. En la pestaña **Ejecutar y depurar**, elige **Olympus Mons - Local (3 agentes)** y pulsa Iniciar.
5. También puedes ejecutar el script ya incluido desde una terminal PowerShell:

```powershell
.\Iniciar-Principal.ps1 -Local -IpLocal 127.0.0.1 -Puerto 1099 -JadeGui
```

Para prueba distribuida, sigue las instrucciones de `README.md` con los scripts
`Iniciar-Principal.ps1` y `Iniciar-Rover.ps1` desde dos computadoras.

Para probar los escenarios automáticamente, usa `Ensayar-Local.ps1`.

Si el puerto 1099 ya está ocupado, cierra las otras instancias de JADE o cambia el
puerto en la configuración de ejecución.
