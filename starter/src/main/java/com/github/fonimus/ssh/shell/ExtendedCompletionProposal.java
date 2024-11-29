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

 package com.github.fonimus.ssh.shell;

 import lombok.Getter;
 import lombok.Setter;
 import org.springframework.shell.CompletionProposal;
 
 /**
  * Extended completion proposal to allow customization of completion behavior.
  */
 public class ExtendedCompletionProposal extends CompletionProposal {
 
     /**
      * The string value of the proposal.
      */
     @Getter
     private final String value;
 
     /**
      * Indicates if a space should be added after the proposed completion.
      */
     @Getter
     @Setter
     private boolean complete;
 
     /**
      * Default constructor.
      *
      * @param value    string value
      * @param complete true if a space should be added after the proposed completion
      */
     public ExtendedCompletionProposal(String value, boolean complete) {
         super(value);
         this.value = value;
         this.complete = complete;
     }
 }
 