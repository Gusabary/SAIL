# Guide to IP Layer Network Administration with Linux

+ use `route -n`, we can see default gateway like this:

  ```
  Destination     Gateway         Genmask         Flags Metric Ref    Use Iface
  0.0.0.0         192.168.99.254  0.0.0.0         UG    0      0        0 eth0
  ```

  note that `Genmask` is all zero.

+ gateway is just router, right?

+ to my understanding, there is a route table inside kernel, and every time the host is to send a packet, it will look up the table. (i.e. route table doesn't exist in router only). After finding the corresponding entry in the route table, the host will send the packet through the interface recorded in the entry, and the interface's ip address will be the source ip of the packet.

+ Ethernet is just one of the most common link layer networks in use today. the main responsibility of ARP is to map an IP address to a link layer address

+ when a machine is going to send a packet to a locally connected host (at this time it knows the ip of that host but not link layer address), it will need the link layer address (MAC address) of that host so it broadcasts an ARP request to query the link layer address of that host which is locally connected. and the target host will unicast a reply back.

+ after querying the mapping from an IP address to a link layer address, the host will store it in an ARP cache, also called neighbor table. regular entries in the table will be verified periodically.

+ there are three fundamental concepts of reachability about IP networking:

  + locally hosted: each interface (including `lo`) of the host has an IP, those IPs are locally hosted
  + directly reachable: some IPs that can be directly reached from the interface of the host
  + ultimately reachable: some IPs that can be reached through the router (gateway)

+ it seems that CIDR address is just with an extra "slash-number"

+ within the ranges denoted by the IP network portion and subnet mask (i.e. IP network), the first address is network address while the last one is broadcast address.

+ just as mentioned above, any IP network is defined by two parts: network address and subnet mask.

+ so to my understanding, hub/switch connects hosts inside a subnet like `192.168.100.0/24` while router can forward packets among many different subnets (kinda like host - switch - router hierarchy)

  > Any machine which will accept and forward packets between two networks is a router. Every router is at least dual-homed; one interface connects to one network, and a second interface connects to another network.

  and switch operates on Ethernet frames, which means they dispatch packets according to link layer address, while router does this according to IP (more precisely, router checks its own route table and find the interface through which the dest IP can be reached)

+ when it comes to route selection, there is not only a basic destination-address-based selection, but also a policy-based selection.

+ > When determining the route by which to send a packet, the kernel always consults the routing cache first. If there is no entry in the routing cache, the kernel begins the process of route selection.

##### Last-modified date: 2021.3.15, 10 p.m.