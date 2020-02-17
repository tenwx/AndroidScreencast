package com.github.xsavikx.androidscreencast.exception;

public final class ExecuteCommandException extends AndroidScreenCastRuntimeException {

    public ExecuteCommandException(String command) {
        super(String.format("Cannot execute command '%s'.", command));
    }
}
