# ============================================================
#  CALCULO DE METRICAS DE PRODUCTO - Sistema Pre-Sustentaciones UTEQ
#  Ejecutar desde la raiz del proyecto con:
#     powershell -ExecutionPolicy Bypass -File metricas.ps1
# ============================================================

$rutaBackend  = 'Backendpresus\backend\backend-mod\src\main\java'
$rutaFrontend = 'frontendpresus\src'

Write-Host ""
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "        METRICAS DE PRODUCTO - PROYECTO PRE-SUSTENTACIONES         " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

# --- Recolectar archivos ---
$java = Get-ChildItem -Path $rutaBackend -Recurse -Filter *.java
$ts   = Get-ChildItem -Path $rutaFrontend -Recurse -Include *.ts -Exclude *.spec.ts
$html = Get-ChildItem -Path $rutaFrontend -Recurse -Include *.html
$css  = Get-ChildItem -Path $rutaFrontend -Recurse -Include *.css
$contJava = $java | Get-Content

# --- METRICA 1: Lineas de codigo (LOC) totales ---
$locJava = ($java | Get-Content | Measure-Object -Line).Lines
$locTs   = ($ts   | Get-Content | Measure-Object -Line).Lines
$locHtml = ($html | Get-Content | Measure-Object -Line).Lines
$locCss  = ($css  | Get-Content | Measure-Object -Line).Lines
$locTotal = $locJava + $locTs + $locHtml + $locCss
Write-Host "METRICA 1 - Lineas de codigo (LOC) totales" -ForegroundColor Yellow
Write-Host "   Total: $locTotal LOC  (Java: $locJava | TS: $locTs | HTML: $locHtml | CSS: $locCss)"
Write-Host ""

# --- METRICA 2: Numero de archivos de codigo ---
$archTotal = $java.Count + $ts.Count + $html.Count + $css.Count
Write-Host "METRICA 2 - Numero de archivos de codigo" -ForegroundColor Yellow
Write-Host "   Total: $archTotal archivos  (Java: $($java.Count) | TS: $($ts.Count) | HTML: $($html.Count) | CSS: $($css.Count))"
Write-Host ""

# --- METRICA 3: Numero de clases / interfaces / enums (backend) ---
$clases = ($contJava | Select-String -Pattern '\b(class|interface|enum)\s+\w+').Count
Write-Host "METRICA 3 - Clases / interfaces / enums (backend)" -ForegroundColor Yellow
Write-Host "   Total: $clases"
Write-Host ""

# --- METRICA 4: Numero de metodos (backend, aprox) ---
$metodos = ($contJava | Select-String -Pattern '(public|private|protected).*\w+\s*\(.*\)').Count
Write-Host "METRICA 4 - Metodos declarados (backend, aprox)" -ForegroundColor Yellow
Write-Host "   Total: $metodos"
Write-Host ""

# --- METRICA 5: Endpoints REST expuestos por el API ---
$endpoints = ($contJava | Select-String -Pattern '@(Get|Post|Put|Delete|Patch)Mapping').Count
Write-Host "METRICA 5 - Endpoints REST del API" -ForegroundColor Yellow
Write-Host "   Total: $endpoints"
Write-Host ""

# --- METRICA 6: Densidad de comentarios (backend) ---
$comentarios = ($contJava | Select-String -Pattern '^\s*(//|\*|/\*)').Count
$densidad = [math]::Round(($comentarios / $locJava) * 100, 2)
Write-Host "METRICA 6 - Densidad de comentarios (backend)" -ForegroundColor Yellow
Write-Host "   $comentarios lineas de comentario / $locJava LOC = $densidad %"
Write-Host ""

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " Calculo finalizado correctamente." -ForegroundColor Green
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""
