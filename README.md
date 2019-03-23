# Spring Boot Ssh Shell

[![Build Status](https://travis-ci.org/fonimus/ssh-shell-spring-boot.svg?branch=master)](https://travis-ci.org/fonimus/ssh-shell-spring-boot)
[![Code Quality](https://api.codacy.com/project/badge/Grade/e695bc79f42c4c80a58f78ebef8c632b)](https://www.codacy.com/app/francois.onimus/ssh-shell-spring-boot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fonimus/ssh-shell-spring-boot&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/e695bc79f42c4c80a58f78ebef8c632b)](https://www.codacy.com/app/francois.onimus/ssh-shell-spring-boot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=fonimus/ssh-shell-spring-boot&amp;utm_campaign=Badge_Coverage)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.fonimus/ssh-shell-spring-boot-starter.svg?label=%22maven%20central%22)](https://search.maven.org/search?q=g:%22com.github.fonimus%22%20AND%20a:%22ssh-shell-spring-boot-starter%22)

> Spring shell in spring boot application over ssh


For more information please visit `spring shell` [website](https://projects.spring.io/spring-shell/) 
or [2.0.1 reference documentation](https://docs.spring.io/spring-shell/docs/2.0.1.RELEASE/reference/htmlsingle/)

* [Getting started](#getting-started)
* [Actuator commands](#actuator-commands)
* [Post processors](#post-processors)
* [Custom authentication](#custom-authentication)
* [Command helper](#command-helper)
* [Banner](#banner)
* [Samples](#samples)
* [Release notes](#release-notes)


## Getting started

### Dependency

```xml
<dependency>
    <groupId>com.github.fonimus</groupId>
    <artifactId>ssh-shell-spring-boot-starter</artifactId>
</dependency>
```

**Note:** auto configuration `SshShellAutoConfiguration` can be deactivated by property **ssh.shell.enable=false**

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
    history-file: <java.io.tmpdir>/sshShellHistory.log
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

### Writing commands

You can write your command exactly the way you would do with `spring shell` (For more information please visit `spring shell` 
([website](https://projects.spring.io/spring-shell/), [reference documentation](https://docs.spring.io/spring-shell/docs/2.0.0.RELEASE/reference/htmlsingle/))

Instead of using `org.springframework.shell.standard.ShellComponent` annotation, you should use `com.github.fonimus.ssh.shell.commands.SshShellComponent`: 
it is just a conditional `@ShellComponent` with `@ConditionalOnProperty` on property **ssh.shell.enable** 

Example:

```java
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;

import com.github.fonimus.ssh.shell.commands.SshShellComponent;


@SshShellComponent
@ShellCommandGroup("Test Commands")
public class TestCommands {

	@ShellMethod("test command")	
	public String test() {
	  return "ok";
	}
}
``` 

## Actuator commands

If `org.springframework.boot:spring-boot-starter-actuator` dependency is present, actuator commands
will be available. 

Command availability is binded to endpoint activation.

```yaml
management:
  endpoint:
    audit:
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

## Post processors

Post processors can be used with '|' (pipe character) followed by the name of the post processor and the parameters.
Also, custom ones can be added.

### Provided post processors

#### Save

This specific post processor takes the key character '>'.

Example: ```echo test > /path/to/file.txt```

#### Pretty

This post processor, named `pretty` takes an object and apply jackson pretty writer.

Example: ```info | pretty```

#### Json

This post processor, named `json` allows you to find a specific path within a json object.

Caution: you need to have a json string. You can apply `pretty` post processor before to do so.

Example: ```info | pretty | json /build/version```

#### Grep

This post processor, named `grep` allows you to find specific patterns within a string.

Examples: ```info | grep boot```,```info | pretty | grep boot spring```

#### Highlight

This post processor, named `highlight` allows you to highlight specific patterns within a string.

Examples: ```info | highlight boot```,```info | pretty | highlight boot spring```

### Custom

To register a new json result post processor, you need to implement interface `PostProcessor`

Then register it within a spring configuration.

Example:

````java
@Bean
public PostProcessor quotePostProcessor() {
    return new PostProcessor<String>() {

        @Override
        public String getName() {
            return "quote";
        }

        @Override
        public String process(String result, List parameters) {
            return "'" + result + "'";
        }
    };
}
````

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

## Command helper

A `com.github.fonimus.ssh.shell.SshShellHelper` bean is provided in context to help for additional functionalities.

You can either autowire it or inject it in constructor:

```java
import com.github.fonimus.ssh.shell.SshShellHelper;

@SshShellComponent
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

#### Print output

```java
@SshShellComponent
public class DemoCommand {
	
	@Autowired
	private SshShellHelper helper;

	@ShellMethod("Print command")	
	public String print() {
        boolean success = ...
	    helper.print("Some message");
	    helper.print("Some black message", PromptColor.BLACK);
	    helper.printSuccess("Some success message");
	    return success ? helper.getSuccess("Some returned success message") : helper.getColored("Some returned blue message", PromptColor.BLUE);
	}
}
```

#### Read input

```java
@SshShellComponent
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
@SshShellComponent
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
@SshShellComponent
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

### Retrieve spring security authentication


```java
@SshShellComponent
public class DemoCommand {
	
	@Autowired
	private SshShellHelper helper;
	
	@ShellMethod("Authentication command")
	public SshAuthentication authentication() {
		return helper.getAuthentication();
	}
}
```

## Banner

If a banner is found in spring context, it will be used as welcome prompt message.


## Samples

* [Basic sample](./samples/basic), no actuator, no security, no sessions

* [Complete sample](./samples/complete), actuator, security dependencies and configurations


## Release notes

### 1.1.2

* Fix option arguments with spaces, quotes, etc 
* Update to `spring boot 2.1.3`
* Update to `javadoc plugin 3.1.0`
* Update to `sshd 2.2.0`

### 1.1.1

* Update to `spring boot 2.1.0`
    * Avoid overriding bean definitions as it is now disabled by default

### 1.1.0

* **New artifact identifier: ssh-shell-spring-boot-starter** -> [maven central link](https://search.maven.org/search?q=g:%22com.github.fonimus%22%20AND%20a:%22ssh-shell-spring-boot-starter%22)

### 1.0.6

* Add new post processor
    * `highlight`
* Fix various issues where ssh.shell.enable=false makes application fail to start
* Add new `@SshShellComponent`

### 1.0.5

* Add post processor feature
    * `pretty`
    * `grep`
    * `json`
    * `save`
* Fix issue with terminal size which made autocomplete fail

### 1.0.4

* Make PrettyJsonResultHandler bean conditional on jackson ObjectMapper class
* Fix application start issue if banner is with configured with off mode

### 1.0.3

* Fix not working history
* Make history file location configurable
* Add authentication objects in ssh session context

### 1.0.2

* Add more user interactions in SshShellHelper

### 1.0.1

* Add spring security authentication provider for ssh connection
* Add spring security roles check
* Add user interactions (read, confirm)
* Bug fixes

### 1.0.0

* First stable release
