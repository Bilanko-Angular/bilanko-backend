package com.backend.bilanko.services.dashboard;

import com.backend.bilanko.DTO.dashboard.*;
import com.backend.bilanko.models.object.product.Product;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.models.transaction.Charge;
import com.backend.bilanko.models.transaction.Sale;
import com.backend.bilanko.repository.object.product.ProductRepository;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.repository.transaction.ChargeRepository;
import com.backend.bilanko.repository.transaction.SaleRepository;
import com.backend.bilanko.utils.annotation.AdminOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int ACTIVITY_DAYS = 7;
    private static final int RECENT_SALES_LIMIT = 8;
    private static final int RECENT_USERS_LIMIT = 8;
    private static final int STOCK_ALERTS_LIMIT = 10;
    private static final int SUPPLIERS_LIMIT = 6;

    private static final String[] SUPPLIER_COLORS = {
            "#022C22", "#065F46", "#047857", "#10B981", "#34D399", "#6EE7B7"
    };

    private static final DateTimeFormatter ISO_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter ISO_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final ChargeRepository chargeRepository;

    @Override
    @Transactional(readOnly = true)
    @AdminOnly
    public DashboardSummaryDTO getSummary() {
        return DashboardSummaryDTO.builder()
                .kpis(buildKpis())
                .activity(buildActivityEvolution())
                .suppliers(buildSupplierShares())
                .recentSales(buildRecentSales())
                .recentUsers(buildRecentUsers())
                .stockAlerts(buildStockAlerts())
                .build();
    }

    private List<KpiDTO> buildKpis() {
        ZoneId zone = ZoneId.systemDefault();
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        Instant currentMonthStart = currentMonth.atDay(1).atStartOfDay(zone).toInstant();
        Instant previousMonthStart = previousMonth.atDay(1).atStartOfDay(zone).toInstant();

        LocalDateTime currentMonthStartDt = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime currentMonthEndDt = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime previousMonthStartDt = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime previousMonthEndDt = previousMonth.atEndOfMonth().atTime(23, 59, 59);

        long totalUsers = userRepository.count();
        long usersThisMonth = userRepository.countByCreatedAtBetween(currentMonthStart, Instant.now());
        long usersPreviousMonth = userRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart);

        long totalProducts = productRepository.count();
        long productsThisMonth = productRepository.countByCreatedAtBetween(currentMonthStart, Instant.now());
        long productsPreviousMonth = productRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart);

        long totalSales = saleRepository.count();
        long salesThisMonth = saleRepository.countBySaleDateBetween(currentMonthStartDt, currentMonthEndDt);
        long salesPreviousMonth = saleRepository.countBySaleDateBetween(previousMonthStartDt, previousMonthEndDt);

        double revenueThisMonth = nullToZero(
                saleRepository.sumTotalAmountByPeriod(currentMonthStartDt, currentMonthEndDt));
        double revenuePreviousMonth = nullToZero(
                saleRepository.sumTotalAmountByPeriod(previousMonthStartDt, previousMonthEndDt));

        return List.of(
                buildKpi("Utilisateurs", formatCount(totalUsers), "group",
                        usersThisMonth, usersPreviousMonth),
                buildKpi("Produits", formatCount(totalProducts), "inventory_2",
                        productsThisMonth, productsPreviousMonth),
                buildKpi("Ventes", formatCount(totalSales), "point_of_sale",
                        salesThisMonth, salesPreviousMonth),
                buildKpi("Revenu du mois", formatCurrency(revenueThisMonth), "payments",
                        revenueThisMonth, revenuePreviousMonth)
        );
    }

    private KpiDTO buildKpi(String label, String value, String icon, double current, double previous) {
        Trend trend = computeTrend(current, previous);
        return KpiDTO.builder()
                .label(label)
                .value(value)
                .icon(icon)
                .trend(trend.text())
                .trendPositive(trend.positive())
                .build();
    }

    private ActivityEvolutionDTO buildActivityEvolution() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(ACTIVITY_DAYS - 1L);

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = today.atTime(23, 59, 59);

        Map<LocalDate, Double> caByDay = new HashMap<>();
        Map<LocalDate, Double> margeByDay = new HashMap<>();
        Map<LocalDate, Double> chargesByDay = new HashMap<>();

        for (int i = 0; i < ACTIVITY_DAYS; i++) {
            LocalDate day = start.plusDays(i);
            caByDay.put(day, 0.0);
            margeByDay.put(day, 0.0);
            chargesByDay.put(day, 0.0);
        }

        for (Sale sale : saleRepository.findBySaleDateBetween(from, to)) {
            LocalDate day = sale.getSaleDate().toLocalDate();
            caByDay.merge(day, sale.getTotalAmount(), Double::sum);
            margeByDay.merge(day, sale.getTotalMargin(), Double::sum);
        }

        for (Charge charge : chargeRepository.findByDateBetween(start, today)) {
            chargesByDay.merge(charge.getDate(), charge.getAmount(), Double::sum);
        }

        List<String> labels = new ArrayList<>(ACTIVITY_DAYS);
        List<Double> ca = new ArrayList<>(ACTIVITY_DAYS);
        List<Double> marge = new ArrayList<>(ACTIVITY_DAYS);
        List<Double> charges = new ArrayList<>(ACTIVITY_DAYS);

        for (int i = 0; i < ACTIVITY_DAYS; i++) {
            LocalDate day = start.plusDays(i);
            labels.add(capitalize(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH)));
            ca.add(round2(caByDay.getOrDefault(day, 0.0)));
            marge.add(round2(margeByDay.getOrDefault(day, 0.0)));
            charges.add(round2(chargesByDay.getOrDefault(day, 0.0)));
        }

        return ActivityEvolutionDTO.builder()
                .labels(labels)
                .ca(ca)
                .marge(marge)
                .charges(charges)
                .build();
    }

    private List<SupplierShareDTO> buildSupplierShares() {
        List<Object[]> rows = chargeRepository.findSupplierShares(PageRequest.of(0, SUPPLIERS_LIMIT));
        List<SupplierShareDTO> result = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            String name = (String) row[0];
            double amount = ((Number) row[1]).doubleValue();
            long count = ((Number) row[2]).longValue();

            result.add(SupplierShareDTO.builder()
                    .name(name)
                    .desc(supplierFrequencyDesc(count))
                    .amount(round2(amount))
                    .color(SUPPLIER_COLORS[i % SUPPLIER_COLORS.length])
                    .build());
        }
        return result;
    }

    private List<RecentSaleDTO> buildRecentSales() {
        return saleRepository.findAllByOrderBySaleDateDesc(PageRequest.of(0, RECENT_SALES_LIMIT))
                .stream()
                .map(sale -> RecentSaleDTO.builder()
                        .id(sale.getId())
                        .date(sale.getSaleDate().format(ISO_DATE_TIME))
                        .client(sale.getCustomerName() != null && !sale.getCustomerName().isBlank()
                                ? sale.getCustomerName()
                                : "Client anonyme")
                        .items(sale.getItems() != null ? sale.getItems().size() : 0)
                        .total(round2(sale.getTotalAmount()))
                        .margin(round2(sale.getTotalMargin()))
                        .build())
                .toList();
    }

    private List<RecentUserDTO> buildRecentUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_USERS_LIMIT))
                .stream()
                .map(user -> RecentUserDTO.builder()
                        .name(fullName(user))
                        .email(user.getEmail())
                        .date(formatInstantAsDate(user.getCreatedAt()))
                        .build())
                .toList();
    }

    private List<StockAlertDTO> buildStockAlerts() {
        return productRepository.findStockAlerts(PageRequest.of(0, STOCK_ALERTS_LIMIT))
                .stream()
                .map(this::toStockAlert)
                .toList();
    }

    private StockAlertDTO toStockAlert(Product product) {
        if (product.getQuantity() == 0) {
            return StockAlertDTO.builder()
                    .name(product.getName())
                    .status("Rupture de stock")
                    .qty(0)
                    .level("low")
                    .build();
        }
        return StockAlertDTO.builder()
                .name(product.getName())
                .status("Seuil d'alerte atteint")
                .qty(product.getQuantity())
                .level("mid")
                .build();
    }

    private String supplierFrequencyDesc(long chargeCount) {
        if (chargeCount >= 8) {
            return "Fréquence : Très élevée (Hebdomadaire)";
        }
        if (chargeCount >= 4) {
            return "Fréquence : Élevée (Bi-hebdomadaire)";
        }
        if (chargeCount >= 2) {
            return "Fréquence : Moyenne (Mensuelle)";
        }
        return "Fréquence : Faible";
    }

    private Trend computeTrend(double current, double previous) {
        if (previous == 0) {
            if (current == 0) {
                return new Trend("0%", true);
            }
            return new Trend("+100%", true);
        }
        double percent = ((current - previous) / previous) * 100.0;
        long rounded = Math.round(percent);
        String sign = rounded > 0 ? "+" : "";
        return new Trend(sign + rounded + "%", rounded >= 0);
    }

    private String formatCount(long value) {
        return NumberFormat.getInstance(Locale.FRANCE).format(value);
    }

    private String formatCurrency(double value) {
        return NumberFormat.getInstance(Locale.FRANCE).format(Math.round(value)) + " FCFA";
    }

    private String fullName(User user) {
        String name = user.getName() != null ? user.getName().trim() : "";
        String subname = user.getSubname() != null ? user.getSubname().trim() : "";
        if (subname.isBlank()) {
            return name;
        }
        if (name.isBlank()) {
            return subname;
        }
        return name + " " + subname;
    }

    private String formatInstantAsDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDate().format(ISO_DATE);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.FRENCH) + value.substring(1);
    }

    private double nullToZero(Double value) {
        return value != null ? value : 0.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Trend(String text, boolean positive) {
    }
}
