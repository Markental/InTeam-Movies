package com.project.third.controllers;

import com.project.third.Repositories.*;
import com.project.third.models.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MainController {

    final BCryptPasswordEncoder passwordEncoder;
    final UsersRepository usersRepository;
    final PostsRepository postsRepository;
    final RolesRepository rolesRepository;
    final CommentsRepository commentsRepository;
    final GenresRepositories genresRepositories;
    final AuthenticationManager authenticationManager;


    @Autowired
    public MainController(UsersRepository usersRepository, PostsRepository postsRepository, CommentsRepository commentsRepository, RolesRepository rolesRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, GenresRepositories genresRepositories) {
        this.usersRepository = usersRepository;
        this.postsRepository = postsRepository;
        this.commentsRepository = commentsRepository;
        this.rolesRepository = rolesRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.genresRepositories = genresRepositories;
    }

    /*
     *
     * MAIN SECTION
     *
     * */

    @GetMapping(path = "/")
    public String index(Model model,
                        @RequestParam(name = "key", defaultValue = "", required = false) String key,
                        @RequestParam(name = "page", defaultValue = "1") int page) {

        int size = (int) postsRepository.count();
        int tabSize = (size+11)/12;

        if(page<1){
            page = 1;
        }

        Pageable pageable = PageRequest.of(page-1,12);
        List<Post> posts = postsRepository.findAll(pageable).toList();

        model.addAttribute("posts", posts);
        model.addAttribute("tabSize", tabSize);

        model.addAttribute("user", getUserData());
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);

        return "index";
    }

    @GetMapping(path = "/search")
    public String list(Model model,
                       @RequestParam(name="query")String query) {
        List<Post> allPosts = postsRepository.findAll();
        List<Post> found = new ArrayList<>();

        for (Post post:allPosts) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase()))
            {
                found.add(post);
            }
        }
        model.addAttribute("posts", found);

        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "search"; // create search.html!!
    }

    @PostMapping(path = "/signUp")
    public String signUp(ModelMap model,
                         @RequestParam(name="email")String email,
                         @RequestParam(name="password")String password,
                         @RequestParam(name="repassword")String repassword,
                         @RequestParam(name="surname")String surname,
                         @RequestParam(name="name")String name){

        Roles role = new Roles(1L, "ROLE_USER", "User");
        HashSet<Roles> roles = new HashSet<Roles>();
        roles.add(role);
        Users user = new Users(null, email, passwordEncoder.encode(password), name, surname, true, roles);
        usersRepository.save(user);
        return "redirect:/";
    }

    @GetMapping(path = "/post/{id}")
    public String post(Model model,
                       @RequestParam(name = "key", defaultValue = "", required = false) String key,
                       @PathVariable(name="id")long id){
        model.addAttribute("user", getUserData());
        Post post = postsRepository.findById(id).get();
        List<Comment> comments = commentsRepository.getCommentsByPost(post);
        comments.sort(Comparator.comparing(Comment::getPostDate)); //sort by date

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);

        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);

        return "single";
    }

    @GetMapping(path = "/login")
    public String enter(Model model){

        return "login";

    }

    @GetMapping(path = "/signup")
    public String register(Model model){

        return "signup";

    }


