# Usage

1. Build sample application, or get jar from maven repository

    ```bash
    mvn clean install [-DskipTests]
    ```
1. Start application

    ```bash
    java -jar sample/target/spring-boot-ssh-shell-basic-sample[-version].jar
    ```
1. Connect to application via ssh (default password: pass)

    ```bash
    ~/home$ ssh -p 2222 user@localhost
    Password authentication
    Password: 
    
            _         _        _ _
      _____| |_    __| |_  ___| | |
     (_-<_-< ' \  (_-< ' \/ -_) | |
     /__/__/_||_| /__/_||_\___|_|_| v1.0.1-SNAPSHOT
    
    
    Please type `help` to see available commands
    basic::>help
    AVAILABLE COMMANDS
    
    Built-In Commands
            clear: Clear the shell screen.
            exit, quit: Exit the shell.
            help: Display help about available commands.
            script: Read and execute commands from a file.
            stacktrace: Display the full stacktrace of the last error.
    
    Demo Command
            echo: Echo command

    ```