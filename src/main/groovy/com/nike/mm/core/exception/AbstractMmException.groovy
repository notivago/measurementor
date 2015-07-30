package com.nike.mm.core.exception

import groovy.transform.CompileStatic;

@CompileStatic
abstract class AbstractMmException extends RuntimeException {

    AbstractMmException(final String message) {
        super(message)
    }
}
