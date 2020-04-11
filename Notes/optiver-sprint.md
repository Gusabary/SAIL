# Optiver Sprint

## C++

### Virtual Keyword

+ **polymorphism**

  From the perspective of programmers, I declare two classes, one is called class A, which is the parent class and the other is called class B, which is the child class. Now I declare a function with virtual keyword in class A and override it in class B. So in my code, I could declare an object of class A but use the constructor of class B to initialize it. So I write "A star obj equals new B", and then when I call the method of obj, the function in class B will actually be invoked although obj is previously declared as class A.

  That's how we use the polymorphism mechanism, and from the level of compiler, when there is any virtual function in the class, the compiler will allocate a virtual table for the class in the static area during compiler-time. In the virtual table, there are addresses of the virtual functions. So when we have the parent class A and child class B, we have two virtual tables. And the most important is that when I override the virtual function in child class, the corresponding entry in the virtual table of child class will also be updated. So now each class has their own version of implementations in the virtual table.

  And finally, when we initialize the object, the compiler will add a field called virtual pointer (vptr) into the memory of the object just like it is a member variable of that class. And the virtual pointer points to the virtual table of the class whose constructor is just invoked. That is to say, when we declare an object as parent class but use the constructor of child class to initialize it, we will get a virtual pointer pointing to the virtual table of child class so when we invoke any virtual function, the compiler will find along the virtual pointer to get the child class version.

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

### Compilation

+ **the whole compilation process, from source code to executable file**

  It can be divided into four steps.

  The first is preprocessing or precompilation. Its task is to expand the included file, replace the macro, choose the branch of ifdefine and remove comments.

  The second is compilation. Its task is to compile preprocessed code to assembly code.

  The third is assembly. Its task is to transfer the assembly code to machine code in binary format.

  And the fourth is linking. Its task is to plug in the actual address of some functions (relocation), symbol resolution and combine some extra code to our program that is required when the program starts and ends such as the code for passing the command line arguments and environment variables.

### Smart Pointer

+ **smart pointer** (circular reference)

  I know three types of smart pointer. They are unique pointer, shared pointer and weak pointer.

  Smart pointer is born to solve the problem that sometimes resources pointed by raw pointer may not be released so we encapsulate the raw pointer and delete it in the destructor.

  First, resources are exclusively possessed by unique pointer, which means it has disabled the copy constructor and copy assignment.

  Second, resources can be shared between shared pointer. It maintains a reference counter, which will increase by one when the copy constructor or copy assignment get invoked and correspondingly will decrease by one when the destructor gets invoked and the resource will not be released until the reference counter reaches zero.

  Finally, the weak pointer is born to solve the problem of circular reference of shared pointer. The weak pointer is a kind of temporary ownership, which means it will not increase the reference counter when pointing to the resource.

  The circular reference is when two shared pointers pointing to each other, which forms a circle, the resources managed by them won't be released. Imagine that class A has a member variable, which is a shared pointer pointing to class B. Correspondingly, class B also has a shared pointer pointing to class A. So when I declare two shared pointers respectively pointing to A and B, and then let the shared pointer in A point to B while the one in B point to A, this problem will come up.

+ **Is shared_ptr thread safe?**

  The reference count itself is thread self because it's accessed atomically.

  But the raw pointer managed by the shared pointer is certainly not thread safe without any lock.

  In my impression, cpp twenty will introduce the atomic shared pointer, which is born for the multithread workload.

+ **What is the difference between shared_ptr's constructor and make_shared?**

  If you use the constructor to create the shared pointer and the object managed by it, you will take two memory allocations, one for the object and one for the reference counter. While make_shared allocates this two blocks of memory at one time.

  So the greatest advantage of make_shared is that it reduces half the overhead of memory allocation. What's more, when you use the constructor as one of the parameter of any function while another parameter is a result of a function call, which could throw exception, then this may lead to memory leak. Because the evaluation order of function parameters are uncertain, at least before cpp 17, one of the possible sequences may be new the object first and then throw an exception before the shared pointer could take the ownership in its constructor. So with the make_shared, the first and third thing in the scenario just described will be done together.

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
+ **template**
+ **copy constructor**
+ **move semantic**
+ 

## Data Structure

+ **heap**

## OS

+ **the difference between process and thread**

  process is the independent unit of resource allocation while thread is the independent unit of schedule

  in the kernel they are both referred to as the same data structure called task, the only difference is that the amount of resources they share is different. Between processes, many resources are not shared such as the virtual memory, fd table and signal handler. But between threads, many resources are shared except stack and registers.

+ **Inter-process communication**

  + pipe (How many kinds of pipe do you know?)
  + semaphore
  + signal
  + message queue
  + shared memory (What's the difference from general memory?)
  + socket

+ **deadlock**

+ **ptrace**

+ **describe the process of fork**

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
+ **A, B, C, D, E IP**
+ **the difference between IPv4 and IPv6**
+ 

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

### Socket

+ **socket**

## Algorithms

+ **How to choose the pivot in quick sort?**

##### Last-modified date: 2020.4.1, 3 p.m.

