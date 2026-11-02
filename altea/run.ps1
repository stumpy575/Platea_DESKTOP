function Get-LatestVersion {
    param (
        [string]$moduleName
    )

    # Find the JAR files and sort them by version while excluding javadoc and sources
    $jarFiles = Get-ChildItem -Path "$env:USERPROFILE\.m2\repository\org\openjfx" -Recurse -File |
                Where-Object { $_.Name -like "${moduleName}-*.jar" -and $_.Name -notmatch "(javadoc|sources)" } |
                Sort-Object Name -Descending

    # Return the latest version's path
    if ($jarFiles.Count -gt 0) {
        return $jarFiles[0].FullName
    } else {
        return $null
    }
}

$fxBasePath = Get-LatestVersion "javafx-base"
$fxControlsPath = Get-LatestVersion "javafx-controls"
$fxFxmlPath = Get-LatestVersion "javafx-fxml"
$fxGraphicsPath = Get-LatestVersion "javafx-graphics"

# Comprova que s'han trobat els quatre mòduls
if (-not ($fxBasePath -and $fxControlsPath -and $fxFxmlPath -and $fxGraphicsPath)) {
    Write-Host "No es pot trobar el mòdul JavaFX al repositori Maven local."
    exit 1
}

$fxPath = "$fxBasePath;$fxControlsPath;$fxFxmlPath;$fxGraphicsPath"

# Opcions comunes per a MAVEN_OPTS
$env:MAVEN_OPTS = "--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --module-path $fxPath --add-modules javafx.controls,javafx.fxml,javafx.graphics"

# Classe principal: el primer argument, o com.project.Main per defecte
$mainClass = if ($args.Count -gt 0) { $args[0] } else { "com.project.Main" }

Write-Output "Setting MAVEN_OPTS to: $env:MAVEN_OPTS"
Write-Output "Main Class: $mainClass"

# Arguments per a Maven
$execArgs = @("-PrunMain", "-Dexec.mainClass=$mainClass")

Write-Output "Exec args: $($execArgs -join ' ')"

# Execute mvn command
mvn clean test-compile exec:java $execArgs