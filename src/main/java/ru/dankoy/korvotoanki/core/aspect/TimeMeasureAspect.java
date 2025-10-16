package ru.dankoy.korvotoanki.core.aspect;

import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class TimeMeasureAspect {

  @Around("@annotation(ru.dankoy.korvotoanki.core.aspect.annot.LogExecutionTime)")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    log.debug("Started '{}'", joinPoint.getSignature().getName());

    Instant start = Instant.now();

    Object result = null;
    result = joinPoint.proceed();
    Instant finish = Instant.now();

    Duration duration = Duration.between(start, finish);
    long timeElapsedMillis = duration.toMillis();
    long timeElapsedSeconds = duration.toSeconds();

    log.info(
        "Method '{}' in '{}' class took {} ms and {} seconds",
        joinPoint.getSignature().getName(),
        joinPoint.getTarget().getClass().getName(),
        timeElapsedMillis,
        timeElapsedSeconds);

    return result;
  }
}
