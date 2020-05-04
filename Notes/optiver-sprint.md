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

## English

### Some phrases

- lie in 在于  Another subtle problem lies in ownership.
- be born to 应运而生，用来...  Smart pointers were born to fix the annoyances mentioned above.
- no longer in use 不再使用
- go out of scope 退出作用域  get destroyed once out of scope 
- get triggered 被调用  (get + pp.)
- might be present 可能存在  A little speed penalty might be present.
- all in all 总而言之
- in a nutshell 简而言之
- pave the way for 为..铺平道路
- have a notion of 有..的概念  C does not have a notion of modules.
- outside work 在工作之外
- pay a lot of attention to sth. 
- underlying implementation 底层实现
- time-consuming 耗时的
- full / half 'duplex 全双工 / 半双工
- means of communication 通信方式
- pointee object 被指向的对象
- as the name suggests 顾名思义
- paren'thesis 圆括号
- bracket 方括号
- brace 花括号 /ei/
- quotes 引号
- ampersand &符号

### non-tech

- Could you please repeat your question? I didn't hear it clearly.
- Sorry, can you say it again?
- that's it 就是这样
- That's all I know.
- That's all that came to my mind.
- If I remember correctly.
- Give me a few minutes to think.
- bachelor's degree 学士学位
- master's degree 硕士学位

### HR / behavioural interview

#### Self Introduction

- **Tell me a little about yourself**

  OK, As you can see on my resume,

  I'm Tao Bocheng. I'm now a junior majoring in software engineering at Shanghai Jiao Tong University.

  // It’s my great honor to be given this great opportunity to talk with you here.

  During the period of school, I write code in cpp most of the time. I really like it.

  In the last semester, I've been fortunately invloved in a project about security inspection and monitoring, which is a cooperative project between Optiver and SJTU.

  Also, I've made a tiny tool for conversion from pseudo-code to flowchat in character format.

  And I'm very interested in the knowledge about computer system and I'm familier with Linux.

  In addition, I'd like to share some of my notes, discoveries and thoughts on my personal blog and GitHub around my learning.

- **How much do you know about Optiver?**

  I know Optiver is a leading global trading firm and market maker for various financial instruments.

  But as far as I know, IT department in Optiver is not in the edge or not important. It's also a core department in Optiver rather than just provide support for other departments.

#### Project

- **Tell me about the project**

  It's a secutiry system for inspection and monitoring, which aims at the third-party application that may be risky. 

  The whole system divides into two parts. The first one is the static analysis with ptrace. We read the user-defined configuration file into our system, then use ptrace to analyze the third-party application statically and finally generate a report about the application's behaviour.

  The second part is real-time monitoring. We use seccomp, which is a security tool provided by linux, to set up a sandbox environment in which we run the target application. And once the application tries some risky operations that we forbid we will shut it down. Also we evaluate the temporal overhead of our system.

  I'm mainly responsible for ptrace-related work in the first part and seccomp-related work in the second part.

- **What's your takeaway from this project?**

  I think the biggest gain is through the project I find what I am really interested in and clear the path and direction for the future development and growth.

  And also, I got more knowledge about linux and cpp and improved my skills to design a system and solve practical problems.

  What's more, this projcet also taught me how to collaborate effectively. It's a team project.

#### Strengthens and Weaknesses

- **What's your strengthens?**

  As you can see in my resume, I'm pretty interested in cpp and good at it.

  And also I have essential knowledge about computer system due to our curriculum.

  And through those projects I have taken part in, I also have some experience about how to work together as a team effectively.

  What's more, I am relatively flexible and able to handle change in my job responsibility because for new things or new technique, I am always curious and willing to learn and have a try.

  And I think I'm not afraid to put forward my opinions and thoughts and I'm always willing to discuss with others.

- **What's your weaknesses?**

  Although I can keep flexible when I'm faced with new things or new technique and I'm not afraid to discuss with others, I might get nervous when talking to strangers, especially in English but this may not last too long if I try to get familiar with them.

  Also sometimes when I think my point of view is right, I may be kind of obstinate and stubborn, but also I am trying to correct it as I have more and more communication with others.

#### Future Plan and Academic Results

