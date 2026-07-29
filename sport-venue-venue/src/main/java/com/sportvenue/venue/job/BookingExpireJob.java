package com.sportvenue.venue.job;

import com.sportvenue.venue.entity.Booking;
import com.sportvenue.venue.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class BookingExpireJob {

    @Autowired
    private BookingRepository bookingRepository;

    /** 每 5 分钟：已过结束时间的 BOOKED → EXPIRED，释放占用 */
    @Scheduled(fixedDelayString = "${saas.booking.expire-delay-ms:300000}")
    @Transactional
    public void expireBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> list = bookingRepository.findByStatusAndEndTimeBefore(Booking.BookingStatus.BOOKED, now);
        if (list.isEmpty()) {
            return;
        }
        for (Booking b : list) {
            b.setStatus(Booking.BookingStatus.EXPIRED);
        }
        bookingRepository.saveAll(list);
        log.info("订场自动过期 {} 单", list.size());
    }
}
