package com.wildlava.explore;

public class ExploreResponse {
   private String output = null;
   private String prompt = null;
   private String state = null;
   private boolean suspend = false;
   private boolean end = false;
   private boolean win = false;
   private boolean die = false;
   private String error = null;

   // public ExploreResponse(String output) {
   //    this.message = output;
   // }

   public String getOutput() {
      return output;
   }

   public void setOutput(String output) {
      this.output = output;
   }

   public String getPrompt() {
      return prompt;
   }

   public void setPrompt(String prompt) {
      this.prompt = prompt;
   }

   public String getState() {
      return state;
   }

   public void setState(String state) {
      this.state = state;
   }

   public boolean getSuspend() {
      return suspend;
   }

   public void setSuspend(boolean suspend) {
      this.suspend = suspend;
   }

   public boolean getEnd() {
      return end;
   }

   public void setEnd(boolean end) {
      this.end = end;
   }

   public boolean getWin() {
      return win;
   }

   public void setWin(boolean win) {
      this.win = win;
   }

   public boolean getDie() {
      return die;
   }

   public void setDie(boolean die) {
      this.die = die;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }
}
