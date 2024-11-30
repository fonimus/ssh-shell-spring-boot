/*
 * Utility class for formatting completion proposals.
 */

 package com.github.fonimus.ssh.shell;

 public class ProposalFormatter {
 
     /**
      * Formats the completion proposal based on the `complete` flag.
      *
      * @param proposal The extended completion proposal
      * @return Formatted proposal value
      */
     public static String formatProposal(ExtendedCompletionProposal proposal) {
         return proposal.isComplete() ? proposal.getValue() + " " : proposal.getValue();
     }
 }
 