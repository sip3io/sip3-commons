package io.sip3.commons.micrometer

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Meter
import io.vertx.core.Vertx
import java.util.concurrent.atomic.DoubleAdder

class AggregatingCounter(
    name: String,
    vertx: Vertx,
    aggregationDelay: Long = 1000L,
    attributes: Map<String, Any> = emptyMap()
): Counter {
    private val counter = Metrics.counter(name, attributes)
    private val adder = DoubleAdder()

    init {
        vertx.setPeriodic(aggregationDelay) {
            val sum = adder.sumThenReset()
            if (sum > 0.0) {
                counter.increment(sum)
            }
        }
    }

    override fun increment(amount: Double) {
        adder.add(amount)
    }

    override fun count(): Double {
        return counter.count() + adder.sum()
    }

    override fun getId(): Meter.Id {
        return counter.id
    }
}