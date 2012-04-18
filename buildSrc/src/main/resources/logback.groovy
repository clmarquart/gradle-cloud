import static ch.qos.logback.classic.Level.*

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%msg%n"
  }
}
root(DEBUG, ["STDOUT"])