#!/bin/bash


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

# prepare environment (it can be placed at .bash_profile)
LIB_SCR_PATH="$SCRIPT_DIR/../../lib_src"
source "$SCRIPT_DIR/doExports.sh" "$LIB_SCR_PATH"


python3 "$SCRIPT_DIR/../example.py"


