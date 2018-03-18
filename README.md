# Spring Boot Ssh Shell

[![Build Status](https://travis-ci.org/fonimus/spring-boot-ssh-shell.svg?branch=master)](https://travis-ci.org/fonimus/spring-boot-ssh-shell)
[![Code Quality](https://api.codacy.com/project/badge/Grade/e695bc79f42c4c80a58f78ebef8c632b)](https://www.codacy.com/app/francois.onimus/spring-boot-ssh-shell?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fonimus/spring-boot-ssh-shell&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/e695bc79f42c4c80a58f78ebef8c632b)](https://www.codacy.com/app/francois.onimus/spring-boot-ssh-shell?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fonimus/spring-boot-ssh-shell&amp;utm_campaign=Badge_Coverage)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.fonimus/spring-boot-ssh-shell-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.fonimus/spring-boot-ssh-shell-starter)

> Spring shell in spring boot application over ssh


For more information please visit `spring shell` [website](https://projects.spring.io/spring-shell/) 
or [reference documentation](https://docs.spring.io/spring-shell/docs/2.0.0.RELEASE/reference/htmlsingle/)


## Getting started

### Dependency

```xml
<dependency>
    <groupId>com.github.fonimus</groupId>
    <artifactId>spring-boot-ssh-shell-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

**Note:**: auto configuration `SshShellAutoConfiguration` can be deactivated by property **ssh.shell.enable=false**

### Configuration

Please check class: [SshShellProperties.java](./starter/src/main/java/com/github/fonimus/ssh/shell/SshShellProperties.java) for more information

```yaml
ssh:
  shell:
    actuator:
      enable: true
      # empty by default
      excludes:
      - ...
    # 'simple' or 'security'
    authentication: simple
    # if authentication set to 'security' the AuthenticationProvider bean name
    # if not specified and only one AuthenticationProvider bean is present in the context, it will be used 
    auth-provider-bean-name:
    # for ssh helper 'confirm' method
    confirmation-words:
    - y    
    - yes    
    enable: true
    host: 127.0.0.1
    host-key-file: <java.io.tmpdir>/hostKey.ser
    # displayed in log if generated
    password:
    port: 2222
    user: user
    prompt:
      # in enum: com.github.fonimus.ssh.shell.PromptColor
      color: white
      text: 'shell>'
```

* Add `spring-boot-starter-actuator` dependency to get actuator commands

* Add `spring-boot-starter-security` dependency to configure `ssh.shell.authentication=security` with *AuthenticationProvider*

## Actuator commands

If `org.springframework.boot:spring-boot-starter-actuator` dependency is present, actuator commands
will be available. 

Command availability is binded to endpoint activation.

```yaml
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

## Custom authentication

Instead of setting user and password (or using generated one), you can implement your own `SshShellAuthenticationProvider`.

Auto configuration will create default implementation only if there is not an existing one in the spring context.

Example:

```java
import com.github.fonimus.ssh.shell.SshShellAuthenticationProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomPasswordConfiguration {

    @Bean
    public SshShellAuthenticationProvider sshShellAuthenticationProvider() {
        return (user, pass, serverSession) -> user.equals(pass);
    }

}
```

## Helper

A `com.github.fonimus.ssh.shell.SshShellHelper` bean is provided in context to help for additional functionalities.

You can either autowire it or inject it in constructor:

```java
import com.github.fonimus.ssh.shell.SshShellHelper;

@ShellComponent
public class DemoCommand {
	
	@Autowired
	private SshShellHelper helper;
	
	// or
	
	public DemoCommand(SshShellHelper helper){
		this.helper = helper;
	}
}
```

### User interaction

#### Read input

```java
@ShellComponent
public class DemoCommand {
	
	@Autowired
	private SshShellHelper helper;

	@ShellMethod("Welcome command")	
	public String welcome() {
	    String name = helper.read("What's your name ?");
	    return "Hello, '" + name + "' !";
	}
}
```

#### Confirmation

Util `confirm` method displays confirmation message and returns `true` 
if response equals ignore case confirmation words.

Default confirmation words are **[`y`, `yes`]**:

You can specify if it is case sensitive and provide your own confirmation words.

```java
@ShellComponent
public class DemoCommand {
	
	@Autowired
	private SshShellHelper helper;

	@ShellMethod("Confirmation command")
	public String conf() {
	    return helper.confirm("Are you sure ?" [, true|false] [, "oui", "si", ...]) ? "Great ! Let's do it !" : "Such a shame ...";
	}
}
```

### Role check

If you are using *AuthenticationProvider* thanks to property `ssh.shell.authentication=security`, you can check that connected user has right authorities for command.
The easiest way of doing it is thanks to `ShellMethodAvailability` functionality. Example:

```java
@ShellComponent
public class DemoCommand {
	
	@Autowired
	private SshShellHelper helper;

	@ShellMethod("Admin command")
	@ShellMethodAvailability("adminAvailability")
	public String admin() {
		return "Finally an administrator !!";
	}

	public Availability adminAvailability() {
		if (!helper.checkAuthorities(Collections.singletonList("ADMIN"))) {
			return Availability.unavailable("admin command is only for an admin users !");
		}
		return Availability.available();
	}
}
```

## Samples

* [Basic sample](./samples/basic), no actuator, no security, no sessions

* [Complete sample](./samples/complete), actuator, security dependencies and configurations