/*
 * Copyright (c) 2020 François Onimus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 package com.github.fonimus.ssh.shell.auth;

 import org.apache.sshd.server.auth.password.PasswordAuthenticator;
 
 /**
  * Interface to implement custom authentication providers.
  */
 @FunctionalInterface
 public interface SshShellAuthenticationProvider extends PasswordAuthenticator {
 
     String AUTHENTICATION_ATTRIBUTE = "authentication";
 
     /**
      * Returns the type of authentication provided by this implementation.
      *
      * @return the authentication type
      */
     default String getAuthenticationType() {
         return "Generic";
     }
 }
 