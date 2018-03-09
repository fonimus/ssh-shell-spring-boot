# Ssh shell

> Spring shell over ssh


For more information please visit [website](https://projects.spring.io/spring-shell/) 
or [reference documentation](https://docs.spring.io/spring-shell/docs/2.0.0.RELEASE/reference/htmlsingle/)


## Getting started

### Dependency

```xml
<dependency>
    <groupId>io.fonimus</groupId>
    <artifactId>ssh-shell-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Note:**: auto configuration `io.fonimus.ssh.shell.SshShellAutoConfiguration` can be deactivated by property **ssh.shell.enable=false**

### Configuration

```properties
ssh:
  shell:
    actuator:
      enable: true
      # empty by default
      excludes: 
    enable: true
    host: 127.0.0.1
    host-key-file: <java.io.tmpdir>/hostKey.ser
    # displayed in log if generated
    password:
    port: 2222
    user: user
    prompt:
      color: white
      text: 'shell>'
```

## Actuator commands

If `org.springframework.boot:spring-boot-starter-actuator` dependency is present, actuator commands
will be available. 

Command availability is binded to endpoint activation.

```properties
# command is disabled because endpoint has been disabled 
management:
  endpoint:
    threaddump:
      enabled: false
```

It can also be deactivated by putting command name is exclusion list.

```properties
ssh:
  shell:
    actuator:
      excludes:
      - audit
      - ...
``` 

## Sample

[Check sample for more details](./sample)

```bash
# build sample application
mvn clean install [-DskipTests]

# start application
java -jar sample/target/sample[-version].jar

# connect to application via ssh (default password: pass)
ssh -p user@localhost

Password authentication
Password: 

        _         _        _ _
  _____| |_    __| |_  ___| | |
 (_-<_-< ' \  (_-< ' \/ -_) | |
 /__/__/_||_| /__/_||_\___|_|_| v0.1.0


shell>help
AVAILABLE COMMANDS

Actuator Commands
        audit: Display audit endpoint.
        beans: Display beans endpoint.
        conditions: Display conditions endpoint.
        configprops: Display configprops endpoint.
        env: Display env endpoint.
        health: Display health endpoint.
        httptrace: Display httptrace endpoint.
        info: Display info endpoint.
        loggers: Display or configure loggers.
        mappings: Display mappings endpoint.
        metrics: Display metrics endpoint.
        scheduledtasks: Display scheduledtasks endpoint.
        shutdown: Shutdown application.
      * threaddump: Display threaddump endpoint.

Built-In Commands
        clear: Clear the shell screen.
        exit, quit: Exit the shell.
        help: Display help about available commands.
        script: Read and execute commands from a file.
        stacktrace: Display the full stacktrace of the last error.

Demo Command
        test: Test command
        testex: Test command ex

Commands marked with (*) are currently unavailable.
Type `help <command>` to learn more.

shell>
```