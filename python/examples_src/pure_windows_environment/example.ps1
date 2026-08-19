# based on a conversion by:
# https://app.codeconvert.ai/code-converter


function ErrLog {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]]$Messages
    )
    foreach ($message in $Messages) {
        [Console]::Error.WriteLine($message)
    }
}

function ExistingDirAbsName {
    param(
        [string]$ExistingDirName
    )
    # The original script used cygpath -wa which returns a Windows-style absolute path.
    # In PowerShell 7 on Linux, we resolve the full path.
    if (Test-Path -Path $ExistingDirName -PathType Container) {
        return (Resolve-Path -Path $ExistingDirName).Path
    }
    else {
        # If it doesn't exist, we still try to get the full path as cygpath might
        return [System.IO.Path]::GetFullPath($ExistingDirName)
    }
}

function DoExports {
    param(
        [string]$LibSrcPath
    )

    $env:ROOT_PATH_WINLIKE = "D:\cygwin64"
    $env:C_ROOT_PATH_UNIXLIKE = "/c/"

    $cygpathLibPath = ExistingDirAbsName -ExistingDirName "$LibSrcPath/cygpath_lib"
    $cygpathImplLibPath = ExistingDirAbsName -ExistingDirName "$LibSrcPath/cygpath_lib/impl/windows_like"
    $sysPathWrapperPath = ExistingDirAbsName -ExistingDirName "$LibSrcPath/facade/transparent"

    # ; windows like delimiter as requested in original comments
    $env:PYTHONPATH = "$cygpathLibPath;$cygpathImplLibPath;$sysPathWrapperPath"

    Get-ChildItem Env: | Where-Object { $_.Name -match "^(PYTHONPATH|ROOT_PATH_WINLIKE|C_ROOT_PATH_UNIXLIKE)$" } | ForEach-Object {
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

# Main execution logic
if ($args.Count -ne 1) {
    $scriptName = $MyInvocation.MyCommand.Name
    ErrLog "Usage: '$scriptName' pythonInterpreterPath/python3.exe"
    ErrLog "Example: '$scriptName' 'C:\Users\myUser\anaconda3\python.exe'"
    exit 1
}

$python3 = $args[0]

# $0 in Bash is the script path. In PowerShell, we use $PSCommandPath or $MyInvocation.MyCommand.Definition
$scriptDir = ResolveFileParentDir -Prg $PSCommandPath

# prepare environment
$libSrcPath = Join-Path -Path $scriptDir -ChildPath "../../lib_src"
DoExports -LibSrcPath $libSrcPath

$examplePyPath = Join-Path -Path $scriptDir -ChildPath "../example.py"

# Execute python
& $python3 $examplePyPath

