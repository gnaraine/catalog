package xyz.sourbet.home;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import xyz.sourbet.image.ImageRepository;
import xyz.sourbet.item.Item;
import xyz.sourbet.item.ItemRepository;
import xyz.sourbet.storage.StorageService;

@Controller
public class HomeController {
    @Autowired
    StorageService storageService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ImageRepository imageRepository;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null || principal.getAttribute("login") == null) {
            return "index";
        }
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<Item> items = new ArrayList<>();
        itemRepository.findAll().forEach(item -> {
            items.add(item);
            if (!item.getImages().isEmpty()) {
                List<String> images = new ArrayList<>();
                images.add(imageRepository.getImage(item.getImages().get(0))); // get the first image only
                item.setImages(images);
            }

        });
        model.addAttribute("allItems", items);
        return "home";
    }

}
