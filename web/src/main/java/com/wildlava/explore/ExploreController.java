package com.wildlava.explore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExploreController {
   @GetMapping("/adventures/{advname}")
   public ExploreResponse playExplore(@PathVariable String advname,
                                      @RequestParam(required = false) String command,
                                      @RequestParam(required = false) String resume,
                                      @RequestParam(required = false) String last_suspend) {
      ExploreResponse response = new ExploreResponse();
      Explore.playOnce(response, advname, command, resume, last_suspend);
      return response;
   }
}
