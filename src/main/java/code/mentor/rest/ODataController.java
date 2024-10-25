package code.mentor.rest;

import code.mentor.models.Post;
import code.mentor.service.iService.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/odata")
public class ODataController {

    private final PostService postService;

    @Autowired
    public ODataController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/Posts")
    public List<Post> getPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/Posts/{id}")
    public Post getPostById(@PathVariable int id) {
        return postService.getPostById(id);
    }
}