- **What's your future plan?**

  In the short term, my primary plan is to take this offer, join Optiver for summer internship and then get the return offer.

  In the long term, my current thinking is to get engaged in the development work and constantly polish up my skills about software, english and cooperation and eventually grow up into an expert in software engineering and IT development.

  When I chatted with a senior in our lab before, he said when he was an intern in Optiver once he encountered a bug and could not locate it immediately. At this time his mentor or someone else, I didn't remember clearly, came to help him. The mentor used gdb with a sequence of very quick operations and located the problem in just few seconds. I don't know if there is any exaggration, but indeed I want to grow as capable as him.

- **Why do you want to work here?**

  Actually there are two reasons. 

  The first is about Optiver itself. Optiver is in the top level of the industry of market maker, which means the nice working environment, good payment, great opportunity to get improved and widely recgonized intership experience. Also there is a billingual environment, so I think both my techincal capability and English skills can get improved.

  And on the other hand I have my own considerations. There are some seniors of our lab in Optiver and I have been there several times to report our work in the cooperative project so compared to other companies Optiver is kind of more familier. And actually I found it's a really good match between the development work in Optiver and my acquired knowledge, technology stack and interests. And to some extent, getting this offer is a goal I set for myself so I don't want to let myself down.

  /* Optiver is a prop'rietary trading firm and market maker for various exchange-listed financial instruments.

  I heared about Optiver from seniors in our lab at the very beginning and I also got a chance fortunately to take part in the cooperative project of Optiver and SJTU. When I firstly got in touch with the project, I was really attracted by the application background and the pure fun of somewhat practical software.

  And also, after several times of reporting our current work at Optiver, the perfect working environment there really leaves me a deep impression.

  And as I learned more and more about what a developer's work is in Optiver, I found it really a good match between that and my acquired knowledge, technology stack and interests. So I hope and believe I can take the summer internship and become part of Optiver. */

- **Why not consider going to graduate school?**

  Actually until half a year ago, I was preparing for further study at graduate school. But when I was fortunetaly involved in the cooperative project, I found that I was really attracted by the application background and the pure fun of somewhat practical software. 

  And considering that my academic results in the freshmen year are not that good, which means I may not get the qualification of postgraduate recommendation so I decided to work directly after graduation.

- **Why are your academic results in the freshmen year not that good? And how about that now?**

  Because in the freshmen year, I didn't pay much attention into my academic studies. I just enjoyed the release from high school. So I was ranked around 50%.

  The turning point was at the first semester of my sophomore year. Our teacher held a class meeting and he asked us what our future plan is. He listed four choices, graduate recommedation, taking exams for graduate school, going abroad for further study and taking a job directly. And that was my first time to consider this kind of question seriously and I was shocked that I hadn't had any plan or consideration about this so since then I gradually paid more and more attention to my academic studies, met a senior who really taught me a lot and joined our lab in last summer, where I met many nice friends. In the recent two years, my academic ranking is about top ten.

- **Why is the academic result in junior year is not better than sophomore year?**

  Because in the sophomore year, my primary goal is to get the qualification of postgraduate recommendation, which means the academic results need to be very excellent. So I strove for every single grade point. But in the junior year, there was a shift in my thoughts that I wanted to take a job instead of graduate recommenadtion. And I found that striving for each single point had taken too much time and efforts that I almost had no time to improve myself in some other interesting fields or aspects that are not very related to the course itself such as diving deeper into linux and cpp, and consolidating the knowledge base of computer system and network.

  I don't mean the academic result is not important but if you decided to take a job instead you could spare some time to learn something more interestring or more practical.

#### Impressive experience

- **an impressive experience or difficulty (Database pre)**

  S: In my second semester of sophomore, we had a course presentation of Database. 

  T: We were a team of four members but only one could take the presentation. At that time one of them, called Yuan and I both wanted to take that chance and we both thought that we would perform better than each other. Because, you know, our score was determined by the performance of the one speaking.

  A: So we came up an idea. We both spoke to another member and let him make the decision which one could take the chance to give the presentation. And finally I won and I did make a good job.

  R: So from that experience, I understood deeply that the chances you strive for and the ones others give you or you get easily are really very different. The former is much more precious. So since then, for the chance I wanna take, I may act like, kind of, more ambitious and more active.

