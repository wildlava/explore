package com.wildlava.explore;

import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExploreController {
   @GetMapping("/adventures/{advname}")
   public ExploreResponse playExplore(@PathVariable String advname) {
      ExploreResponse response = new ExploreResponse();
      Explore.playOnce(response, advname, null, null, null);
      return response;
   }

   @PostMapping(value = {"/adventures/{advname}/{command}", "/adventures/{advname}"})
   public ExploreResponse playExplore(@PathVariable String advname,
                                      @PathVariable Optional<String> command,
                                      @RequestBody ExploreRequest request) {
      ExploreResponse response = new ExploreResponse();
      Explore.playOnce(response, advname, command.orElse(""),
                       request.getState(),
                       request.getLastSuspend());
      return response;
   }
}
