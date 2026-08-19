# cygpath command emulation for Java

## Index

- [cygpath command emulation for Java](#cygpath-command-emulation-for-java)

  * [Index](#index)

  * [Java version](#java-version)

  * [Description](#description)

  * [Structure](#structure)
    - [api](#api)

  * [Implementations](#implementations)
    - [cygpath emulation implementations](#cygpath-emulation-implementations)
    - [facade default implementation](#facade-default-implementation)

  * [Usage](#usage)
    - [Setting the environment](#setting-the-environment)
      * [Library custom variables](#library-custom-variables)
      * [A way to automate the environment setting](#a-way-to-automate-the-environment-setting)
    - [How to use the library default facade in your Java app](#how-to-use-the-library-default-facade-in-your-java-app)

  * [Running the examples for all foreseen system types](#running-the-examples-for-all-foreseen-system-types)

## Java version

The code is compliant with java-8 or above.

## Description

The library is almost self contained, only including external dependencies for logging and some jdk standard ones.

Its aim is to emulate the **cygpath** command, available in **cygwin** and **git-bash** environments.

Some different implementations are being shared in the library.

The internals of the library are hidden by a facade.

The facade chooses the implementatoin to be used in your application depending on some environment variable values.


## Structure

The library has some different parts:
- cygpath emulation
- facade (which hides the internals of the library)
- default singleton facade (to be used directly in your code)

The default singleton facade can be obtained like this:
```java
CygpathLibFacade.getFacade()
```

### api

cygpath emulation is represented in BaseCygpath interface:
```java
    String posix2win(String path, boolean absolute);
    String win2posix(String path, boolean absolute);
```
There are two implementations which try to emulate cygpath function

There is also a facade represented in SysPathWrapper interface which wraps the cygpath emulation and offers some more functions:
```java
    public String win2posix(String myPath, boolean absolute);
    public String posix2win(String myPath, boolean absolute);
    public String hostToSysPath(String hostLikePath, boolean absolute);
    public String sysToHostPath(String systemLikePath, boolean absolute);
    public String javaToSysPath(String javaLikePath, boolean absolute);
    public String sysToJavaPath(String systemLikePath, boolean absolute);
```
There is one default facade implementation which tries to infer the type of system which is being used and calls the appropriate functions taking that into account, which can be obtained by invoking: `CygpathLibFacade.getFacade()`

## Implementations

[cygpath emulation implementations](#cygpath-emulation-implementations)

[facade default implementations](#facade-default-implementations)


### cygpath emulation implementations

There are two implementations of BaseCygpath which depend on:

- **Java interpreter** for which the library is going to be used:
   * **Windows like** a windows Java JVM executor (theoretically only possible on either **cygwin** or **git-bash** or **pure windows** systems) (implementation at: CygpathStraightWindowsImpl class):
       - Normally all java JVM executors on those systems are windows like.
   * **Unix like** a windows Java JVM executor (implementation at: CygpathStraightUnixImpl class):
       - At a pure unix like environment

Those implementations are located at:
```
repo_root/java/cygpath-lib/src/main/java/com/frojasg1/cygpath/cygwin/unixlike/CygpathStraightUnixImpl.java
repo_root/java/cygpath-lib/src/main/java/com/frojasg1/cygpath/cygwin/windowslike/CygpathStraightWindowsImpl.java
```

### facade default implementation

There are three implementations of the facade which might differ depending on the type of system:

- **System** on which the lib is going to be used:
   * **cygwin** or **git-bash**
      - Implementation for unix like Python interpreter (at `cygwin_guestlike`)
      - Implementation for windows like Python interpreter (at `cygwin_hostlike`)
   * **pure unix-like** system (for instance, Linux or Mac or the Windows subsystem for Unix) (_only tested on a pure Linux one_) (at `transparent`)
   * **pure windows** system (at `transparent`)

The default facade implementation has wraps the cygpath emulation implementation, which is obtained by inspecting the System directory separator.

It also tries to infer the kind of system compared to the kind of java JVM which is being used.

That is done by inspecting the **UNAME** environment variable value, which is expected to have been set before calling the java jar using the library.

**UNAME** variable is expected to have the value of `uname -a` command output on unix like systems.

And an empty string "" on windows systems.

If it is not set, the library tries to continue, making the presumption that the system matches the kind of Java JVM which is being used (wrong presumption if it is being used under a cygwin or git-bash environment. In such cases that variable must be set previously for the library to work properly)


## Usage

  - [Setting the environment](#setting-the-environment)
    * [Library custom variables](#library-custom-variables)
    * [A way to automate the environment setting](#a-way-to-automate-the-environment-setting)
  - [How to use the library default facade in your Java app](#how-to-use-the-library-default-facade-in-your-java-app)

Previously to be able to use the library, you will have to prepare your environment, just as it is [explained here](#setting-the-environment)


### Setting the environment

Some environment variables must be set

#### Library custom variables

First, you will have to export the library custom environment variables.

The variables that the library expects to be set are the following:
- **C_ROOT_PATH_UNIXLIKE**
- **ROOT_PATH_WINLIKE**
- **UNAME**

Which might take different values depending on the environment for which you are going to use the library.

An example of those values (for a cygwin system in this case), could be:

```console
export C_ROOT_PATH_UNIXLIKE='/cygdrive/c/'
export ROOT_PATH_WINLIKE='C:\cygwin64'
export UNAME="$( uname -a )"
```

For pure **windows** environments, **UNAME** should be set with an empty value, as for instance, in a power shell script (?):
```console
$env:UNAME = ""
```

You do not have to know the literal values with which to set those variables. [See how to](#a-way-to-automate-the-environment-setting)


#### A way to automate the environment setting

The way to set the environment variables is just the same as the ones in the examples:

```
repo_root/java/runningTheExamples/cygwinOrGitbash/example.sh
repo_root/java/runningTheExamples/pureUnixlike/example.sh
repo_root/java/runningTheExamples/pureWindows/example.ps1
repo_root/java/runningTheExamples/pureWindows/example.sh
```

This Java library expects **UNAME** environment variable be set this way:

- For unix like systems:
```console
export UNAME="$( uname -a )"
```
- For pure windows systems, it should be assigned with an empty value (zero length string):
```console
$env:UNAME = ""
```


### How to use the library default facade in your Java app

The library functionality is wrapped in a default Singleton class.

First of all, you have to obtain the singleton Object, which is of type: `SysPathWrapper`

And then, call the appropriate function with your parameters.

Just like this:

```java
    SysPathWrapper instance = CygpathLibFacade.getFacade();
    String myPath = "........";
    boolean absolute = xxxx;
    String pathResult = null;
    
    pathResult = instance.win2posix(myPath, absolute);
    pathResult = instance.posix2win(myPath, absolute);
    pathResult = instance.hostToSysPath(myPath, absolute);
    pathResult = instance.sysToHostPath(myPath, absolute);
    pathResult = instance.javaToSysPath(myPath, absolute);
    pathResult = instance.sysToJavaPath(myPath, absolute);
```

There are some different functions available (which might behave differently depending on the implementation you chose)

All functions are expected to be idempotent.
- **win2posix**. For converting a windows like path into a unix like path compatible with your system. wrap of cygpath emulation
- **posix2win**. For converting a unix like path compatible with your system into a windows like path. wrap of cygpath emulation
- **hostToSysPath**. For converting a host like path (windows in case of cygwin or git-bash, or no change otherwise) into a system like path (expected to be unix like) compatible with your system.
- **sysToHostPath**. For converting a system like path (expected to be unix like) into a host like path (windows in case of cygwin or git-bash, or no change otherwise).
- **javaToSysPath**. For converting a Java like path (windows in case of a windows like Java interpreter, or no change otherwise) into a system like path (expected to be unix like) compatible with your system.
- **sysToJavaPath**. For converting a system like path (expected to be unix like) into a Java like path (windows in case of a windows like Java interpreter, or no change otherwise).

You can [see the examples](#running-the-examples-for-all-foreseen-system-types)



## Running the examples for all foreseen system types

You can run the suitable example for your system.

Choose the suitable one among the following:
```
repo_root/java/runningTheExamples/cygwinOrGitbash/example.sh
repo_root/java/runningTheExamples/pureUnixlike/example.sh
repo_root/java/runningTheExamples/pureWindows/example.ps1
```


