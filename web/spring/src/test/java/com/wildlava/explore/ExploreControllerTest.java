package com.wildlava.explore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
// Use the following for unit tests:
// @WebMvcTest(ExploreController.class)
// Use the following for integration tests:
@SpringBootTest
@AutoConfigureMockMvc
public class ExploreControllerTest {
   @Autowired
   private MockMvc mvc;

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
   }

   @Test
   public void caveShouldReturnCorrectResponseAtStart() throws Exception {
      this.mvc.perform(get("/adventures/cave"))
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.output", is("\n\n*** EXPLORE ***  ver 4.10\n\ncave is now being built...\n\n\nWelcome to the Enchanted Cave!\nThis adventure was created by Joe Peterson and De Crandell.\n\n\nTo the east is a large opening in a wall of rock.\nYou are surrounded by a dense forest.\n")))
         .andExpect(jsonPath("$.prompt", is(":")))
         .andExpect(jsonPath("$.suspend", is(false)))
         .andExpect(jsonPath("$.state", is("2:3:L_mt-1R8pxBlK9ySamZB9DDhXvPJss-GKA9igbuVMdUpTD_ZTVhdgDP8q5RA6EXCm0YQgspkyker8yFH7bNLc88OUUIrscGccSVDEfGYUJRLrnWC44vqRqkQP8JxusPyN6xo3m3wddq6ZeP9vyeZLAwL")));
   }

   @Test
   public void caveShouldReturnCorrectResponseGoingEastFromStart() throws Exception {
      MockHttpServletRequestBuilder request = post("/adventures/cave/e")
         .contentType(MediaType.APPLICATION_JSON)
         .content("{\"state\": \"2:3:L_mt-1R8pxBlK9ySamZB9DDhXvPJss-GKA9igbuVMdUpTD_ZTVhdgDP8q5RA6EXCm0YQgspkyker8yFH7bNLc88OUUIrscGccSVDEfGYUJRLrnWC44vqRqkQP8JxusPyN6xo3m3wddq6ZeP9vyeZLAwL\"}");

      this.mvc.perform(request)
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.output", is("\nYou are in a room dimly lit by torches. Light is pouring\nin through a large opening to the west. A passage goes\nsouth.\n")))
         .andExpect(jsonPath("$.prompt", is(":")))
         .andExpect(jsonPath("$.suspend", is(false)))
         .andExpect(jsonPath("$.state", is("2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ")));
   }

   @Test
   public void caveShouldReturnCorrectResponseGoingSouthFromDimlyLitRoom() throws Exception {
      MockHttpServletRequestBuilder request = post("/adventures/cave/s")
         .contentType(MediaType.APPLICATION_JSON)
         .content("{\"state\": \"2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ\"}");

      this.mvc.perform(request)
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.output", is("\nYou are in an antechamber. There is dust everywhere.\nThis room must have served as a waiting room because there\nare comfortable looking chairs along one wall.\nThe room exits to the north and south.\n")))
         .andExpect(jsonPath("$.prompt", is(":")))
         .andExpect(jsonPath("$.suspend", is(false)))
         .andExpect(jsonPath("$.state", is("2:3:L_mt-1R4phBlbszSbsrkfhp2NErxskPXAF2b5QqQ0d8DyiGNH6LhrI0F_0j0TkZunfippXkdOP3ntiMztuc31HG5YoigM1ww05WvmtBYrM_ngTQJplWp5ljuTviBWfD8Ni1J_qar8zeCZez9nyecWAwP")));
   }

   @Test
   public void caveShouldReturnSuspendTrueWhenSuspendingInDimlyLitRoom() throws Exception {
      MockHttpServletRequestBuilder request = post("/adventures/cave/suspend")
         .contentType(MediaType.APPLICATION_JSON)
         .content("{\"state\": \"2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ\"}");

      this.mvc.perform(request)
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.output", is("Ok\n")))
         .andExpect(jsonPath("$.prompt", is(":")))
         .andExpect(jsonPath("$.suspend", is(true)))
         .andExpect(jsonPath("$.state", is("2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ")));
   }

   @Test
   public void caveShouldReturnCorrectResponseWhenLookingInDimlyLitRoomWithSuspendedGame() throws Exception {
      MockHttpServletRequestBuilder request = post("/adventures/cave/look")
         .contentType(MediaType.APPLICATION_JSON)
         .content("{\"state\": \"2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ\", \"lastSuspend\": \"2:3:L_mt-1R8pxBlK9ySamZB9DDhXvPJss-GKA9igbuVMdUpTD_ZTVhdgDP8q5RA6EXCm0YQgspkyker8yFH7bNLc88OUUIrscGccSVDEfGYUJRLrnWC44vqRqkQP8JxusPyN6xo3m3wddq6ZeP9vyeZLAwL\"}");

      this.mvc.perform(request)
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.output", is("\nYou are in a room dimly lit by torches. Light is pouring\nin through a large opening to the west. A passage goes\nsouth.\n")))
         .andExpect(jsonPath("$.prompt", is(":")))
         .andExpect(jsonPath("$.suspend", is(false)))
         .andExpect(jsonPath("$.state", is("2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ")));
   }

   @Test
   public void caveShouldReturnCorrectResponseWhenResumingInDimlyLitRoomWithSuspendedGame() throws Exception {
      MockHttpServletRequestBuilder request = post("/adventures/cave/resume")
         .contentType(MediaType.APPLICATION_JSON)
         .content("{\"state\": \"2:3:L_mt-yR4ZxFlbszibiYEcyRD3phIw6RRQRDKprSREZVYWWT5qVUQmpUB7brmQyAcnX5GiWulvuoNVx2pDgnQsGdOe6oqHTqNZupdtDuv7buSMRGYYKZhBvI3HNxQIv1q9_d33oWrL9nCKb2plSGexQwJ\", \"lastSuspend\": \"2:3:L_mt-1R8pxBlK9ySamZB9DDhXvPJss-GKA9igbuVMdUpTD_ZTVhdgDP8q5RA6EXCm0YQgspkyker8yFH7bNLc88OUUIrscGccSVDEfGYUJRLrnWC44vqRqkQP8JxusPyN6xo3m3wddq6ZeP9vyeZLAwL\"}");

      this.mvc.perform(request)
         .andExpect(status().isOk())
         .andExpect(jsonPath("$.output", is("\nTo the east is a large opening in a wall of rock.\nYou are surrounded by a dense forest.\n")))
         .andExpect(jsonPath("$.prompt", is(":")))
         .andExpect(jsonPath("$.suspend", is(false)))
         .andExpect(jsonPath("$.state", is("2:3:L_mt-1R8pxBlK9ySamZB9DDhXvPJss-GKA9igbuVMdUpTD_ZTVhdgDP8q5RA6EXCm0YQgspkyker8yFH7bNLc88OUUIrscGccSVDEfGYUJRLrnWC44vqRqkQP8JxusPyN6xo3m3wddq6ZeP9vyeZLAwL")));
   }
}
