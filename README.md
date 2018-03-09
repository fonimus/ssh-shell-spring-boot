# Ssh shell

> Spring shell over ssh


For more information please visit [website](https://projects.spring.io/spring-shell/) 
or [reference documentation](https://docs.spring.io/spring-shell/docs/2.0.0.RELEASE/reference/htmlsingle/)


## Getting started

### Dependency

```xml
<dependency>
    <groupId>com.github.fonimus</groupId>
    <artifactId>ssh-shell-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

**Note:**: auto configuration `SshShellAutoConfiguration` can be deactivated by property **ssh.shell.enable=false**

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