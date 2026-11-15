param([int]$Puerto = 1099, [string]$IpLocal = '', [switch]$Local, [switch]$JadeGui)
$raiz = $PSScriptRoot
& "$raiz\Compilar.ps1"
$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $java) {
    $java = Get-ChildItem -Path "$env:USERPROFILE\.vscode\extensions" -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $java) { throw 'No se encontr� java.' }
$opciones = @($(if ($JadeGui) { '-gui' } else { '-nomtp' }), '-port', "$Puerto")
if ($IpLocal) { $opciones += @('-local-host', $IpLocal) }
$agentes = 'entorno:marte.agentes.Entorno;coordinador:marte.agentes.Coordinador'
if ($Local) { $agentes += ';rover:marte.agentes.Rover' }
Push-Location -LiteralPath $raiz
try {
    & $java '-Dfile.encoding=UTF-8' -cp "$raiz\lib\jade.jar;$raiz\out" jade.Boot @opciones $agentes
} finally { Pop-Location }
