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

##### Last-modified date: 2020.9.10, 8 p.m.