////////////////////////////////////!!!!!!!!!!!
    @GetMapping(path = "/genre/{name}")
    public String genre(Model model,
                        @PathVariable(name="name")String name) {
        Genre selected = genresRepositories.findByName(name);

        List<Post> posts = postsRepository.findAllByGenresContains(selected);
        model.addAttribute("posts", posts);

        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "genre";
    }

    @GetMapping(path = "/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model){

        model.addAttribute("user", getUserData());
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "profile";
    }

    @PostMapping(path = "/addComment")
    @PreAuthorize("isAuthenticated()")
    public String addComment(ModelMap model,
                             @RequestParam(name="add_comment_post_id")Long id,
                             @RequestParam(name="comment")String comment) {
        commentsRepository.save(new Comment(getUserData(), postsRepository.findById(id).get(), comment));
        return "redirect:/post/"+id;
    }

    @GetMapping(path = "/editComment/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editCommentPage(ModelMap model,
                                  @PathVariable(name="id")Long id) {
        Comment comment = commentsRepository.findById(id).get();
        model.addAttribute("comment", comment);
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "editCommentPage";
    }

    @PostMapping(path = "/editComment/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editComment(ModelMap model,
                              @PathVariable(name="id")Long id,
                              @RequestParam(name="comment")String comment) {
        Comment c = commentsRepository.findById(id).get();
        c.setComment(comment);
        commentsRepository.save(c);
        return "redirect:/post/"+c.getPost().getId();
    }

    @PostMapping(path = "/deleteComment")
    @PreAuthorize("isAuthenticated()")
    public String deleteComment(ModelMap model,
                                @RequestParam(name="comment_id")Long id) {
        Comment comment = commentsRepository.findById(id).get();
        //commentsRepository.save(comment);
        commentsRepository.delete(comment);
        return "redirect:/post/"+comment.getPost().getId();
    }

    @GetMapping(path = "/editPost/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editPostPage(ModelMap model,
                               @PathVariable(name="id")Long id) {
        model.addAttribute("post", postsRepository.findById(id).get());
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "editPost";
    }

    @PostMapping(path = "/editPost")
    @PreAuthorize("isAuthenticated()")
    public String editPost(ModelMap model,
                           @RequestParam(name="id")Long id,
                           @RequestParam(name="title")String title,
                           @RequestParam(name="posterUrl")String posterUrl,
                           @RequestParam(name="shortContent")String shortContent,
                           @RequestParam(name="content")String content,
                           @RequestParam(name="genres")long[] genres_ids) {
        Post post = postsRepository.findById(id).get();
        post.setTitle(title);
        post.setShortContent(shortContent);
        post.setContent(content);
        post.setPosterURL(posterUrl);
        HashSet<Genre> genres = new HashSet<>();
        for (long idid : genres_ids) {
            Genre temp = genresRepositories.getOne(idid);
            if(temp!=null) {
                genres.add(temp);
            }
        }
        post.setGenres(genres);

        postsRepository.save(post);
        return "redirect:/post/"+id;
    }

    @PostMapping(path = "deletePost")
    @PreAuthorize("isAuthenticated()")
    public String deletePost(ModelMap model,
                             @RequestParam(name="id")Long id) {
        Post post = postsRepository.findById(id).get();
        List<Comment> comments =  commentsRepository.getCommentsByPost(post);
        for (Comment comment: comments) {
            commentsRepository.delete(comment);
        }
        postsRepository.deleteById(id);
        return "redirect:/";
    }

    @GetMapping(path = "/addPost")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String addPostPage(ModelMap model) {
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);

        return "addPost";
    }

    @PostMapping(path = "/addPost")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String addPost(ModelMap model,
                          @RequestParam(name="title")String title,
                          @RequestParam(name="posterUrl")String posterUrl,
                          @RequestParam(name="shortContent")String shortContent,
                          @RequestParam(name="content")String content,
                          @RequestParam(name="genres")long[] genres_ids) {
        HashSet<Genre> genres = new HashSet<>();
        for (long id : genres_ids) {
            Genre temp = genresRepositories.getOne(id);
            if(temp!=null) {
                genres.add(temp);
            }
        }

        postsRepository.save(new Post(title, posterUrl, shortContent, content, genres, getUserData()));
        return "redirect:/";
    }




    /*
     *
     * Profile SECTION
     *
     * */

    @PostMapping(value = "/changePassword")
    @PreAuthorize("isAuthenticated()")
    public String changePassword(ModelMap model,
                                 @RequestParam(name="oldPassword")String oldPassword,
                                 @RequestParam(name="newPassword")String newPassword,
                                 @RequestParam(name="repeatNewPassword")String repeat)
    {
        Users user = getUserData();
        if (user != null) {

            if (newPassword.equals(repeat)) {
                if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    usersRepository.save(user);
                    return "redirect:/logout";
                }
            }
//              else{
//                //error here
//            }
        }
        return "redirect:/index";
    }

    @PostMapping(value = "/changeFullName")
    @PreAuthorize("isAuthenticated()")
    public String changeFullName(ModelMap model,
                                 @RequestParam(name="newSurname")String surname,
                                 @RequestParam(name="newName")String name)
    {
        Users user = getUserData();
        if(user!=null) {
            user.setSurname(surname);
            user.setName(name);
            usersRepository.save(user);
        }
        model.addAttribute("user", user);
        return "redirect:/profile";
    }

    @PostMapping(value = "/changeEmail")
    @PreAuthorize("isAuthenticated()")
    public String changeEmail(ModelMap model,
                                 @RequestParam(name="newEmail")String email)
    {
        Users user = getUserData();
        if(user != null)
        {
            user.setEmail(email);
            usersRepository.save(user);

//            Authentication request = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
//            Authentication result = authenticationManager.authenticate(request);
//            SecurityContextHolder.getContext().setAuthentication(result);


        }
        return "redirect:/logout";
    }
    /*
    *
    * SUPERUSER SECTION
    *
    * */


    @GetMapping(path = "/adminPage")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String adminPage(ModelMap model) {
        List<Users> users = usersRepository.findAll();
        users.sort(Comparator.comparing(Users::getId));
        model.addAttribute("users", users);
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "adminPage";
    }

    @GetMapping(path = "/moderatorPage")
    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    public String moderatorPage(ModelMap model) {

        Roles moderatorRole = new Roles(2L, "ROLE_MODERATOR", "Moderator");
        Roles adminRole = new Roles(3L, "ROLE_ADMIN", "Administrator");

        List<Users> allUsers = usersRepository.findAll();
        List<Users> users = new ArrayList<>();
        for (Users u : allUsers) {
            boolean toAdd = true;
            for (Roles r : u.getRoles()) {
                if (r.getRole().equals("ROLE_MODERATOR") || r.getRole().equals("ROLE_ADMIN")) {
                    toAdd = false;
                }
            }
            if (toAdd) {
                users.add(u);
            }
        }
        //users.removeIf(user -> user.getRoles().contains(moderatorRole) || user.getRoles().contains(adminRole));

        users.sort(Comparator.comparing(Users::getId));

        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);

        model.addAttribute("users", users);
        return "moderatorPage";
    }

    @GetMapping(path = "/editUser/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String editUserPage(ModelMap model,
                               @PathVariable(name="id")Long id) {
        Users user = usersRepository.findById(id).get();
        boolean isModerator = false;
        for (Roles r : user.getRoles()) {
            isModerator = r.getRole().equals("ROLE_MODERATOR");
            if(isModerator){
                break;
            }
        }
        model.addAttribute("isModerator", isModerator);
        model.addAttribute("user", user);
        List<Genre> genres = genresRepositories.findAll();
        model.addAttribute("genres", genres);
        return "editUser";
    }

    @PostMapping(path = "/editUser")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public String editUser(ModelMap model,
                           @RequestParam(name="id")Long id,
                           @RequestParam(name="email")String email,
                           @RequestParam(name="surname")String surname,
                           @RequestParam(name="name")String name,
                           @RequestParam(name="isActive", required = false)Boolean isActive,
                           @RequestParam(name="isModerator", required = false)Boolean isModerator) {
        Users user = usersRepository.findById(id).get();
        user.setEmail(email);
        user.setIsActive(isActive);
        if(isActive != null) {
            if(isActive)
                user.setIsActive(true);
            else
                user.setIsActive(false);
        }
        else {
            user.setIsActive(false);
        }
        user.setSurname(surname);
        user.setName(name);

        if(isModerator != null) {
            if(isModerator) {
                user.getRoles().add(rolesRepository.getOne(2L));
            }
            else {
                user.getRoles().remove(rolesRepository.getOne(2L));
            }
        }
        else {
            user.getRoles().remove(rolesRepository.getOne(2L));
        }

        usersRepository.save(user);
        return "redirect:/profile";
    }

    public Users getUserData(){
        Users userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            User secUser = (User)authentication.getPrincipal();
            userData = usersRepository.findByEmail(secUser.getUsername());
        }
        return userData;
    }
}
