package com.backend.bilanko.models.person;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class NotificationPreferences {
    @Column(name = "notify_stock_alerts")
    private boolean stockAlerts = true;

    @Column(name = "notify_new_sales")
    private boolean newSales = true;

    @Column(name = "notify_monthly_reports")
    private boolean monthlyReports = false;

    @Column(name = "notify_updates")
    private boolean updates = true;
}