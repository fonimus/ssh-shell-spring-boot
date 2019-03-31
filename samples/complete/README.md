# Usage

1. Build sample application

    ```bash
    mvn clean install -f samples/complete [-DskipTests]
    ```
1. Start application

    ```bash
    java -jar samples/complete/target/ssh-shell-spring-boot-complete-sample[-version].jar
    ```
1. Connect to application via ssh (default password: pass)

    ```bash
    ~/home$ ssh -p 2222 [user|actuator|admin]@localhost
    Password authentication
    Password: [password]
    
            _         _        _ _
      _____| |_    __| |_  ___| | |
     (_-<_-< ' \  (_-< ' \/ -_) | |
     /__/__/_||_| /__/_||_\___|_|_| v1.0.1-SNAPSHOT
    
    
    Please type `help` to see available commands
    complete::>help
    AVAILABLE COMMANDS
    
    Actuator Commands
          * audit: Display audit endpoint.
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
          * sessions: Display sessions endpoint.
            shutdown: Shutdown application.
          * threaddump: Display threaddump endpoint.
    
    Built-In Commands
            clear: Clear the shell screen.
            exit, quit: Exit the shell.
            help: Display help about available commands.
            history: Display or save the history of previously run commands.
            jvm-env: List system env.
            jvm-properties: List system properties.
            postprocessors: Display the available post processors.
            script: Read and execute commands from a file.
            stacktrace: Display the full stacktrace of the last error.
            threads: Thread comman.
    
    Demo Command
            admin: Admin command
            authentication: Authentication command
            conf: Confirmation command
            echo: Echo command
            ex: Ex command
            file: File command
            interactive: Interactive command
            progress: Progress command
            size: Terminal size command
            welcome: Welcome command
    
    Commands marked with (*) are currently unavailable.
    Type `help <command>` to learn more.

    
    
    complete::>
    ```