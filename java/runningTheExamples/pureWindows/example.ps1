

function DoExports {
    param(
        [string]$LibSrcPath
    )

    $env:ROOT_PATH_WINLIKE = "D:\cygwin64"
    $env:C_ROOT_PATH_UNIXLIKE = "/c/"
    $env:UNAME = ""

    Get-ChildItem Env: | Where-Object { $_.Name -match "^(UNAME|ROOT_PATH_WINLIKE|C_ROOT_PATH_UNIXLIKE)$" } | ForEach-Object {
        Write-Host "$($_.Name)=$($_.Value)"
    }
}

function ResolveFileParentDir {
    param(
        [string]$Prg
    )

    $currentPrg = $Prg

    # Handle symlinks manually to match the logic of the original while loop
    while (Test-Path -Path $currentPrg -ItemType SymbolicLink) {
        $target = (Get-Item -Path $currentPrg).Target
        if ([System.IO.Path]::IsPathRooted($target)) {
            $currentPrg = $target
        }
        else {
            $dir = Split-Path -Path $currentPrg -Parent
            $currentPrg = [System.IO.Path]::Combine($dir, $target)
        }
    }

    return Split-Path -Path $currentPrg -Parent
}

$java = "java"

# $0 in Bash is the script path. In PowerShell, we use $PSCommandPath or $MyInvocation.MyCommand.Definition
$scriptDir = ResolveFileParentDir -Prg $PSCommandPath
$targetDirName = Join-Path -Path $scriptDir -ChildPath "../../cygpath-lib-example/target/"

DoExports

$jarname = Join-Path -Path $targetDirName -ChildPath "./cygpath-lib-example-v1.0-SNAPSHOT-all.jar"

# Execute python
& $java -jar $jarname

