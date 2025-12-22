package com.foodapp.foodhub.service;

import com.foodapp.foodhub.dto.cart.CheckoutRequestDTO;
import com.foodapp.foodhub.entity.*;
import com.foodapp.foodhub.enums.OrderStatus;
import com.foodapp.foodhub.enums.PaymentStatus;
import com.foodapp.foodhub.exceptions.CartEmptyException;
import com.foodapp.foodhub.exceptions.CartNotFoundException;
import com.foodapp.foodhub.exceptions.InvalidOrderStatusTransitionException;
import com.foodapp.foodhub.exceptions.OrderNotFoundException;
import com.foodapp.foodhub.exceptions.OutsideDeliveryZoneException;
import com.foodapp.foodhub.exceptions.RestaurantNotFoundException;
import com.foodapp.foodhub.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    private final ZoneService zoneService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order checkout(CheckoutRequestDTO request) {


        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);

        // CHECK the customer's delivery address is still in the zone
        boolean isInZone = zoneService.isWithinDeliveryZone(
                request.getUserLatitude(),
                request.getUserLongitude(),
                restaurant.getLatitude(),
                restaurant.getLongitude(),
                restaurant.getDeliveryRadiusInKm()
        );

        if (!isInZone) {
            throw new OutsideDeliveryZoneException();
        }
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(CartNotFoundException::new);

        if (cart.getItems().isEmpty()) {
            throw new CartEmptyException();
        }

        User user = cart.getUser();

        // Create Order Address
        OrderAddress address = new OrderAddress();
        address.setAddress(request.getAddress());
        address.setPhone(request.getPhone());
        address.setNotes(request.getNotes());
        address.setArea(request.getArea());
        address.setStreet(request.getStreet());
        address.setBuilding(request.getBuilding());
        address.setFloor(request.getFloor());
        address.setApartment(request.getApartment());

        // 3 Create Order entity
        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(cart.getRestaurant());
        order.setOrderAddress(address);
        order.setStatus(OrderStatus.PENDING);

        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        // Copy CartItems to OrderItems
        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getMeal().getPrice());

            orderItems.add(orderItem);

            // Calculate total
            total = total.add(
                    cartItem.getMeal().getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);


        Order savedOrder = orderRepository.save(order);

        //  Delete cart after checkout
        cartItemRepository.deleteAll(cart.getItems());
        cartRepository.delete(cart);

        return savedOrder;
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        OrderStatus currentStatus = order.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            throw new InvalidOrderStatusTransitionException("Invalid status transition: "
                    + currentStatus + " → " + newStatus);
        }

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }




    private boolean isValidTransition(OrderStatus current, OrderStatus next) {

        return switch (current) {
            case PENDING -> (next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED);

            case CONFIRMED -> (next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED);

            case PREPARING -> (next == OrderStatus.OUT_FOR_DELIVERY);

            case OUT_FOR_DELIVERY -> (next == OrderStatus.DELIVERED);

            case DELIVERED -> false;

            case CANCELLED -> false;
        };
    }
    @Transactional
    public void completeOrderPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }


}
