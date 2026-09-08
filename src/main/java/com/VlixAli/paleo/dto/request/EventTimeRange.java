package com.VlixAli.paleo.dto.request;

import java.time.Instant;

public interface EventTimeRange {
    Instant startTime();
    Instant endTime();
}