- **Wandering D19**

  In last summer semester, authority of our university decided to rebuild our dormitory building and ordered us who lived in this dormitory to move to another building. This decision sounds like nothing special but the problem is that the authority didn't ask for our students' advice and opinions. It just put up a notice in our domitory, which says this building would be rebuilt and you students should move to another dormitory in next semester. What is more annoying is that when we asked for an explanation why the chosen building is our dormitory, why not rebuilding after our graduiation and what the purpose is for rebuilding, the authority didn't give a clear explanation. Because at that time, it was reported that the rebuilding was for ZhiYuan Academy. But it might be rumors. Anyway we just felt kind of discriminated against and we decided to do something. Because for those students who didn't have summer semester, it means when they came back to campus, their dormitory building would be gone. That's ridiculous.

  So we collected students' opinions and attitude and wrote a joint letter to the authority, in which we declared that we needed an explanation and some necessary help in the process of moving house if there must be a rebuilding. But there was no response. And then we contacted the student life park and had a talk with them. We said if there was still not any response, we might public the joint letter and seek help from public voice. And just that night, another notice was put up in our domitory, which said the rebuilding would be postponed until our graduation.

  I fortunately took part in the process. When I heard about this news that the rebuilding was postponed, I was still translating the voice recording of our talk with student like park. Looking at our dormitory qq group was filled with thanks, I felt it's all worth it.

  I learnt from this event that authority is not always right or always unchallengeable. When you think some decisions are made casually, you can and you should raise your questions and challenge it. Our students' power or capability should never be ignored or underestimated.

  We even created a repository on GitHub to commemorate this event.

- **focus on front-end vs. k8s**

  In last summer semester, our project was to develop a mobile application with microservice architecture and my responsibility was building the infrastructure and developing front-end. At the very beginnnig, our infrastructure was just based on Spring Cloud and Docker. There was no problem until about the third week. At that time, we found that there existed some minor problems such as the deployment was inconvinient and it's difficult to maintain the replicated service. So I suggested that we should use Kubernetes to solve these problems. But my teammates thought the current architecture was not that bad and I should focus on the development of front-end. 

  We discussed and argued for several times. I insisted that the architecture needed to be refactored although the problems now were minor. We should prepare in advance. But it was useless. They were not convinced. They thought refactoring the architecture would affect the progress of front-end.

  So I just spent the whole weekend learning Kubernetes and refactoring and successfully gave the new architecture on Monday. 

  From this I learnt that many times people will not be convinced unless you really do it rather than just say it and most of the time you need to believe your instinct, stick to your opinions and make the effort. And with several iterations, our architecture became a highlight in the final presentation.

- **an impressive course (Compiler)**

  Maybe Compiler, a course taken in the first semester of junior. 

  Because in my perspective, Compiler is the hardest course in all of our curriculum, even more difficult than operating system and CSAPP. 

  It's challenging but rewarding. On the one hand, the course content is really too much and difficult to understand; but on the other hand, when I finished the course and completed the course project, I felt totally accomplished and indeed, Compiler taught me a lot of, you know, low-level or fundamental knowledge about computer system that really benefits me.

  One thing which really impressed me is that the task of lab five of Compiler is to complete all the components except register allocation, which is the most challenging one. But the fact is that a compiler cannot work without register allocation so the actual solution is to implement a naive version of register allocation but at that time I thought instead of this, I'd rather implement the real one directly, which means I would work on lab five and six together. However the task is a little bit more difficult than I thought so I stayed up really late to complete them just before deadline, luckily.

  And that decision helped me save the valuable time before final exam because I had completed lab six with lab five.

#### Ending

- **Out of all the other candidates, why should we hire you?**

- **Have you got any offers from other companies?**

  Yeah actually, I have got offers from sixie, meituan, bytedance and tencent. But I declined those from meituan and bytedance because they told me I needed to reply in just few days. Of course I would not give up the opportunity at Optiver so I declined them directly.

- **Do you have any question?**

  - // When will I take the next round of interview if I pass this one? next week? or just after a few days?
  - What is the top quality the candidate needs to have / in order to become a best performer in the following interviews and as a developer in Optiver?
  - // As far as I know, Optiver doesn't provide dinner, so what if someone works overtime. Does he need to have dinner outside?
  - Could you give me any evaluation or suggestions about my performance in today's interview?
  - Could you talk a little about how you feel about Optiver from a perspective of a, you know, veteran?
  - What do you think I should learn in the following months before summer?

##### Last-modified date: 2020.5.4, 12 p.m.

