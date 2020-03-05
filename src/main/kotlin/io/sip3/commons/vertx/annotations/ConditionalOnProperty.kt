package io.sip3.commons.vertx.annotations

@Target(AnnotationTarget.CLASS)
annotation class ConditionalOnProperty(val value: String)