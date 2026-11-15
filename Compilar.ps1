$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
if (-not $javac) {
    $jdk = Get-ChildItem -Path "$env:USERPROFILE\.vscode\extensions" -Recurse -Filter javac.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
    if (-not $jdk) { throw 'No se encontr� javac. Instala un JDK o abre el proyecto con la extensi�n Java de VS Code.' }
    $javac = $jdk
}
$archivos = @(Get-ChildItem -LiteralPath "$raiz\src" -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
$archivos += "$raiz\pruebas\EnsayoConsola.java"
New-Item -ItemType Directory -Force -Path "$raiz\out" | Out-Null
& $javac -encoding UTF-8 -cp "$raiz\lib\jade.jar" -d "$raiz\out" $archivos
if ($LASTEXITCODE -ne 0) { throw 'Fall� la compilaci�n.' }
Write-Host "Compilados $($archivos.Count) archivos Java." -ForegroundColor Green
