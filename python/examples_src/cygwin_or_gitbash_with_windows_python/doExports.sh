

function errLog()
{
    printf "%s\n" "$@" >&2
}

function reescape()
{
    sed -e 's/\\/\\\\/g'
}

function toWindowsFileName()
{
    cygpath -w "$@" #| reescape
}

function toAbsoluteWindowsFileName()
{
    cygpath -wa "$@" #| reescape
}

function toUnixFileName()
{
    cygpath -u "$@"
}

function toAbsoluteUnixFileName()
{
    cygpath -ua "$@"
}

function toHostPath()
{
    toWindowsFileName "$@"
}

function toHostAbsPath()
{
    toAbsoluteWindowsFileName "$@"
}

function toGuestPath()
{
    toUnixFileName "$@"
}

function toGuestAbsPath()
{
    toAbsoluteUnixFileName "$@"
}

if (( $# != 1 ))
then
    errLog "Expected to receive an argument with lib_src path"
    return 1
fi

LIB_SCR_PATH="$1"

export ROOT_PATH_WINLIKE="$( toAbsoluteWindowsFileName "/" )"
export C_ROOT_PATH_UNIXLIKE="$( toAbsoluteUnixFileName 'C:\' )"


CYGPATH_LIB_PATH="$( toAbsoluteWindowsFileName "$LIB_SCR_PATH/cygpath_lib" )"
CYGPATH_LIB_IMPL_PATH="$( toAbsoluteWindowsFileName "$LIB_SCR_PATH/cygpath_lib/impl/windows_like" )"
SYS_PATH_WRAPPER_PATH="$( toAbsoluteWindowsFileName "$LIB_SCR_PATH/facade/cygwin_hostlike" )"

# :  unix like delimiter
export PYTHONPATH="$CYGPATH_LIB_PATH;$CYGPATH_LIB_IMPL_PATH;$SYS_PATH_WRAPPER_PATH"

env | grep -E "^(PYTHONPATH|ROOT_PATH_WINLIKE|C_ROOT_PATH_UNIXLIKE)="

