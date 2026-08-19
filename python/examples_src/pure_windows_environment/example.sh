#!/bin/bash

function errLog()
{
    printf '%s\n' "$@" >&2
}

function existingDirAbsName()
{
    local __EXISTING_DIR_NAME__="$1"
#    ( cd "$__EXISTING_DIR_NAME__" && pwd )
    cygpath -wa "$__EXISTING_DIR_NAME__"
}


function doExports()
{
    local __LIB_SCR_PATH__="$1"

    export ROOT_PATH_WINLIKE="D:\\cygwin64"
    export C_ROOT_PATH_UNIXLIKE="/c/"


    local __CYGPATH_LIB_PATH__="$( existingDirAbsName "$__LIB_SCR_PATH__/cygpath_lib" )"
    local __CYGPATH_IMPL_LIB_PATH__="$( existingDirAbsName "$__LIB_SCR_PATH__/cygpath_lib/impl/windows_like" )"
    local __SYS_PATH_WRAPPER_PATH__="$( existingDirAbsName "$__LIB_SCR_PATH__/facade/transparent" )"

# ;  windows like delimiter
    export PYTHONPATH="$__CYGPATH_LIB_PATH__;$__CYGPATH_IMPL_LIB_PATH__;$__SYS_PATH_WRAPPER_PATH__"

    env | grep -E "^(PYTHONPATH|ROOT_PATH_WINLIKE|C_ROOT_PATH_UNIXLIKE)="
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


if (( $# != 1 ))
then
    errLog "Usage: '$0' pythonInterpreterPath/python3.exe"
    errLog "Example: '$0' 'C:\\Users\\myUser\\anaconda3\\python.exe'"
    exit 1
fi

PYTHON3="$1"

SCRIPT_DIR="$( resolveFileParentDir "$0" )"

# prepare environment
LIB_SCR_PATH="$SCRIPT_DIR/../../lib_src"
doExports "$LIB_SCR_PATH"


"$PYTHON3" "$SCRIPT_DIR/../example.py"


