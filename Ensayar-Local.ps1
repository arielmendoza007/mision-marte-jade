$ErrorActionPreference = 'Stop'
$raiz = $PSScriptRoot
& "$raiz\Compilar.ps1"
$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $java) {
    $java = Get-ChildItem -Path "$env:USERPROFILE\.vscode\extensions" -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $java) { throw 'No se encontró java. Instala un JDK.' }
Push-Location -LiteralPath $raiz
try {
    & $java '-Dfile.encoding=UTF-8' -cp "$raiz\out;$raiz\lib\jade.jar" EnsayoConsola
    if ($LASTEXITCODE -ne 0) { throw 'El ensayo local no terminó correctamente.' }
} finally { Pop-Location }
