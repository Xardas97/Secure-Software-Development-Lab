package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.ForbiddenException;
import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.SecurityUtil;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.PersonRepository;
import com.zuehlke.securesoftwaredevelopment.repository.RoleRepository;
import com.zuehlke.securesoftwaredevelopment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Controller

public class PersonsController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonsController.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(PersonRepository.class);

    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public PersonsController(PersonRepository personRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/persons/{id}")
    @PreAuthorize("hasAuthority('VIEW_PERSON')")
    public String person(@PathVariable int id, Model model, HttpSession session) {
        model.addAttribute("CSRF_TOKEN", session.getAttribute("CSRF_TOKEN"));

        model.addAttribute("person", personRepository.get("" + id));
        return "person";
    }

    @GetMapping("/myprofile")
    @PreAuthorize("hasAuthority('VIEW_MY_PROFILE')")
    public String self(Model model, Authentication authentication, HttpSession session) {
        model.addAttribute("CSRF_TOKEN", session.getAttribute("CSRF_TOKEN"));

        User user = (User) authentication.getPrincipal();
        model.addAttribute("person", personRepository.get("" + user.getId()));
        return "person";
    }

    @DeleteMapping("/persons/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PERSON')")
    public ResponseEntity<Void> person(@PathVariable int id) {
        if (!canUpdatePerson(id))
            throw new ForbiddenException();

        personRepository.delete(id);
        userRepository.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-person")
    @PreAuthorize("hasAuthority('UPDATE_PERSON')")
    public String updatePerson(Person person, HttpSession session, @RequestParam("csrfToken") String receivedToken) {
        String realToken = session.getAttribute("CSRF_TOKEN").toString();
        if (!realToken.equals(receivedToken))
            throw new ForbiddenException();

        if (!canUpdatePerson(person.getId()))
            throw new ForbiddenException();

        personRepository.update(person);

        if (Objects.equals(person.getId(), Integer.toString(SecurityUtil.getCurrentUser().getId())))
            return "redirect:/myprofile";

        return "redirect:/persons/" + person.getId();
    }

    private boolean canUpdatePerson(int id) {
        User currentUser = SecurityUtil.getCurrentUser();
        boolean isAdmin = roleRepository.findByUserId(currentUser.getId()).stream().anyMatch(r -> r.getName().equals("ADMIN"));
        boolean isThisUser = currentUser.getId() == id;
        return isAdmin || isThisUser;
    }

    private boolean canUpdatePerson(String idString) {
        try{
            int id = Integer.parseInt(idString);
            return canUpdatePerson(id);
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }

    @GetMapping("/persons")
    @PreAuthorize("hasAuthority('VIEW_PERSONS_LIST')")
    public String persons(Model model) {
        model.addAttribute("persons", personRepository.getAll());
        return "persons";
    }

    @GetMapping(value = "/persons/search", produces = "application/json")
    @PreAuthorize("hasAuthority('VIEW_PERSONS_LIST')")
    @ResponseBody
    public List<Person> searchPersons(@RequestParam String searchTerm) throws SQLException {
        return personRepository.search(searchTerm);
    }
}
