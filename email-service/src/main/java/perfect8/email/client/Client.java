package perfect8.email.client;

import perfect8.email.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shop-service", url = "${services.shop.url:http://localhost:8082}")
public interface ShopServiceClient {

    @GetMapping("/api/v1/orders/{orderId}")
    OrderDto getOrderById(@PathVariable("orderId") Long orderId);
}
