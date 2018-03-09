# Usage

1. Build sample application

    ```bash
    mvn clean install [-DskipTests]
    ```
1. Start application

    ```bash
    java -jar sample/target/sample[-version].jar
    ```
1. Connect to application via ssh (default password: pass)

    ```bash
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