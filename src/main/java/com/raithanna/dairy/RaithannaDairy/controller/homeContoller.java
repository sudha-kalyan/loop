package com.raithanna.dairy.RaithannaDairy.controller;

import com.raithanna.dairy.RaithannaDairy.models.userModel;
import com.raithanna.dairy.RaithannaDairy.repositories.UserModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class homeContoller {
    @Autowired
    private UserModelRepository userModelRepository;
    @GetMapping("/")
    public String loginHtml(){ return "welcomescreen";}

    @PostMapping("/loginPage")
    public String loginPage(){ return "loginPage";}


    @PostMapping("/")
    public String login(@RequestParam String mobile, Model model, @RequestParam String password, HttpServletRequest request, HttpSession session){
        System.out.println(mobile);
        System.out.println(password);

        List<String> messages = new ArrayList<>();

        try{
            userModel user = userModelRepository.findByMobileAndPassword(mobile,password);
            if (user == null ) {
                messages.add("Account not found! retry ");
                model.addAttribute("messages",messages);
                return "login";
            }
            System.out.println(user);}
        catch (Exception handlerException){
            messages.add("Error logging in! retry ");
            model.addAttribute("messages",messages);
            return "login";
        }
        model.addAttribute("messages",messages);
        session.setAttribute("loggedIn", "yes");
        return "home";
    }
}
