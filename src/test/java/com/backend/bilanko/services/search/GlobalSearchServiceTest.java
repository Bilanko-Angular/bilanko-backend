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
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    // -------------------------------------------------------------------------
    // Les méthodes @Async ne s'exécutent PAS de façon asynchrone en test unitaire
    // (pas de contexte Spring), donc on peut appeler service.search() directement.
    // Les nouveaux mocks ciblent les méthodes de recherche optimisées.
    // -------------------------------------------------------------------------

    @Test
    void searchesProductsSalesAndChargesByStringFields() {
        User merchant = User.builder().email("merchant@example.com").build();
        Product product = Product.builder().name("Café premium").reference("CAF-001").build();
        Sale sale = Sale.builder()
                .customerName("Client fidèle")
                .items(List.of(SaleItem.builder().product(product).build()))
                .build();
        Charge charge = Charge.builder().label("Transport café").supplier("Fournisseur local").build();

        givenMerchantData(List.of(product), List.of(sale), List.of(charge));

        List<GlobalSearchResultDTO> results = service.search("CAFÉ", merchant);

        assertThat(results).extracting(GlobalSearchResultDTO::type)
                .containsExactlyInAnyOrder("PRODUCT", "SALE", "CHARGE");
    }

    @Test
    void limitsResultsToFivePerType() {
        User merchant = User.builder().email("merchant@example.com").build();

        // La BDD retourne exactement MAX_RESULTS_PER_TYPE résultats par source
        List<Product> products = IntStream.range(0, GlobalSearchService.MAX_RESULTS_PER_TYPE)
                .mapToObj(i -> Product.builder().name("matching-" + i).reference("ref-" + i).build())
                .toList();
        List<Sale> sales = IntStream.range(0, GlobalSearchService.MAX_RESULTS_PER_TYPE)
                .mapToObj(i -> Sale.builder().customerName("matching-" + i).build())
                .toList();
        List<Charge> charges = IntStream.range(0, GlobalSearchService.MAX_RESULTS_PER_TYPE)
                .mapToObj(i -> Charge.builder().label("matching-" + i).build())
                .toList();

        givenMerchantData(products, sales, charges);

        List<GlobalSearchResultDTO> results = service.search("matching", merchant);

        // 5 produits + 5 ventes + 5 charges = 15 max
        assertThat(results).hasSize(GlobalSearchService.MAX_RESULTS_PER_TYPE * 3);
        assertThat(results).extracting(GlobalSearchResultDTO::type)
                .containsExactlyInAnyOrderElementsOf(
                        IntStream.range(0, GlobalSearchService.MAX_RESULTS_PER_TYPE)
                                .boxed()
                                .flatMap(i -> List.of("PRODUCT", "SALE", "CHARGE").stream())
                                .toList()
                );
    }

    @Test
    void rejectsBlankKeyword() {
        User merchant = User.builder().email("merchant@example.com").build();

        assertThatThrownBy(() -> service.search("  ", merchant))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("keyword");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void givenMerchantData(List<Product> products, List<Sale> sales, List<Charge> charges) {
        when(productRepository.searchByUserEmailAndKeyword(anyString(), anyString()))
                .thenReturn(products);
        when(saleRepository.searchByKeywordFetched(any(User.class), anyString(), any(Pageable.class)))
                .thenReturn(sales);
        when(chargeRepository.searchByUserAndKeyword(any(User.class), anyString(), any(Pageable.class)))
                .thenReturn(charges);
    }
}
