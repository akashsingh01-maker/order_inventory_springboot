package com.example.inventoryservice;

import com.example.inventoryservice.dto.ReservationItem;
import com.example.inventoryservice.persistence.ProductEntity;
import com.example.inventoryservice.persistence.ProductRepository;
import com.example.inventoryservice.service.InventoryReservationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InventoryReservationServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryReservationService reservationService;

    @Test
    public void testReserveSuccess() {
        ProductEntity p = new ProductEntity();
        p.setId(1L);
        p.setName("widget");
        p.setAvailable(10);
        p.setReserved(0);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));

        reservationService.reserveForOrder("order-1", Arrays.asList(new ReservationItem(1L, 4)));

        Assert.assertEquals(6, p.getAvailable());
        Assert.assertEquals(4, p.getReserved());
        verify(productRepository, times(1)).save(p);
    }

    @Test(expected = RuntimeException.class)
    public void testReserveInsufficient() {
        ProductEntity p = new ProductEntity();
        p.setId(1L);
        p.setName("widget");
        p.setAvailable(2);
        p.setReserved(0);

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));

        reservationService.reserveForOrder("order-2", Arrays.asList(new ReservationItem(1L, 4)));
    }
}
