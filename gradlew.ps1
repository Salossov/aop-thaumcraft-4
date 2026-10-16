$ErrorActionPreference = 'Stop'
$taskRoot = $PSScriptRoot
$taskParent = Split-Path -Parent $taskRoot
$taskJava = $null
foreach ($taskJdkRoot in @((Join-Path $taskRoot '.tools\java21'), (Join-Path $taskParent '.tools\java21'))) {
    if (-not (Test-Path -LiteralPath $taskJdkRoot)) { continue }
    $taskJdk = Get-ChildItem -LiteralPath $taskJdkRoot -Directory |
        Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin\java.exe') } |
        Select-Object -First 1
    if ($null -ne $taskJdk) {
        $taskJava = Join-Path $taskJdk.FullName 'bin\java.exe'
        break
    }
}
if ($null -eq $taskJava -and $env:JAVA_HOME) {
    $taskCandidate = Join-Path $env:JAVA_HOME 'bin\java.exe'
    if (Test-Path -LiteralPath $taskCandidate) { $taskJava = $taskCandidate }
}
if ($null -eq $taskJava) {
    $taskJavaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($null -ne $taskJavaCommand) { $taskJava = $taskJavaCommand.Source }
}
if ($null -eq $taskJava) { throw 'Java 21 is missing. Set JAVA_HOME or install it in .tools/java21/<jdk>.' }
$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $taskJava)
$taskCacheRoot = Join-Path $taskRoot '.cache'
if (Test-Path -LiteralPath (Join-Path $taskParent '.cache\gradle')) {
    $taskCacheRoot = Join-Path $taskParent '.cache'
}
if (-not $env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME = Join-Path $taskCacheRoot 'gradle' }
$env:TEMP = Join-Path $taskRoot '.cache\tmp'
$env:TMP = $env:TEMP
$taskJavaHome = Join-Path $taskCacheRoot 'java-home'
New-Item -ItemType Directory -Force -Path $env:GRADLE_USER_HOME,$env:TEMP,$taskJavaHome | Out-Null
$taskWrapper = Join-Path $taskRoot 'gradle\wrapper\gradle-wrapper.jar'
Push-Location -LiteralPath $taskRoot
try {
    & $taskJava '-Xmx2G' "-Duser.home=$taskJavaHome" "-Dorg.gradle.jvmargs=-Xmx2G -Duser.home=$taskJavaHome" '-classpath' $taskWrapper 'org.gradle.wrapper.GradleWrapperMain' @args
    $taskExit = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $taskExit
