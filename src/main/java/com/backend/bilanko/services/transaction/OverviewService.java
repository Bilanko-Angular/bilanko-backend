package com.backend.bilanko.services.transaction;

import com.backend.bilanko.DTO.concept.transaction.ChargeSummaryDTO;
import com.backend.bilanko.DTO.concept.transaction.OverviewSummaryDTO;
import com.backend.bilanko.DTO.concept.transaction.SaleSummaryDTO;
import com.backend.bilanko.DTO.object.product.StockOverviewDTO;
import com.backend.bilanko.models.person.User;
import com.backend.bilanko.services.object.product.ProductServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OverviewService {

    private final SaleService saleService;
    private final ChargeService chargeService;
    private final ProductServices productServices;

    public OverviewSummaryDTO getSummary(User currentUser, LocalDateTime from, LocalDateTime to) {
        SaleSummaryDTO sales = saleService.getSummary(currentUser, from, to);

        LocalDate chargeFrom = from != null ? from.toLocalDate() : null;
        LocalDate chargeTo = to != null ? to.toLocalDate() : null;
        ChargeSummaryDTO charges = chargeService.getSummary(currentUser, chargeFrom, chargeTo);

        StockOverviewDTO stock = productServices.getStockOverview(currentUser.getEmail());

        double netProfit = sales.totalAmount() - charges.totalAmount();

        return new OverviewSummaryDTO(
                sales.totalAmount(),
                sales.totalMargin(),
                charges.totalAmount(),
                netProfit,
                sales.salesCount(),
                charges.chargesCount(),
                stock,
                from,
                to
        );
    }
}
