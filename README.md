# Ssh shell

[![Build Status](https://travis-ci.org/fonimus/spring-boot-ssh-shell.svg?branch=master)](https://travis-ci.org/fonimus/spring-boot-ssh-shell)
[![Code Coverage](https://codecov.io/gh/fonimus/spring-boot-ssh-shell/branch/master/graph/badge.svg)](https://codecov.io/gh/fonimus/spring-boot-ssh-shell)
[![Code Quality](https://api.codacy.com/project/badge/Grade/e695bc79f42c4c80a58f78ebef8c632b)](https://www.codacy.com/app/francois.onimus/spring-boot-ssh-shell?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fonimus/spring-boot-ssh-shell&amp;utm_campaign=Badge_Grade)

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

```yaml
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

```yaml
# command is disabled because endpoint has been disabled 
management:
  endpoint:
    threaddump:
      enabled: false
```

It can also be deactivated by putting command name is exclusion list.

```yaml
ssh:
  shell:
    actuator:
      excludes:
      - audit
      - ...
``` 

## Sample

[Check sample for more details](./sample)