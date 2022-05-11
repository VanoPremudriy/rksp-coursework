package ru.mirea.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.mirea.entity.Genre;
import ru.mirea.entity.Roles;
import ru.mirea.entity.User;
import ru.mirea.repository.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Controller
public class AuthorizationController{

    @Value("${upload.path}")
    String uploadPath;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepo userRepo;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping(value = "/AuthorizationPage")
    public String AuthorizationPage(Model model, HttpSession session){
        if (session.getAttribute("theme") == null) session.setAttribute("theme", 1);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.getUserByUsername(auth.getName());
        model.addAttribute("user",user);
        return "/AuthorizationPage_v2";
    }

    @GetMapping(value = "/RegistrationPage")
    public String RegistrationPage(Model model,HttpSession session){
        if (session.getAttribute("theme") == null) session.setAttribute("theme", 1);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.getUserByUsername(auth.getName());
        if(!model.containsAttribute("new_user")){
            model.addAttribute("new_user", new User());
        }
        model.addAttribute("user",user);
        return "/RegistrationPage_v2";
    }

    @PostMapping(value = "/AuthorizationPage")
    public String AuthorizationPagePost(){
        return "/GenrePage";
    }


    @PostMapping(value = "/registration")
    public String registration(@Valid @ModelAttribute("new_user") User newUser,BindingResult bindingResult,
                               @RequestParam("file") MultipartFile file) throws IOException {
        if (bindingResult.hasErrors()) {
            return "/RegistrationPage_v2";
        } else {
            User user = userRepo.getUserByUsername(newUser.getUsername());
            if (user != null) {
                System.out.println("Пользователь существует!");
                return "/RegistrationPage_v2";
            }
            newUser.setEnabled(true);
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            newUser.setRole(Roles.ROLE_USER);
            if (!file.getOriginalFilename().equals("")) {
                String uuidFile = UUID.randomUUID().toString();
                String resultFileName = uploadPath + "/"+ uuidFile + "_" + file.getOriginalFilename();
                file.transferTo(new File(resultFileName));
                newUser.setAvatar("/avatars/" + uuidFile + "_" + file.getOriginalFilename());
            } else newUser.setAvatar("/avatars/default_avatar.png");
            userRepo.save(newUser);
            return "redirect:/AuthorizationPage_v2";
        }
    }

    @PostMapping("/editing_profile")
    public String editingProfile(String name, String lastName, @RequestParam("file") MultipartFile file) throws IOException{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.getUserByUsername(auth.getName());
        if (!file.getOriginalFilename().equals("")) {
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uploadPath + "/" + uuidFile + "_" + file.getOriginalFilename();
            file.transferTo(new File(resultFileName));
            user.setAvatar("/avatars/" + uuidFile + "_" + file.getOriginalFilename());
        }
        user.setRealName(name);
        user.setRealLastName(lastName);
        user.setPassword(user.getPassword());
        userRepo.save(user);
        return "redirect:/Profile";
    }


    @RequestMapping("/delete_avatar")
    public String deleteAvatar(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.getUserByUsername(auth.getName());
        user.setAvatar("/avatars/default_avatar.png");
        userRepo.save(user);
        return "redirect:/EditingProfile";
    }


    @GetMapping("/403")
    public String error403(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.getUserByUsername(auth.getName());
        model.addAttribute("user", user);
        return "/403_v2";
    }

}
