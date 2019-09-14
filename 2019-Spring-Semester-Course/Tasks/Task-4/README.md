# Task-4

## Requirements

> Implement a container in Java.
>
> + If the number of items in the container is less than threshold, the container should be a **queue**. Otherwise, it should be a **stack**.
> + If the time that an item is staying in the container is longer than timeout, remove it.

## Results

You can try at *http://47.100.126.180:8410* with following path:

+ `/api/produce`: produce an item and insert it into container.
+ `/api/consume`: consume an item and remove it from container.
+ `/api/view`: view current items in container.

Or you can run locally with following commands under *task-4* directory:

```
mvnw clean test
mvnw spring-boot:run
```

##### Last-modified date: 2019.5.25, 6 p.m.

