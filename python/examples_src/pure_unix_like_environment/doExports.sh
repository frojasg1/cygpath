

function errLog()
{
    printf "%s\n" "$@" >&2
}

function existingDirAbsName()
{
    local __EXISTING_DIR_NAME__="$1"
    ( cd "$__EXISTING_DIR_NAME__" && pwd )
}


if (( $# != 1 ))
then
    errLog "Expected to receive an argument with lib_src path"
    return 1
fi

LIB_SCR_PATH="$1"

export ROOT_PATH_WINLIKE="D:\\cygwin64"
export C_ROOT_PATH_UNIXLIKE="/c/"


CYGPATH_LIB_PATH="$( existingDirAbsName "$LIB_SCR_PATH/cygpath_lib" )"
CYGPATH_LIB_IMPL_PATH="$( existingDirAbsName "$LIB_SCR_PATH/cygpath_lib/impl/unix_like" )"
SYS_PATH_WRAPPER_PATH="$( existingDirAbsName "$LIB_SCR_PATH/facade/transparent" )"

# :  unix like delimiter
export PYTHONPATH="$CYGPATH_LIB_PATH:$CYGPATH_LIB_IMPL_PATH:$SYS_PATH_WRAPPER_PATH"

env | grep -E "^(PYTHONPATH|ROOT_PATH_WINLIKE|C_ROOT_PATH_UNIXLIKE)="

