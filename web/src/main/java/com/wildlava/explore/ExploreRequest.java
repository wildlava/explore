package com.wildlava.explore;

public class ExploreRequest {
   private String state = null;
   private String lastSuspend = null;

   public String getState() {
      return state;
   }

   public void setState(String state) {
      this.state = state;
   }

   public String getLastSuspend() {
      return lastSuspend;
   }

   public void setLastSuspend(String lastSuspend) {
      this.lastSuspend = lastSuspend;
   }
}
