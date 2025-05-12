# Spring Boot Ssh Shell

> [!CAUTION]
> As you may have seen, it's been a while since this repository has been active, so I am deciding to archive it. You are welcome to fork it and make this library live if you still need it !

[![Build Status](https://github.com/fonimus/ssh-shell-spring-boot/actions/workflows/build.yml/badge.svg)](https://github.com/fonimus/ssh-shell-spring-boot/actions/workflows/build.yml)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=fonimus_ssh-shell-spring-boot&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=fonimus_ssh-shell-spring-boot)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=fonimus_ssh-shell-spring-boot&metric=coverage)](https://sonarcloud.io/dashboard?id=fonimus_ssh-shell-spring-boot)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.fonimus/ssh-shell-spring-boot-starter.svg?label=maven%20central)](https://search.maven.org/search?q=g:%22com.github.fonimus%22%20AND%20a:%22ssh-shell-spring-boot-starter%22)

> Spring shell in spring boot application over ssh

For more information please
visit `spring shell` [website](https://docs.spring.io/spring-shell/docs/3.0.3/docs/index.html).

* [Getting started](#getting-started)
* [Commands](#commands)
* [Post processors](#post-processors)
* [Parameter providers](#parameter-providers)
* [Custom authentication](#custom-authentication)
* [Command helper](#command-helper)
* [Banner](#banner)
* [Listeners](#listeners)
* [Session Manager](#session-manager)
* [Tests](#tests)
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

_Warning :_ since version 2.0.0 (spring shell 2.1.0) interactive shell is
enabled by default.
You can set property `spring.shell.interactive.enabled=false` to disable it.

> **Note:** auto configuration `SshShellAutoConfiguration` (active by default)
> can be deactivated by property
> **ssh.shell.enable=false**.

It means that the ssh server won't start and the commands won't be scanned.
Unfortunately the application will still
load the `spring-shell` auto configuration classes and display a shell at
startup (shell:>). You can disable them with
following property:

```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.shell.boot.ExitCodeAutoConfiguration
      - org.springframework.shell.boot.ShellContextAutoConfiguration
      - org.springframework.shell.boot.SpringShellAutoConfiguration
      - org.springframework.shell.boot.ShellRunnerAutoConfiguration
      - org.springframework.shell.boot.ApplicationRunnerAutoConfiguration
      - org.springframework.shell.boot.CommandCatalogAutoConfiguration
      - org.springframework.shell.boot.LineReaderAutoConfiguration
      - org.springframework.shell.boot.CompleterAutoConfiguration
      - org.springframework.shell.boot.UserConfigAutoConfiguration
      - org.springframework.shell.boot.JLineAutoConfiguration
      - org.springframework.shell.boot.JLineShellAutoConfiguration
      - org.springframework.shell.boot.ParameterResolverAutoConfiguration
      - org.springframework.shell.boot.StandardAPIAutoConfiguration
      - org.springframework.shell.boot.ThemingAutoConfiguration
      - org.springframework.shell.boot.StandardCommandsAutoConfiguration
      - org.springframework.shell.boot.ComponentFlowAutoConfiguration  
```  

### Configuration

Please check
class: [SshShellProperties.java](./starter/src/main/java/com/github/fonimus/ssh/shell/SshShellProperties.java)
for more
information

```yaml
ssh:
  shell:
    enable: true
    # 'simple' or 'security'
    authentication: simple
    # if authentication set to 'security' the AuthenticationProvider bean name
    # if not specified and only one AuthenticationProvider bean is present in the context, it will be used 
    auth-provider-bean-name:
    # since 1.2.2, optional file containing authorized public keys (standard authorized_keys format, one key per line
    # starting with 'ssh-rsa'), takes precedence over authentication (simple or not)
    authorized-public-keys-file:
    # since 1.5.5, optional spring resource containing authorized public keys (file:, classpath: , etc)
    # note: in case of a non file resource, a temporary file is created with given content and deleted on process exit
    # this is due to ssh external library which only accepts file in api
    authorized-public-keys:
    # for ssh helper 'confirm' method
    confirmation-words:
      - y
      - yes
    # since 1.4.0, set enable to false to disable following default commands
    commands:
      actuator:
        create: true
        enable: true
        restricted: true
        # empty by default
        excludes:
          - ...
        authorized-roles:
          - ACTUATOR
      # since 1.4.0
      jmx:
        create: true
        enable: true
        restricted: true
        authorized-roles:
          - ADMIN
      system:
        create: true
        enable: true
        restricted: true
        authorized-roles:
          - ADMIN
      # since 1.4.0
      datasource:
        create: true
        enable: true
        restricted: true
        authorized-roles:
          - ADMIN
        excludes:
          - datasource-update
      postprocessors:
        create: true
        enable: true
        restricted: false
      # history and script added in 1.8.0
      history:
        create: true
        enable: true
        restricted: false
      script:
        create: true
        enable: true
        restricted: false
      # since 1.3.0, command which allows you to list ssh sessions, and stop them
      manage-sessions:
        create: true
        enable: false
        restricted: true
        authorized-roles:
          - ADMIN
      # since 1.5.0
      tasks:
        create: true
        enable: false
        restricted: true
        authorized-roles:
          - ADMIN
    display-banner: true
    history-file: <java.io.tmpdir>/sshShellHistory.log
    # since 1.3.0, set to false to have one file per user (<history-directory>/sshShellHistory-<user>.log)
    shared-history: true
    # since 1.3.0, only if shared-history is set to false
    history-directory: <java.io.tmpdir>
    host: 127.0.0.1
    host-key-file: <java.io.tmpdir>/hostKey.ser
    # displayed in log if generated
    password:
    port: 2222
    user: user
    prompt:
      # in enum: com.github.fonimus.ssh.shell.PromptColor (black, red, green, yellow, blue, magenta, cyan, white, bright)
      color: white
      text: 'shell>'
```

* Add `spring-boot-starter-actuator` dependency to get actuator commands

* Add `spring-boot-starter-security` dependency to
  configure `ssh.shell.authentication=security` with *
  AuthenticationProvider*

#### Default behavior

Some commands are disabled by default, it can be the whole group (
like `manage-sessions`), or just
one sub command (like `datasource-update` in group `datasource`).

To enable a group, set the **enable** property to true :

```yaml
ssh:
  shell:
    commands:
      manage-sessions:
        enable: true
      datasource:
        excludes:
```

To un-exclude a sub command inside a group, set the **excludes** property to the
new wanted
array. To include all sub commands, set new empty array :

```yaml
ssh:
  shell:
    commands:
      datasource:
        excludes:
```

### Writing commands

You can write your command exactly the way you would do with `spring shell` (For
more information please
visit `spring shell` [website](https://docs.spring.io/spring-shell/docs/3.0.3/docs/index.html).

Instead of using `org.springframework.shell.standard.ShellComponent` annotation,
you should
use `com.github.fonimus.ssh.shell.commands.SshShellComponent`:
it is just a conditional `@ShellComponent` with `@ConditionalOnProperty` on
property **ssh.shell.enable**

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

## Commands

All commands group can be deactivated by enable property :

```yaml
ssh:
  shell:
    commands:
      <command>:
        enable: true
```

Sub commands in group can be also filtered by includes and excludes properties :

```yaml
ssh:
  shell:
    commands:
      <command>:
        includes:
          - xxx
        excludes:
          - xxx
```

### Actuator

If `org.springframework.boot:spring-boot-starter-actuator` dependency is
present, actuator commands
will be available.

Command availability is also bind to endpoint activation.

```yaml
management:
  endpoint:
    audit:
      enabled: false
```

### Tasks

Activated by default if you have ``@EnableScheduling``,
these commands allow you to interact with spring boot scheduled tasks :

* `tasks-list` : List scheduled tasks
* `tasks-stop` : Stop one or all scheduled tasks or execution
* `tasks-restart` : Restart one or all scheduled tasks
* `tasks-single` : Launch one execution of all or specified task(s)

Note: refresh parameter in `tasks-list` will remove single executions.

#### Task scheduler

Based on spring
documentation ``org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor.setScheduler``
the task scheduler used for scheduled tasks will be :

If not specified, it will look for unique bean of type ``TaskScheduler``, or
with name
``taskScheduler``. Otherwise, a local single-threaded will be created.

The ``TasksCommand`` keep the same mechanism in order to be able to restart
stopped scheduled tasks.
It also provides a ``setTaskScheduler()`` in case you want to specify custom
one.

##### Examples

| Context                                                                                                                                                    | Task scheduler used in TaskCommand                            |
|------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| No ``TaskScheduler`` bean in context                                                                                                                       | Local single-threaded                                         |
| One ``TaskScheduler`` bean named **ts** in context                                                                                                         | **ts** bean                                                   |                                               |
| Multiple ``TaskScheduler`` beans named **ts1**, **ts2** in context                                                                                         | Local single-threaded (could not find name **taskScheduler**) |                                               |
| Multiple ``TaskScheduler`` beans named **taskScheduler**, **ts2**, **ts3** in context                                                                      | **taskScheduler** bean                                        |                                               |
| Task scheduler specified in method ``SchedulingConfigurer#configureTasks``                                                                                 | Local single-threaded (not set in task)                       |
| Task scheduler specified in method ``SchedulingConfigurer#configureTasks`` **AND** ``com.github.fonimus.ssh.shell.commands.TasksCommand.setTaskScheduler`` | Scheduler manually set                                        |

### Jmx

* `jmx-info`: Displays information about jmx mbean. Use -a option to query
  attribute values.
* `jmx-invoke`: Invoke operation on object name.
* `jmx-list`: List jmx mbeans.

### System

* `system-env`: List system environment.
* `system-properties`: List system properties.
* `system-threads`: List jvm threads.

### Datasource

* `datasource-list`: List available datasources
* `datasource-properties`: Datasource properties command. Executes 'show
  variables'
* `datasource-query`: Datasource query command.
* `datasource-update`: Datasource update command.

### Postprocessors

* `postprocessors`: Display the available post processors

## Post processors

> **Note: since 1.0.6**

Post processors can be used with '|' (pipe character) followed by the name of
the post processor and the parameters.
Also, custom ones can be added.

### Provided post processors

#### Save

This specific post processor takes the key character '>'.

Example: ```echo test > /path/to/file.txt```

#### Pretty

This post processor, named `pretty` takes an object and apply jackson pretty
writer.

Example: ```info | pretty```

#### Json

This post processor, named `json` allows you to find a specific path within a
json object.

Caution: you need to have a json string. You can apply `pretty` post processor
before to do so.

Example: ```info | pretty | json /build/version```

#### Grep

This post processor, named `grep` allows you to find specific patterns within a
string.

Examples: ```info | grep boot```,```info | pretty | grep boot spring```

#### Highlight

This post processor, named `highlight` allows you to highlight specific patterns
within a string.

Examples: ```info | highlight boot```,```info | pretty | highlight boot spring```

### Custom

To register a new json result post processor, you need to implement
interface `PostProcessor`

Then register it within a spring configuration.

Example:

````java

@Configuration
class PostProcessorConfiguration {
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
}
````

## Parameter providers

### Enum

Enumeration option parameters have auto completion by default.

### File

Thanks to [ExtendedFileValueProvider.java](
./starter/src/main/java/com/github/fonimus/ssh/shell/providers
/ExtendedFileValueProvider.java)
(or FileValueProvider is deactivated), auto completion is available
for `java.io.File` option parameters.

### Custom values

To enable auto completion for a parameter, declare a **valueProvider** class.

> **Note:** the value provider has to be in the spring context.

````java
class Commands {
    public command(@ShellOption(valueProvider = CustomValuesProvider.class) String message) {
        // deal with message
    }
}

@Component
class CustomValuesProvider implements ValueProvider {

    private final static String[] VALUES = new String[]{
            "message1", "message2", "message3"
    };

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        return Arrays.stream(VALUES).map(CompletionProposal::new).collect(Collectors.toList());
    }
}
````

## Custom authentication

Instead of setting user and password (or using generated one), you can implement
your
own `SshShellAuthenticationProvider`.

Auto configuration will create default implementation only if there is not an
existing one in the spring context.

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

A `com.github.fonimus.ssh.shell.SshShellHelper` bean is provided in context to
help for additional functionalities.

You can either autowire it or inject it in a constructor:

```java
import com.github.fonimus.ssh.shell.SshShellHelper;

@SshShellComponent
public class DemoCommand {

    @Autowired
    private SshShellHelper helper;

    // or

    public DemoCommand(SshShellHelper helper) {
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
        boolean success = true;
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
        return helper.confirm("Are you sure ?") ? "Great ! Let's do it !" : "Such a shame ...";
    }
}
```

#### Table

A builder `com.github.fonimus.ssh.shell.SimpleTableBuilder` is available to
quickly set up print table.

Quick example:

````java
class Commands {
    public table() {
        helper.renderTable(SimpleTable.builder()
                .column("col1")
                .column("col2")
                .column("col3")
                .column("col4")
                .line(Arrays.asList("line1 col1", "line1 col2", "line1 col3", "line1 col4"))
                .line(Arrays.asList("line2 col1", "line2 col2", "line2 col3", "line2 col4"))
                .line(Arrays.asList("line3 col1", "line3 col2", "line3 col3", "line3 col4"))
                .line(Arrays.asList("line4 col1", "line4 col2", "line4 col3", "line4 col4"))
                .line(Arrays.asList("line5 col1", "line5 col2", "line5 col3", "line5 col4"))
                .line(Arrays.asList("line6 col1", "line6 col2", "line6 col3", "line6 col4"))
                .build());
    }
}

````

Result :

````text
┌──────────┬──────────┬──────────┬──────────┐
│   col1   │   col2   │   col3   │   col4   │
├──────────┼──────────┼──────────┼──────────┤
│line1 col1│line1 col2│line1 col3│line1 col4│
├──────────┼──────────┼──────────┼──────────┤
│line2 col1│line2 col2│line2 col3│line2 col4│
├──────────┼──────────┼──────────┼──────────┤
│line3 col1│line3 col2│line3 col3│line3 col4│
├──────────┼──────────┼──────────┼──────────┤
│line4 col1│line4 col2│line4 col3│line4 col4│
├──────────┼──────────┼──────────┼──────────┤
│line5 col1│line5 col2│line5 col3│line5 col4│
├──────────┼──────────┼──────────┼──────────┤
│line6 col1│line6 col2│line6 col3│line6 col4│
└──────────┴──────────┴──────────┴──────────┘
````

### Interactive

> **Note: since 1.1.3**

This method takes an interface to display lines at regular interval.

Every **refresh delay** (here 2
seconds), `com.github.fonimus.ssh.shell.interactive.InteractiveInput.getLines`
is
called.

This can be used to display progress, monitoring, etc.

The interactive
builder, [Interactive.java](./starter/src/main/java/com/github/fonimus/ssh/shell/interactive/Interactive.java)
allows you to build your interactive command.

This builder can also take key bindings to make specific actions, whose can be
made by the following builder:
[KeyBinding.java](./starter/src/main/java/com/github/fonimus/ssh/shell/interactive/KeyBinding.java).

```java

@SshShellComponent
public class DemoCommand {

    @Autowired
    private SshShellHelper helper;

    @ShellMethod("Interactive command")
    public void interactive() {

        KeyBinding binding = KeyBinding.builder()
                .description("K binding example")
                .key("k").input(() -> LOGGER.info("In specific action triggered by key 'k' !")).build();

        Interactive interactive = Interactive.builder().input((size, currentDelay) -> {
            LOGGER.info("In interactive command for input...");
            List<AttributedString> lines = new ArrayList<>();
            AttributedStringBuilder sb = new AttributedStringBuilder(size.getColumns());

            sb.append("\nCurrent time", AttributedStyle.BOLD).append(" : ");
            sb.append(String.format("%8tT", new Date()));

            lines.add(sb.toAttributedString());

            SecureRandom sr = new SecureRandom();
            lines.add(new AttributedStringBuilder().append(helper.progress(sr.nextInt(100)),
                    AttributedStyle.DEFAULT.foreground(sr.nextInt(6) + 1)).toAttributedString());
            lines.add(AttributedString.fromAnsi(SshShellHelper.INTERACTIVE_LONG_MESSAGE + "\n"));

            return lines;
        }).binding(binding).fullScreen(true | false).refreshDelay(5000).build();

        helper.interactive(interactive);
    }
}
```

Note: existing key bindings are:

* `q`: to quit interactive command and go back to shell
* `+`: to increase refresh delay by 1000 milliseconds
* `-`: to decrease refresh delay by 1000 milliseconds

### Role check

If you are using *AuthenticationProvider* thanks to
property `ssh.shell.authentication=security`, you can check that
connected user has right authorities for command.
The easiest way of doing it is thanks to `ShellMethodAvailability`
functionality. Example:

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

If a banner is found in spring context and `display-banner` is set to true,
it will be used as welcome prompt message.

## Listeners

An interface is provided in order to receive events on ssh
sessions : ``com.github.fonimus.ssh.shell.listeners
.SshShellListener``.

Implement it and define a spring bean in order to receive events.

_Example_

````java

@Configuration
class ShellListenerConfiguration {
    @Bean
    public SshShellListener sshShellListener() {
        return event -> LOGGER.info("[listener] event '{}' [id={}, ip={}]",
                event.getType(),
                event.getSession().getServerSession().getIoSession().getId(),
                event.getSession().getServerSession().getIoSession().getRemoteAddress());
    }
}
````

## Session Manager

> **Note: since 1.3.0**`

A session manager bean is available and allows you to:

* list active sessions
* get information about one session
* stop a session

**Note: you need to use @Lazy injection if you are using it in a command**

_Example_

````java
class Commands {
    public MyCommand(@Lazy SshShellSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @ShellMethod("My command")
    public String myCommand() {
        sessionManager.listSessions();
        //...
    }
}
````

### Manage sessions commands

If activated `ssh.shell.commands.manage-sessions.enable=true`, the following
commands are available :

* `manage-sessions-info`: Displays information about single session
* `manage-sessions-list`: Displays active sessions
* `manage-sessions-stop`: Stop single specific session

## Tests

It can be annoying to load ssh server during spring boot tests.
`SshShellProperties` class provides constants to easily deactivate
the all ssh and spring shell auto configurations:

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {"ssh.shell.port=2346",
        SshShellProperties.DISABLE_SSH_SHELL,
        SshShellProperties.DISABLE_SPRING_SHELL_AUTO_CONFIG
})
@ExtendWith(SpringExtension.class)
public class ApplicationTest {
}
```

## Samples

* [Basic sample](./samples/basic), no actuator, no security, no sessions
* [Complete sample](./samples/complete), with actuator, security dependencies and configurations

## Release notes

Please check [github releases page](https://github.com/fonimus/ssh-shell-spring-boot/releases).
