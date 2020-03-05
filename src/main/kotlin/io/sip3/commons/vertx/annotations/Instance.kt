package io.sip3.commons.vertx.annotations

@Target(AnnotationTarget.CLASS)
annotation class Instance(val singleton: Boolean = false)