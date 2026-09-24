package com.backend.bilanko.scheduler;

import com.backend.bilanko.models.person.notification.NotificationType;
import com.backend.bilanko.models.person.user.Role;
import com.backend.bilanko.models.person.user.User;
import com.backend.bilanko.repository.person.UserRepository;
import com.backend.bilanko.services.person.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // S'exécute le 1er jour de chaque mois à 00:00
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlyReportNotifications() {
        log.info("Génération des notifications de rapport mensuel pour les MARCHANDS...");
        
        List<User> merchants = userRepository.findByRole(Role.MERCHANT);
        
        for (User merchant : merchants) {
            notificationService.createNotification(
                    merchant,
                    NotificationType.MONTHLY_REPORT,
                    "Rapport mensuel disponible",
                    "Votre rapport d'activité pour le mois dernier est maintenant disponible.",
                    null
            );
        }
        
        log.info("Notifications de rapport mensuel envoyées avec succès.");
    }
}
