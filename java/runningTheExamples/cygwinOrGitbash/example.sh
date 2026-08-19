#!/bin/bash


function toAbsoluteWindowsFileName()
{
    cygpath -wa "$@" #| reescape
}

function toAbsoluteUnixFileName()
{
    cygpath -ua "$@"
}

function doExports()
{
    export ROOT_PATH_WINLIKE="$( toAbsoluteWindowsFileName "/" )"
    export C_ROOT_PATH_UNIXLIKE="$( toAbsoluteUnixFileName 'C:\' )"
    export UNAME="$( uname -a )"
}


function resolveFileParentDir()
{
    local __PRG__="$1"

    while [ -h "$__PRG__" ]; do
      local ls=`ls -ld "$__PRG__"`
      local link=`expr "$ls" : '.*-> \(.*\)$'`
      if expr "$link" : '/.*' > /dev/null; then
        __PRG__="$link"
      else
        __PRG__=`dirname "$__PRG__"`/"$link"
      fi
    done

    dirname "$__PRG__"
}


SCRIPT_DIR="$( resolveFileParentDir "$0" )"
TARGET_DIRNAME="$SCRIPT_DIR/../../cygpath-lib-example/target/"

doExports
env | grep -E "^(ROOT_PATH_WINLIKE|C_ROOT_PATH_UNIXLIKE|UNAME)="

ABS_WIN_JARNAME="$( toAbsoluteWindowsFileName "$TARGET_DIRNAME/cygpath-lib-example-v1.0-SNAPSHOT-all.jar" )"
java -jar "$ABS_WIN_JARNAME"

