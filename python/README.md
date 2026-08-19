# cygpath command emulation lib for Python

## Index

- [cygpath command emulation lib for Python](#cygpath-command-emulation-lib-for-python)

  * [Index](#index)

  * [Python version](#python-version)

  * [Description](#description)

  * [Structure](#structure)
    - [api](#api)

  * [api implementations](#api-implementations)
    - [cygpath emulation implementations](#cygpath-emulation-implementations)
    - [facade implementations](#facade-implementations)

  * [Usage](#usage)
    - [Setting the environment](#setting-the-environment)
      * [Library custom variables](#library-custom-variables)
      * [PYTHONPATH environment variable](#pythonpath-environment-variable)
      * [A way to automate the environment setting](#a-way-to-automate-the-environment-setting)
    - [How to use the facade library in your Python app](#how-to-use-the-facade-library-in-your-python-app)

  * [Running the examples for all foreseen system types](#running-the-examples-for-all-foreseen-system-types)


## Python version

It has been tested under a Python 3.10 and a Python 3.12 version.


## Description

The library is self contained, and does not include any other dependency different from some Python standard ones.

Its aim is to emulate the **cygpath** command, available in **cygwin** and **git-bash** environments.

Some different implementations are being shared in the library. You will have to choose the one most suitable one for your environment.

The internals of the library are hidden by a facade, which is available for some different environment setups.

For promoting compatibility and reusage of the scripts which are going to use the library, that facade is being provided also for **pure unix** and for **pure windows** systems.

You should choose the most suitable facade for your system.


## Structure

The library is divided into some common code placed in a common dir, which is shared among the different implementations.
```
repo_root/python/lib_src/cygpath_lib
```

The different implementations themselves.
```
repo_root/lib_src/cygpath_lib/impl/unix_like
repo_root/lib_src/cygpath_lib/impl/windows_like
```

And the facades to be used as dependency, which hide the library internal implementation:
```
repo_root/lib_src/facade/cygwin_guestlike
repo_root/lib_src/facade/cygwin_hostlike
repo_root/lib_src/facade/transparent
```

A possible way for the library directories to be available for your applications, would be to set appropriately the **PYTHONPATH** environment variable for including the sources in the common directory, the directory having the implementation, and the facade implementation you are going to use.


### api

cygpath emulation is represented at `cygpath_wrapper.py` in BaseCygpath interface
```python
    def posix2win(self, path, absolute=False):
    def win2posix(self, path, absolute=False):
```
There are two implementations which try to emulate cygpath function

There is also a facade (in `sys_path_wrapper.py`) which wraps the cygpath emulation and offers some more functions:
```python
def win2posix(my_path: str, absolute: bool=False):
def posix2win(my_path: str, absolute: bool=False):
def host_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
def sys_to_host_path(system_like_path: str, absolute: bool=False) -> str:
def python_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
def sys_to_python_path(system_like_path: str, absolute: bool=False) -> str:
```
There are three implementations, 


## api implementations

[cygpath emulation implementations](#cygpath-emulation-implementations)

[facade implementations](#facade-implementations)


### cygpath emulation implementations

There are two implementations of BaseCygpath which depend on:

- **Python interpreter** for which the library is going to be used:
   * **Windows like** Python interpreter (theoretically only possible on either **cygwin** or **git-bash** or **pure windows** systems) (at `windows_like`)
       - like anaconda ones
       - or any installed with a Windows like IDE
           * just like Pycharm
           * or Visual Studio Code
       - or directly installed in your Windows system, either with chocolatey or with any other app install wrapper or directly installed into your Windows system in another way)
   * **Unix like** Python interpreter (at `unix_like`)
       - the one included in cygwin (it has the setoff that at least I was not successful at installing new Python dependencies with pip, so it might be limited to using the originally installed ones)
       - any of the available in a pure unix like environment

Those implementations are located at:
```
repo_root/python/lib_src/cygpath_lib/impl/unix_like/cygpath_wrapper_impl.py
repo_root/python/lib_src/cygpath_lib/impl/windows_like/cygpath_wrapper_impl.py
```

### facade implementations

There are three implementations of the facade which might differ depending on the type of system:

- **System** on which the lib is going to be used:
   * **cygwin** or **git-bash**
      - Implementation for unix like Python interpreter (at `cygwin_guestlike`)
      - Implementation for windows like Python interpreter (at `cygwin_hostlike`)
   * **pure unix-like** system (for instance, Linux or Mac or the Windows subsystem for Unix) (_only tested on a pure Linux one_) (at `transparent`)
   * **pure windows** system (at `transparent`)

The implementations are located at:
```
repo_root/python/lib_src/facade/cygwin_guestlike/sys_path_wrapper.py
repo_root/python/lib_src/facade/cygwin_hostlike/sys_path_wrapper.py
repo_root/python/lib_src/facade/transparent/sys_path_wrapper.py
```


## Usage

  - [Setting the environment](#setting-the-environment)
    * [Library custom variables](#library-custom-variables)
    * [PYTHONPATH environment variable](#pythonpath-environment-variable)
    * [A way to automate the environment setting](#a-way-to-automate-the-environment-setting)
  - [How to use the facade library in your Python app](#how-to-use-the-facade-library-in-your-python-app)


Previously to be able to use the library, you will have to prepare your environment, just as it is [explained here](#setting-the-environment)

### Setting the environment

Some environment variables must be set

#### Library custom variables

First, you will have to export the library custom environment variables.

The variables that the library expects to be set are the following:
- **C_ROOT_PATH_UNIXLIKE**
- **ROOT_PATH_WINLIKE**

Which might take different values depending on the environment for which you are going to use the library.

An example of those values (for a cygwin system in this case), could be:

```console
export C_ROOT_PATH_UNIXLIKE='/cygdrive/c/'
export ROOT_PATH_WINLIKE='C:\cygwin64'
```

You do not have to know the literal values with which to set those variables. [See how to](#a-way-to-automate-the-environment-setting)


#### PYTHONPATH environment variable

You will have to indicate the Python interpreter the directories in which the library code is.

A way to do that, would be by setting the **PYTHONPATH** variable.

It has the same format as the one in PATH variable.

That means that the PATH delimiter may be different depending on if the Python interpreter to be used is windows like or linux like.

path delimiter:
- **:**  to be used for PYTHONPATH value, if a **linux** like python interpreter is going to be used.
- **;**  to be used for PYTHONPATH value, if a **windows** like python interpreter is going to be used.

An example for that value would be (for a cygwin system using its Unix like Python interpreter in this case) (":" delimiter):
```
PYTHONPATH=/absolute_path_to_repo_root/python/lib_src/cygpath_lib:/absolute_path_to_repo_root/python/lib_src/cygpath_lib/impl/unix_like:/absolute_path_to_repo_root/python/lib_src/facade/cygwin_guestlike
```

You can see a example for setting that environment variable for any of the foreseen environments. [See how to](#a-way-to-automate-the-environment-setting)


#### A way to automate the environment setting

The environment setting can be automated at least for the following kind of systems:

You can see the examples, at:

```
repo_root/python/examples_src/cygwin_or_gitbash_with_windows_python
repo_root/python/examples_src/cygwin_with_cygwin_python
repo_root/python/examples_src/pure_unix_like_environment
repo_root/python/examples_src/pure_windows_environment
```

An example of the automations for those systems can be found at:

```
repo_root/python/examples_src/cygwin_or_gitbash_with_windows_python/doExports.sh
repo_root/python/examples_src/cygwin_with_cygwin_python/doExports.sh
repo_root/python/examples_src/pure_unix_like_environment/doExports.sh
repo_root/python/examples_src/pure_windows_environment/example.ps1
```

### How to use the facade library in your Python app

For using the library, you first should set the environment as explained at the above sections (which adds the chosen facade implementation directory in the PYTHONPATH env variable)

Once that is done, you can simply use this import in your app, and use its functions:

```python
import sys_path_wrapper as sys_w

def win2posix(my_path: str, absolute: bool=False):
def posix2win(my_path: str, absolute: bool=False):
def host_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
def sys_to_host_path(system_like_path: str, absolute: bool=False) -> str:
def python_to_sys_path(python_like_path: str, absolute: bool=False) -> str:
def sys_to_python_path(system_like_path: str, absolute: bool=False) -> str:
```

There are some different functions available (which might behave differently depending on the implementation you chose)
All functions are expected to be idempotent.
- **win2posix**. For converting a windows like path into a unix like path compatible with your system.
- **posix2win**. For converting a unix like path compatible with your system into a windows like path.
- **host_to_sys_path**. For converting a host like path (windows in case of cygwin or git-bash, or no change otherwise) into a system like path (expected to be unix like) compatible with your system.
- **sys_to_host_path**. For converting a system like path (expected to be unix like) into a host like path (windows in case of cygwin or git-bash, or no change otherwise).
- **python_to_sys_path**. For converting a Python like path (windows in case of a windows like Python interpreter, or no change otherwise) into a system like path (expected to be unix like) compatible with your system.
- **sys_to_python_path**. For converting a system like path (expected to be unix like) into a Pythonn like path (windows in case of a windows like Python interpreter, or no change otherwise).

You can [see the examples](#running-the-examples-for-all-foreseen-system-types)



## Running the examples for all foreseen system types


You can run the suitable example for your system.

Choose the suitable one among the following:
```
repo_root/python/examples_src/cygwin_or_gitbash_with_windows_python/example.sh
repo_root/python/examples_src/cygwin_with_cygwin_python/example.sh
repo_root/python/examples_src/pure_unix_like_environment/example.sh
repo_root/python/examples_src/pure_windows_environment/example.ps1
```


