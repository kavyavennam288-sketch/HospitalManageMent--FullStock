package com.medsync.service;

import com.medsync.model.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    // JavaMailSender is commented out for H2/dev mode.
    // Uncomment when switching back to PostgreSQL + real SMTP:
    // private final JavaMailSender mailSender;
    // private final String fromEmail = "noreply@medsync.com";

    // @Async means this method runs on a background thread from our
    // AsyncConfig thread pool — the caller (AppointmentService) doesn't
    // wait for the email to send before returning the booking response.
    @Async
    public void sendBookingConfirmation(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getEmail();
            String doctorName = appointment.getDoctor().getFullName();
            String startTime = appointment.getStartTime().toString();

            // ── Dev mode: log instead of sending ─────────────────────
            log.info("📧 [EMAIL] Booking confirmation sent to: {}", patientEmail);
            log.info("   Appointment with Dr. {} at {}", doctorName, startTime);
            log.info("   Appointment ID: {}", appointment.getId());

            // ── Production: uncomment this block ─────────────────────
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setFrom(fromEmail);
            // message.setTo(patientEmail);
            // message.setSubject("MedSync - Appointment Confirmed");
            // message.setText(String.format(
            //     "Dear %s,\n\nYour appointment with Dr. %s is confirmed.\n" +
            //     "Date & Time: %s\nAppointment ID: %d\n\nThank you,\nMedSync Team",
            //     appointment.getPatient().getFullName(),
            //     doctorName, startTime, appointment.getId()
            // ));
            // mailSender.send(message);

        } catch (Exception e) {
            // Never let email failure crash the app — just log it.
            // The appointment is already saved and confirmed.
            log.error("Failed to send booking confirmation email: {}", e.getMessage());
        }
    }

    @Async
    public void sendCancellationNotification(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getEmail();

            log.info("📧 [EMAIL] Cancellation notification sent to: {}", patientEmail);
            log.info("   Appointment ID: {} cancelled", appointment.getId());

        } catch (Exception e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }
}