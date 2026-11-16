param([Parameter(Mandatory=$true)][string]$HostPrincipal, [int]$Puerto = 1099,
      [string]$IpLocal = '', [int]$PuertoLocal = 1100)
$raiz = $PSScriptRoot
& "$raiz\Compilar.ps1"
$java = (Get-Command java -ErrorAction SilentlyContinue).Source
if (-not $java) {
    $java = Get-ChildItem -Path "$env:USERPROFILE\.vscode\extensions" -Recurse -Filter java.exe -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $java) { throw 'No se encontró java.' }
$opciones = @('-container', '-host', $HostPrincipal, '-port', "$Puerto", '-local-port', "$PuertoLocal")
if ($IpLocal) { $opciones += @('-local-host', $IpLocal) }
Push-Location -LiteralPath $raiz
try {
    & $java '-Dfile.encoding=UTF-8' -cp "$raiz\lib\jade.jar;$raiz\out" jade.Boot @opciones 'rover:marte.agentes.Rover'
} finally { Pop-Location }
