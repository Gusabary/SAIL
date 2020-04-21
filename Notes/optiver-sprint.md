# Optiver Sprint

## C++

+ **Why do you like cpp?**

  First it's fast. It provides the excellent execution efficiency that other languages cannot have.

  And it's powerful. There are so many functionalities or features in cpp. The process of learning the language itself is enjoyable.

  And it's the language that I'm most familiar with. I mean the feeling is great that when you come up with an idea you can directly write it out without thinking "oh how should I write code here in Java or Python" and when you write each line of code, you know exactly what will happen and how it works.

+ **Why is cpp fast?**

  First the variables and data structures are created on the system's stack unless you use malloc or new or something like that. It's faster for system to use data on the stack (Because it's very fast to allocate and free memory space in the stack, just decrease and increase the rsp but for the heap, it needs to maintain some data structures. And the data on the stack is tighter, which means less cache miss. Also, heap is a global resource so it needs to guarantee the thread safety.)

  And cpp organize its data structure in a more direct way. For example, the access to a field of a struct needs just an addition of a pointer and a constant without any more operations (like looking up a hash table)

  And cpp can generate executable file directly and run it on the system but other languages like java run on JVM and they are compiled at run-time.

### Virtual Keyword

+ **polymorphism**

  From the perspective of programmers, I declare two classes, one is called class A, which is the parent class and the other is called class B, which is the child class. Now I declare a function with virtual keyword in class A and override it in class B. So in my code, I could declare an object pointer of class A but use the constructor of class B to initialize it. So I write "A star obj equals new B", and then when I call the method of obj, the function in class B will actually be invoked although obj is previously declared as class A.

  That's how we use the polymorphism mechanism, and from the level of compiler, when there is any virtual function in the class, the compiler will allocate a virtual table for the class in the static area during compile-time. In the virtual table, there are addresses of the virtual functions. So when we have the parent class A and child class B, we have two virtual tables. And the most important is that when I override the virtual function in child class, the corresponding entry in the virtual table of child class will also be updated. So now each class has their own version of implementations in the virtual table.

  And finally, when we initialize the object, the compiler will add a field called virtual pointer (vptr) into the memory of the object just like it is a member variable of that class. And the virtual pointer points to the virtual table of the class whose constructor is just invoked. That is to say, when we declare an object as parent class but use the constructor of child class to initialize it, we will get a virtual pointer pointing to the virtual table of child class so when we invoke any virtual function, the compiler will go along the virtual pointer to get the child class version.

+ **Why cannot the constructor be virtual function?**

  There are two reasons I think. 

  First the virtual pointer is stored in the memory space of the object, which means your cannot use the virtual pointer to find the virtual table before initializing the object. So if the constructor is virtual function, you need virtual pointer but if you need virtual pointer you should invoke constructor to initialize the object first. It's like a kind of endless loop. It's contradictory.

  And second the meaning of virtual function lies in the difference between the type when the object is declared and the runtime type of this object. But when we invoke the constructor the type is always specified.

+ **Why we need virtual destructor?**

  If the destructor is not virtual, when the object is deleted or goes out of its scope, only the destructor of the parent class will get invoked, which means some resources that are possessed by the child class won't be released and this can cause memory leak.

  So if we declare the destructor as virtual function, the destructor of child class will be invoked actually. Here you may ask me how the resources possessed by the parent subobject get released. Well to my understanding, the compiler will invoke the destructor of parent class at the end of the destructor of child class. 

  What's more, the compiler will also add some codes to change the object's runtime type to parent class before invoking the destructor of parent class because there is a specification in the cpp standard, which says that the runtime type of the object must be the class that is being destructed at this time.

+ **Why we need virtual inheritance**

  Virtual inheritance aims for the ambiguity in the diamond inheritance. You know there will be a subobject of parent class in the memory space of child class. So in the scenario of diamond inheritance, the child class will have two subobjects of grandparent class, which could lead to ambiguity when you modify some variable in them.

  So with the help of virtual inheritance, we could make the two subobjects become a shared part, which means we only reserve one copy of them so that the ambiguity can be eliminated.

+ **final identifier in cpp 11**

  The final specifier can be applied to a virtual function so that this function cannot be overridden in the child class.

  Also the final specifier can be applied to a class so that the class cannot be inherited.

### Compilation

+ **the whole compilation process, from source code to executable file**

  It can be divided into four steps.

  The first is preprocessing or precompilation. Its task is to expand the included file, replace the macro, choose the branch of ifdefine and remove comments.

  The second is compilation. Its task is to compile preprocessed code to assembly code.

  The third is assembly. Its task is to transfer the assembly code to machine code in binary format.

  And the fourth is linking. Its task is to plug in the actual address of some functions (**relocation**), **symbol resolution** and combine some extra code to our program that is required when the program starts and ends such as the code for passing the command line arguments and environment variables.

### Smart Pointer

+ **RAII**

  Resource Acquisition Is Initialization.

  It's a programming technique which binds the life cycle of a resource to the life cycle of an object. That means the constructor will allocate the resource and the destructor will release the resource. This can avoid memory leakage. 

  Shared pointer and std string are both examples of RAII.

+ **smart pointer** (circular reference)

  I know three types of smart pointer. They are unique pointer, shared pointer and weak pointer.

  Smart pointer is born to solve the problem that sometimes resources pointed by raw pointer may not be released so we encapsulate the raw pointer and delete it in the destructor.

  First, resources are exclusively possessed by unique pointer, which means it has disabled the copy constructor and copy assignment.

  Second, resources can be shared between shared pointers. It maintains a reference counter, which will increase by one when the copy constructor or copy assignment get invoked and correspondingly will decrease by one when the destructor gets invoked and the resource will not be released until the reference counter reaches zero.

  Finally, the weak pointer is born to solve the problem of circular reference of shared pointer. The weak pointer is a kind of temporary ownership, which means it will not increase the reference counter when pointing to the resource.

  The circular reference is when two shared pointers pointing to each other, which forms a circle, the resources managed by them won't be released. Imagine that class A has a member variable, which is a shared pointer pointing to class B. Correspondingly, class B also has a shared pointer pointing to class A. So when I declare two shared pointers respectively pointing to A and B, and then let the shared pointer in A point to B while the one in B point to A, this problem will come up.

+ **Is shared_ptr thread safe?**

  The reference counter itself is thread safe because it's accessed a'tomically.

  But the raw pointer managed by the shared pointer is certainly not thread safe without any lock.

  In my impression, cpp twenty will introduce the atomic shared pointer, which is born for the multithread workload.

+ **What is the difference between shared_ptr's constructor and make_shared?**

  If you use the constructor to create the shared pointer and the object managed by it, you will take two memory allocations, one for the object and one for the reference counter. While make_shared allocates this two blocks of memory at one time.

  So the greatest advantage of make_shared is that it reduces half the overhead of memory allocation. What's more, when you use the constructor as one of the parameter of any function while another parameter is a result of a function call, which could throw exception, then this may lead to memory leak. Because the evaluation order of function parameters are uncertain, at least before cpp 17, one of the possible sequences may be new the object first and then throw an exception before the shared pointer could take the ownership in its constructor. So with the make_shared, the first and third thing in the scenario just described will be done together.

  But make_shared needs access to the constructor of the class, which may be private.

### STL

+ **STL Iterator**

  five categories:

  + Input Iterator: they can perform sequential single-pass input operations (read from the elements in the container)
  + Output Iterator: they can perform sequential single-pass output operations (write to the elements in the container)
  + Forward Iterator: have all the functionality of input iterator and output iterator (if they are not constant iterators), it can iterate repeatedly
  + Bidirectional Iterator: they are like forward iterators but can also iterate through backwards.
  + Random Access Iterator: it's non-sequential, which means distant elements can be accessed directly by applying an offset value to the iterator without walking through all the elements in between. They have a similar functionality to pointers.

+ **difference between STL Iterator and pointer**

  Although pointers and iterators can both be dereferenced to get the value, there are several differences  between them.

  First the pointer is just a pointer, nothing special while the iterator is an object.

  Second we can perform simple arithmetic operations on pointers like increment and decrement, while not all iterators allow these operations. For example, we cannot perform a decrement operation on a forward-iterator, or add an integer to a non-random-access iterator.

  And the most importantly, iterators abstract the concern of how to walk through a container, so for programmers, we just need to rely on the methods provided by iterators without any insight into how the container stores the elements.

### Something else

+ **hash collision**

  + open addressing: find another free hash unit and store the element in it
  + separate chaining: replace the single element with a linked list
  + rehashing: use another hash function for calculation

+ **template**

+ **copy constructor**

  Empty class will have default constructor, destructor, copy constructor and copy assignment. After cpp 11, there are also move constructor and move assignment.

  when would the copy constructor be invoked:

  + initialize an object with another object of the same class
  + the object is passed by value to a function
  + the object is returned by value from a function

  But there exists a return value optimization, so in the third scenario, the copy constructor may not be invoked actually.

+ **move semantic**

  Move semantic is a new way of moving resources around in an optimal way by avoiding unnecessary copy of temporary objects, based on rvalue references.

## Data Structure

+ **heap**

  A max-heap is a complete binary tree in which the value in each internal node is greater than or equal to the values in the children of that node.

## OS

+ **the difference between process and thread**

  process is the independent unit of resource allocation while thread is the independent unit of schedule

  In the kernel they are both referred to as the same data structure called task, the only difference is that the amount of resources they share is different. Between processes, many resources are not shared such as the virtual memory, fd table and signal handler. But between threads, many resources are shared except stack and registers.

  So the different threads in the same process could directly communicate with each other while the communication between processes needs IPC.

+ **IPC, Inter-process communication**

  + pipe (How many kinds of pipe do you know?)
  + semaphore
  + signal
  + message queue
  + shared memory (What's the difference from general memory?)
  + socket

+ **scheduling policies**

  + First Come, First Served: easy to understand and implement while both turn-around time and response time are long.
  + Shortest Job First: turn-around time is short while response time is long and unfair.
  + Round Robin: fair and short response time while turn-around time is long.
  + Priority scheduling such as Multi-level Queue.

  Turn-around time is the interval from first entrance of the task to the completion.

  Response time is the interval from first entrance of the task to the first response it gets.

+ **thread synchronization**

  When two or more threads are executed concurrently, the shared critical resources need to be protected, which is to say, the accesses to it from different threads need to be synchronized. Otherwise, there may be some conflicts.

  Semaphore / Lock

+ **deadlock**

  There need to be four conditions:

  + mutual exclusive: the resource (lock) is non-shareable
  + hold and wait: a process holding a lock is waiting for another lock
  + no preemption: the lock cannot be taken from the process unless the process itself releases it
  + circular wait: many processes are waiting for each other in circular form

+ **Why virtual memory?**

  + With virtual memory, we can use DRAM as a cache for disc.
  + Virtual memory simplifies memory management. Every process gets the same linear address space.
  + One process cannot interfere with the memory space of another process.

+ **page fault**

  Page fault is an exception raised by MMU when a running program is trying to access a virtual address that has not been mapped. Then the OS exception handler will be invoked to move data from disk to memory and return the execution flow. That is what we call page swapping.

  page swapping policies: FIFO, LRU, clock algorithm

+ **ptrace**

  Ptrace is a system call. It provides a means by which one process can observe and control the execution of another process. Usually the process being traced is child process we called tracee, and the parent process is tracer. The principle is whenever the tracee is delivered a signal, it will be trapped in the tracer and at this moment, tracer can observe and even change the tracee's memory and registers. So ptrace is usually used to implement breakpoint debugging and system call tracing.

+ **seccomp**

  Seccomp is a security tool provided by linux kernel. It allows a process to run in a sandbox environment in which the process can only invoke limited system calls. For example, I could write a configuration file and specify some system calls that the process could invoke. If the process is trying any system call that is not in the whitelist, it will be shut down.

+ **describe the process of fork**

## Network

### OSI Seven Layer Model

+ Open System Interconnection Model

+ **Application Layer**
  + HTTP, FTP, TELNET, SSH, SMTP, POP3, HTML, DNS

+ **Presentation Layer**
  + translation of data between a networking service and an application
  + character encoding, data compression and encryption/decryption

+ **Session Layer**
  + manage communication sessions

+ **Transport Layer**
  + process to process
  + TCP, UDP

+ **Network Layer**
  + host to host
  + IP
  + in the form of packet

+ **Data Link Layer**
  + in the form of data frame
  + error detection

+ **Physical Layer**
  + responsible for the transmission and reception of unstructured raw data

### Network Devices

+ **difference between router and switch**

  The main objective of router is to connect different networks together while the main objective of switch is to connect different devices within a network.

  Router works in network layer where the data is in the form of packet while switch works in data link layer where data is in the form of frame.

### IP

+ **Subnet mask** is used to differentiate network address and host address from a given IP address
+ **A, B, C, D, E IP**
+ **the difference between IPv4 and IPv6**
+ Since IP does not have a inbuilt mechanism for sending error and control messages. It depends on  **ICMP** (Internet Control Message Protocol) to provide an error control.

### TCP

+ **What's the difference between TCP and UDP?**

  First, TCP is a connection-oriented protocol, which means the client and server should establish a connection before transmitting data and close the connection after it while UDP is a datagram oriented protocol so there is no overhead of opening a connection, maintaining a connection and terminating a connection.

  Second, TCP is reliable and has flow control and congestion control while the delivery of data in UDP cannot be guaranteed.

  Third, TCP makes sure that the packets arrive at server in order but there is no sequencing of data in UDP.

  Fourth, TCP is one-to-one while UDP can also be one-to-many, many-to-one and many-to-many.

#### Flow Control and Congestion Control

+ **What is flow control?**

  Flow control basically means that TCP will ensure that a sender will not overwhelm a receiver by sending packets faster then it can consume. The idea is that the receiver will send some feedbacks to the sender to let it know about its current condition.

  Actually with every ack the receiver responses, it will also advertise its current receive window, which is the free space in the receiver buffer. The sender will ensure that the number of packets in flight is never more than the size of receive window. Packets in flight means packets that have been sent but yet haven't been acknowledged.

  When the window size reaches zero, the sender will stop transmitting data and start the persist timer. That is to say, the sender will periodically send a small packet to receiver to check whether it can receive the data again.

  Sliding window is to say that when the sender receives an ack, it first checks whether the ack is the left-most one of its window. If it is, the window will slide forward until the number of packets in flight reaches the size of receive window again.

+ **What is congestion control?**

  Congestion control is used to avoid congestion in the network. It has several policies. They are slow start, congestion avoidance, fast retransmit and fast recovery.

  At the very beginning, the congestion window size increases exponentially until it reaches a threshold called ssthresh. Then the window size increases linearly, which is called congestion avoidance.

  Then there may be two results. First is timeout, in which condition, the ssthresh is reduced to half of the current window size and the window size is set to one and then start with slow start again.

  The second condition is that the sender receives 3 duplicated ack. In this condition, the ssthresh is also reduced to half of the current window size but the window size is set to the new ssthresh instead of one, and then start with congestion avoidance again.

#### Handshake

+ **TCP connection begin - three-way handshake**
  + step 1: SYN. In the first step, the client sends a SYN to the server, informing that it wants to establish a connection and telling the server the sequence number it wants to start with.
  + step 2: ACK + SYN. Then the server responds with an ACK and SYN. ACK signifies the response of segment it received and SYN signifies the sequence number that the server wants to start with.
  + step 3: ACK. Finally the client sends an ACK to acknowledge the response of server and now they  have both established a reliable connection and they can begin to transmit data.

  The first two steps establish the connection from client to server while the last two steps establish the connection from server to client. To my understanding, a pair of SYN and ACK means a connection established in one direction.

+ **TCP connection terminated - four-way handshake**
  + step 1: FIN. First the client decides to close the connection so it sends a FIN to the server and enters  FIN_WAIT_1 state.
  + step 2: ACK. Once the server receives the FIN, it sends back an ACK immediately and enters CLOSE_WAIT state. And after the client receives the ACK, it will enter FIN_WAIT_2 state, in which the client can no longer send any packet to the server but the server can send to the client.
  + step 3: FIN. After some closing process in the server, it will send a FIN to the client and enter LAST_ACK state.
  + step 4: ACK. The client sends an ACK and enters TIME_WAIT state, which may last for 2MSL and then closes the connection.

#### Timer

+ **Retransmission Timer**

  It aims to retransmitting lost segments. When a segment is sent, the timer starts and when the segment's acknowledgement is received, the timer ends. If the timer expires the retransmission timeout, we should retransmit the segment.

  Typically, the retransmission timeout should be set to the RTT but it's very hard to get the precise RTT value so we need some more values.

  First is the RTTm, which is measured RTT. That is the measured round-trip time for one segment.

  Second is the RTTs, which is smoothed RTT. That is the weighted average of RTTm. It can reduce the impact of high fluctuation of RTTm.

  Third is RTTd, which is deviated RTT. It also aims to reduce the impact of fluctuation.

  So the retransmission timeout will be calculated with RTTs and RTTd. What's more, at every transmission, the value of RTO (retransmission timeout) will double.

+ **Persistent Timer**

  When the sender receives an acknowledgement with a window size of zero, it will start a persistent timer. When the timer goes off, it will send a small segment called probe to check whether the receiver can receive data again.

+ **Keep Alive Timer**

  This timer aims to prevent long idle connection. Whenever the server hears from client, it will reset the timer. The timeout is usually 2 hours, which means if the server hasn't heard from client for 2 hours, it will send several probes to check whether the client is still alive. If there are no responses, the server may assume that the client is down and terminate the connection.

+ **Time Wait Timer**

  This timer is used during connection termination. The timer starts when the client sends the last ACK. Usually, the timer is set to twice the maximum segment lifetime to make sure that all segments will have been received or discarded.

### Socket

+ **socket** is an endpoint for sending and receiving data between two applications in the network.

### Cast

+ **Unicast** is a one-to-one transmission

+ **Broadcast** transmits data to all the hosts in a network

  + **limited broadcast** transmits data to all the devices over the network that the sender resides.

    Limited Broadcast Address (i.e. 255.255.255.255) is reserved for limited broadcast.

  + **direct broadcast** transmits data to all the devices over the other network

    Direct Broadcast Address translates all the Host ID part of bits to 1.

+ **Multicast** is like a broadcast that can cross subnets, but unlike broadcast does not touch all nodes.

  Nodes have to subscribe to a multicast group to receive information. (IP address of Class D is used for multicast)

## Algorithms

+ **How to choose the pivot in quick sort?**

  Choose three elements. They could be randomly chosen or we can choose the left most one, the right most one and the middle one and then choose the median of them as the pivot.

##### Last-modified date: 2020.4.15, 4 p.m.

