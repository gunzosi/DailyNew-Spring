package code.mentor.service;

import code.mentor.dto.SearchCriteria;
import code.mentor.models.Post;
import code.mentor.repository.PostRepository;
import code.mentor.service.iService.PostService;
import com.intuit.fuzzymatcher.component.MatchService;
import com.intuit.fuzzymatcher.domain.Document;
import com.intuit.fuzzymatcher.domain.Element;
import com.intuit.fuzzymatcher.domain.ElementType;
import com.intuit.fuzzymatcher.domain.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MatchService matchService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
        this.matchService = new MatchService(); // Khởi tạo MatchService
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Post getPostById(int id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    @Override
    @Transactional
    public Post savePost(Post post) {
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post updatePost(Post post) {
        if (!postRepository.existsById(post.getId())) {
            throw new RuntimeException("Post not found with id: " + post.getId());
        }
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePostById(int id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
    }

    public Optional<List<Post>> searchPostsByBody(SearchCriteria criteria) {
        return Optional.ofNullable(postRepository.findByTitleContaining(criteria.getQuery()));
    }


//    @Override
//    public List<Post> searchFuzzyByTitle(String keyword) {
//        // Lấy danh sách tất cả bài viết
//        List<Post> posts = postRepository.findAll();
//
//        // Chuyển các bài viết thành các Document để so khớp fuzzy
//        List<Document> postDocs = posts.stream()
//                .map(post -> new Document.Builder(String.valueOf(post.getId()))
//                        .addElement(new Element(ElementType.TEXT, post.getTitle(), post.getTitle(), 1.0, 0.3, 0.9, null, null, null))
//                        .createDocument())
//                .collect(Collectors.toList());
//
//        // Tạo Document cho từ khóa tìm kiếm
//        Document keywordDoc = new Document.Builder("keyword")
//                .addElement(new Element(ElementType.TEXT, keyword, keyword, 1.0, 0.3, 0.9, null, null, null)) // Sử dụng TEXT thay vì KEYWORD nếu KEYWORD không tồn tại
//                .createDocument();
//
//        // Thực hiện tìm kiếm fuzzy
//        Map<Document, List<Match<Document>>> matches = matchService.applyMatch(keywordDoc, postDocs);
//
//        // Lọc các bài viết phù hợp từ kết quả
//        return matches.keySet().stream()
//                .flatMap(document -> posts.stream()
//                        .filter(post -> String.valueOf(post.getId()).equals(document.getKey()))
//                        .findFirst().stream())
//                .collect(Collectors.toList());
//    }

    @Override
    public List<Post> searchFuzzyByTitle(String keyword) {
        List<Post> posts = postRepository.findAll();

        // Log danh sách các bài viết
        posts.forEach(post -> System.out.println("Post ID: " + post.getId() + ", Title: " + post.getTitle()));

        List<Document> postDocs = posts.stream()
                .map(post -> new Document.Builder(String.valueOf(post.getId()))
                        .addElement(new Element.Builder()
                                .setType(ElementType.TEXT)
                                .setValue(post.getTitle())
                                .createElement())
                        .createDocument())
                .collect(Collectors.toList());

        Document keywordDoc = new Document.Builder("keyword")
                .addElement(new Element.Builder()
                        .setType(ElementType.TEXT)
                        .setValue(keyword)
                        .createElement())
                .createDocument();

        Map<Document, List<Match<Document>>> matches = matchService.applyMatch(keywordDoc, postDocs);

        List<Post> matchingPosts = matches.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .filter(match -> match.getResult() > 4)
                .map(Match::getMatchedWith)
                .flatMap(document -> posts.stream()
                        .filter(post -> String.valueOf(post.getId()).equals(document.getKey()))
                        .findFirst().stream())
                .collect(Collectors.toList());

        System.out.println("Found posts: " + matchingPosts.size());
        matchingPosts.forEach(post -> System.out.println("Post ID: " + post.getId() + ", Title: " + post.getTitle()));

        return matchingPosts;
    }


    @Override
    public Page<Post> getAllPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,
                "pubDate",
                "updatedAt"));
        return postRepository.findAll(pageable);
    }

    @Override
    public List<Post> getPostsByCategories(List<Integer> categoryIds) {
        return postRepository.findByCategoryIdIn(categoryIds);
    }


}
