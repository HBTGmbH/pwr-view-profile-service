package de.hbt.pwr.view.exception;

import de.hbt.pwr.view.controller.ViewProfileController;
import de.hbt.pwr.view.controller.ViewProfileOperationsController;
import de.hbt.pwr.view.service.ViewProfileImportService;
import de.hbt.pwr.view.service.ViewProfileService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ViewProfileController.class)
@ActiveProfiles("test")
public class ViewProfileExceptionHandlerTest {
    @MockBean
    // DO NOT REMOVE. Stops the container for crashing. Don't ask why ~nt
    private JedisConnectionFactory jedisConnectionFactory;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    public ViewProfileService viewProfileService;

    @MockBean
    public ViewProfileImportService viewProfileImportService;


    @Test
    public void shouldReturnForbidden403() throws Exception {
        given(viewProfileService.getViewProfileIdsForInitials("fooBar")).willThrow(new InvalidOwnerException("12", "fooBar"));
        String url = "/view/fooBar";
        mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnNotFound404() throws Exception {
        given(viewProfileService.getViewProfileIdsForInitials("fooBar")).willThrow(new ViewProfileNotFoundException("12"));
        String url = "/view/fooBar";
        mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }
}