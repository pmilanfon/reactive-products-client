import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class WebClientAPI {

    private WebClient webClient;

    public WebClientAPI() {
        try {
            this.webClient = WebClient.create("http://localhost:8080/products");
        } catch (Exception e) {
            System.out.println("e");
        }
    }

    public static void main(String[] args) {
        WebClientAPI api = new WebClientAPI();

        final Mono<ResponseEntity<Product>> responseEntityMono = api.postNewProduct();

        responseEntityMono
                .thenMany(api.getAllProducts())
                .take(1)
                .flatMap(product -> api.updateProduct(product.getId(), "Old Fashioned", 6.50))
                .flatMap(product -> api.deleteProduct(product.getId()))
                .thenMany(api.getAllProducts())
                .thenMany(api.getAllEvents())
                .subscribe(System.out::println);
    }

    private Mono<ResponseEntity<Product>> postNewProduct() {
        return webClient
                .post()
                .body(Mono.just(new Product(null, "Long Island", 6.99)), Product.class)
                .exchange()
                .flatMap(clientResponse -> clientResponse.toEntity(Product.class))
                .doOnSuccess(entity -> System.out.println("**** POST " + entity))
                .doOnError(err -> System.out.println("Error " + err));

    }

    private Flux<Product> getAllProducts() {
        return webClient
                .get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(product ->
                        System.out.println("***Client consumed product  via GET: " + product)
                );
    }

    private Mono<Product> updateProduct(String id, String name, double price) {
        return webClient
                .put()
                .uri("/id")
                .body(Mono.just(new Product(id, name, price)), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(updatedProduct -> System.out.println("*** UPDATE of product to : " + updatedProduct));
    }

    private Mono<Product> deleteProduct(String id) {
        return webClient
                .delete()
                .uri("/id")
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(dp -> System.out.println("*** DELETED product " + dp));
    }

    private Flux<ProductEvent> getAllEvents() {
        return webClient
                .get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class)
                .doOnNext(pe -> System.out.println("*** Client GET event: " + pe));
    }
}
