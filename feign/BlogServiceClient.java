@FeignClient(name = "blog-service", url = "http://blog-service:8082", configuration = FeignConfig.class)
public interface BlogServiceClient {

    @GetMapping("/api/posts")
    ResponseEntity<List<BlogPostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    // Magnum Opus: Domänspecifikt ID för blogginlägg
    @GetMapping("/api/posts/{postId}")
    ResponseEntity<BlogPostDto> getPost(@PathVariable("postId") Long postId);

    @GetMapping("/api/posts/published")
    ResponseEntity<List<BlogPostDto>> getPublishedPosts();
}