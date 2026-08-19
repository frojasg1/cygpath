#!/bin/bash

function errLog()
{
    printf "%s\n" "$@" >&2
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
    errLog "Usage: '$0' '/python/interpreter/path/python3.exe'"
    errLog "Example '$0' /cygdrive/c/Users/my_user/anaconda3/python.exe"
    exit 1
fi

PYTHON_EXE="$1"

SCRIPT_DIR="$( resolveFileParentDir "$0" )"

# prepare environment (it can be placed at .bash_profile)
LIB_SCR_PATH="$SCRIPT_DIR/../../lib_src"
source "$SCRIPT_DIR/doExports.sh" "$LIB_SCR_PATH"


"$PYTHON_EXE" "$SCRIPT_DIR/../example.py"


