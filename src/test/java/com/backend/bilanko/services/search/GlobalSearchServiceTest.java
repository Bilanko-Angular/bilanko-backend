package com.backend.bilanko.services.search;

import com.backend.bilanko.DTO.search.GlobalSearchResultDTO;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.models.transaction.SaleItem;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.transaction.ChargeRepository;
import com.backend.bilanko.repository.transaction.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ChargeRepository chargeRepository;

    @InjectMocks
    private GlobalSearchService service;

    @Test
    void searchesProductsSalesAndChargesByStringFields() {
        User merchant = User.builder().email("merchant@example.com").build();
        Product product = Product.builder().name("Café premium").reference("CAF-001").build();
        Sale sale = Sale.builder()
                .customerName("Client fidèle")
                .items(List.of(SaleItem.builder().product(product).build()))
                .build();
        Charge charge = Charge.builder().label("Transport café").supplier("Fournisseur local").build();

        givenMerchantData(product, sale, charge);

        List<GlobalSearchResultDTO> results = service.search("CAFÉ", merchant);

        assertThat(results).extracting(GlobalSearchResultDTO::type)
                .containsExactlyInAnyOrder("PRODUCT", "SALE", "CHARGE");
    }

    @Test
    void limitsCombinedResultsToTenWithoutPagination() {
        User merchant = User.builder().email("merchant@example.com").build();
        List<Product> products = java.util.stream.IntStream.range(0, 12)
                .mapToObj(index -> Product.builder().name("matching-" + index).reference("ref-" + index).build())
                .toList();
        givenMerchantData(products, List.of(), List.of());

        List<GlobalSearchResultDTO> results = service.search("matching", merchant);

        assertThat(results).hasSize(10);
        assertThat(results).allMatch(result -> result.type().equals("PRODUCT"));
    }

    @Test
    void rejectsBlankKeyword() {
        User merchant = User.builder().email("merchant@example.com").build();

        assertThatThrownBy(() -> service.search("  ", merchant))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("keyword");
    }

    private void givenMerchantData(Product product, Sale sale, Charge charge) {
        givenMerchantData(List.of(product), List.of(sale), List.of(charge));
    }

    private void givenMerchantData(List<Product> products, List<Sale> sales, List<Charge> charges) {
        when(productRepository.findByUserEmail("merchant@example.com")).thenReturn(products);
        when(saleRepository.findByUserOrderBySaleDateDesc(org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(sales);
        when(chargeRepository.findByUserOrderByDateDesc(org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(charges);
    }
}
