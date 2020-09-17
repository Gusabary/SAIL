# Notes

## Pre-requisite

### install g++-8

First it may need to add a new apt source to speed up download. *[reference]()*

Directly install g++-8 (which supports c++17) and then redirect the default g++ to g++-8 with `update-alternatives` tool: *[reference](https://linuxconfig.org/how-to-switch-between-multiple-gcc-and-g-compiler-versions-on-ubuntu-20-04-lts-focal-fossa)*

```bash
sudo apt install g++-8
sudo update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-8 8
g++ -v
```

## Lab 0  Networking Warmup

+ `telnet` is used as a client, connecting to a server while `netcat` can be used as a server, listening on a port.

## Lab 1  Stitching substrings into a byte stream

+ use `.base()` method to convert a `reverse_iterator` to `iterator`

## Lab 2  The TCP receiver

+ you may find [`numeric_limits`](https://en.cppreference.com/w/cpp/types/numeric_limits) is helpful sometimes

## Lab 3  The TCP sender

## Lab 4  The TCP connection

There are some interesting commands in shell script:

+ [`shift`](https://www.shellscript.sh/tips/shift/) can be used to discard command line argument
+ [`set`](https://unix.stackexchange.com/questions/308260/what-does-set-do-in-this-dockerfile-entrypoint) can be used to add command line argument
+ [`declare`](https://www.runoob.com/linux/linux-comm-declare.html) can be used to declare a variable
+ `dirname` can be used to get the directory under which the file exists
+ `.` can be used to execute commands in a file
+ `eval` merges all its arguments as a single command and then execute it
+ [`trap`](https://www.computerhope.com/unix/utrap.htm) can be used to register a callback for a specific event
+ `mktemp` can be used to create a tmp file under `/tmp` directory
+ [`getopts`](https://www.shellscript.sh/tips/getopts/) can be used to parse arguments
+ [`iptables`](https://phoenixnap.com/kb/iptables-tutorial-linux-firewall) can be used to configure firewall
+ [`numfmt`](http://manpages.ubuntu.com/manpages/trusty/man1/numfmt.1.html) can be used to convert numbers from/to human-readable strings
+ `dd` can be used to convert and copy a file (setting `/dev/urandom` as input can generate random bytes)

##### Last-modified date: 2020.9.17, 6 p.m.