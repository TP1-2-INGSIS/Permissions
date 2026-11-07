$ErrorActionPreference = 'Stop'

$repoRoot = (git rev-parse --show-toplevel).Trim()
if ($LASTEXITCODE -ne 0 -or -not $repoRoot) {
    throw 'No se pudo encontrar la raíz del repositorio Git.'
}

git -C $repoRoot config core.hooksPath .githooks
if ($LASTEXITCODE -ne 0) {
    throw 'No se pudo configurar core.hooksPath.'
}

Write-Output 'Hooks activados desde .githooks.'
