# Task-3

## Requirements

>Dockerize your REST service of Homework II.

## Results

### Docker Hub

+ My Docker Hub account: [gusabary](<https://hub.docker.com/u/gusabary>)
+ My image for Homework-2: [intplat-hw2](<https://cloud.docker.com/repository/docker/gusabary/intplat-hw2>)

### How to Dockerize a SpringBoot application

1. In `pom.xml`, 

   + add `<docker.image.prefix>springboot</docker.image.prefix>` into `<properties>` wrapper.

   + add following plugin into `<plugins>` wrapper:

     ```xml
     <plugin>
     	<groupId>com.spotify</groupId>
     	<artifactId>docker-maven-plugin</artifactId>
     	<version>0.4.11</version>
         <configuration>
     		<imageName>${docker.image.prefix}/${project.artifactId}</imageName>
     		<dockerDirectory>src/main/docker</dockerDirectory>
     		<resources>
     			<resource>
     				<targetPath>/</targetPath>
     				<directory>${project.build.directory}</directory>
     				<include>${project.build.finalName}.jar</include>
     			</resource>
     		</resources>
     	</configuration>
     </plugin>
     ```

   + add `<finalName>docker_spring_boot</finalName>` into `<build>` wrapper.

2. `mvn package` your SpringBoot application. The generated jar `docker_spring_boot.jar` should be under the **target** directory.

3. create `Dockerfile` with following code:

   ```dockerfile
   FROM frolvlad/alpine-oraclejre8
   VOLUME /tmp
   ADD docker_spring_boot.jar app.jar
   RUN sh -c 'touch /app.jar'
   ENV JAVA_OPTS=""
   ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]
   ```

4. `cd` into the directory containing the `Dockerfile` and `docker_spring_boot.jar`.

5. run the command `docker build -t myDocker .` (note the last '**.**')

6. run the command `docker run --rm -d -p 8888:8080 myDocker`

7. your service is built at port 8888 now.

*[Reference](<https://blog.csdn.net/caox_nazi/article/details/78366584#commentBox>)*

##### Last-modified date: 2019.4.25, 10 p.m.


