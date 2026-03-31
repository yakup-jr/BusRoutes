package ru.teamscore.busroutes.web.controllers.admin;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.InUseException;

@ControllerAdvice
public class AdminExceptionHandler {

    @ExceptionHandler(InUseException.class)
    public String handleInUseException(InUseException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "adminpanel/stops";
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public String handleAlreadyExistsException(AlreadyExistsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

        return "redirect:/adminpanel/stops/create";
    }
}
