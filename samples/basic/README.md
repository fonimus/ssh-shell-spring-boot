# Usage

1. Build sample application

    ```bash
    mvn clean install -f samples/basic [-DskipTests]
    ```
1. Start application

    ```bash
    java -jar samples/basic/target/ssh-shell-spring-boot-basic-sample[-version].jar
    ```
1. Connect to application via ssh (default password: pass)

    ```bash
    ~/home$ ssh -p 2222 user@localhost
    Password authentication
    Password: [password]
    
    Please type `help` to see available commands
    basic::>help
    AVAILABLE COMMANDS
    
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
            echo: Echo command
            pojo: Pojo command

    ```