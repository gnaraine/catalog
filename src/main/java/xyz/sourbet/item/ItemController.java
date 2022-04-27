package xyz.sourbet.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import xyz.sourbet.image.ImageRepository;
import xyz.sourbet.storage.StorageService;

@Controller
public class ItemController {

    @Autowired
    StorageService storageService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ImageRepository imageRepository;

    @GetMapping(value = "/items/id={itemId}")
    public String home(@PathVariable String itemId, Model model) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            model.addAttribute("item", item);

            List<String> images = new ArrayList<>();

            item.getImages().forEach(Id -> {
                images.add(imageRepository.getImage(Id));
            });

            item.setImages(images);

            return "item";
        }
        return "item-not-found";
    }

    @GetMapping(value = "/items/id={itemId}/edit")
    public String edit(@PathVariable String itemId, Model model) {
        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();

            List<String> images = new ArrayList<>();

            item.getImages().forEach(Id -> {
                images.add(imageRepository.getImage(Id));
            });

            item.setImages(images);

            model.addAttribute("item", item);
            return "edit";
        }
        return "item-not-found";
    }

    @PostMapping(value = "/items/id={itemId}/edit")
    public ModelAndView saveEdit(
            @RequestPart("itemId") String itemId,
            @RequestPart("itemName") String itemName,
            @RequestPart("itemPrice") String itemPrice,
            @RequestPart("itemDescription") String itemDescription,

            @RequestParam(value = "file", required = false) MultipartFile[] files,
            RedirectAttributes redirectAttributes) {

        List<String> fileNames = new ArrayList<>();
        List<String> imageIds = new ArrayList<>();

        try {
            Arrays.asList(files).stream().forEach(file -> {
                if (!file.isEmpty()) {
                    String fileId = storageService.storeAndGetIds(file);
                    imageIds.add(fileId);
                    fileNames.add(file.getOriginalFilename());
                    System.out.println("Uploaded the files successfully: " + fileNames + "ID: " +
                            imageIds);
                }

            });

        } catch (Exception e) {

        }

        Optional<Item> optionalItem = itemRepository.findById(itemId);
        Item item = new Item();

        // load original data
        if (optionalItem.isPresent()) {
            item = optionalItem.get();
        }

        // set changes
        item.setName(itemName);
        item.setPrice(Integer.parseInt(itemPrice));
        item.setDescription(itemDescription);

        // combine array of original images and new images
        List<String> combinedImageIds = new ArrayList<>();
        combinedImageIds.addAll(item.getImages());
        combinedImageIds.addAll(imageIds);
        item.setImages(combinedImageIds);

        itemRepository.save(item);

        return new ModelAndView("redirect:/manage");
    }

    @RequestMapping(value = "/items/id={itemId}/delete", method = { RequestMethod.DELETE, RequestMethod.GET })
    public ModelAndView delete(@PathVariable(value = "itemId") String itemId) {
        System.out.println("Delete " + itemId);

        Optional<Item> optionalItem = itemRepository.findById(itemId);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            // delete all images in gridfs db
            item.getImages().forEach(id -> {
                storageService.delete(id);
            });
            // delete item from db
            itemRepository.deleteById(itemId);
        }

        return new ModelAndView("redirect:/manage");
    }

    @PostMapping("/new")
    public ModelAndView addNewItem(

            @RequestPart("itemName") String itemName,
            @RequestPart("itemPrice") String itemPrice,
            @RequestPart("itemDescription") String itemDescription,

            @RequestParam(value = "file", required = false) MultipartFile[] files,

            RedirectAttributes redirectAttributes) {

        List<String> fileNames = new ArrayList<>();
        List<String> imageIds = new ArrayList<>();

        // store images in db
        try {
            Arrays.asList(files).stream().forEach(file -> {
                String fileId = storageService.storeAndGetIds(file);
                imageIds.add(fileId);
                fileNames.add(file.getOriginalFilename());
                System.out.println("Uploaded the files successfully: " + fileNames + "ID: " +
                        imageIds);
            });

        } catch (Exception e) {

        }

        Item item = new Item();
        item.setName(itemName);
        item.setPrice(Integer.parseInt(itemPrice));
        item.setDescription(itemDescription);
        item.setImages(imageIds);
        itemRepository.save(item);

        return new ModelAndView("redirect:/new");
    }

    @GetMapping(value = "/new")
    public String addItem(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null || principal.getAttribute("login") == null) {
            return "/index";
        }

        return "/new";
    }

    @GetMapping(value = "/manage")
    public String manageItems(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null || principal.getAttribute("login") == null) {
            return "/index";
        }

        List<Item> items = new ArrayList<>();
        itemRepository.findAll().forEach(item -> {
            items.add(item);
            List<String> images = new ArrayList<>();

            item.getImages().forEach(Id -> {
                images.add(imageRepository.getImage(Id));
            });

            item.setImages(images);

        });

        model.addAttribute("allItems", items);

        return "manage";

    }

}
