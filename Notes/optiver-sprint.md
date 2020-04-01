# Optiver Sprint

## Network

### OSI 七层模型

+ Open System Interconnection Model

#### Application Layer

+ HTTP, FTP, TELNET, SSH, SMTP, POP3, HTML, DNS

#### Presentation Layer

+ translation of data between a networking service and an application
+ character encoding, data compression and encryption/decryption

#### Session Layer

+ manage communication sessions

#### Transport Layer

+ process to process
+ TCP, UDP

#### Network Layer

+ host to host
+ IP
+ in the form of packet

#### Data Link Layer

+ in the form of data frame
+ error detection

#### Physical Layer

+ responsible for the transmission and reception of unstructured raw data

### IP

+ **Subnet mask** is used to differentiate network address and host address from a given IP address

### TCP

#### TCP connection begin - three-way handshake

+ step 1: SYN
+ step 2: SYN + ACK
+ step 3: ACK

#### TCP connection terminated - four-way handshake

+ step 1: FIN from the client
+ step 2: from now on, the client can no longer send any packet to the server but the server can send to the client
+ step 3: FIN from the server
+ step 4: the client waits for 2MSL (Most Segment Lifetime) and then closes the connection

##### Last-modified date: 2020.4.1, 3 p.m.

