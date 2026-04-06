package com.wthallys.cryptex.cli;

import java.nio.file.Path;

public record Command(
        Path inputFile,
        boolean decrypt,
        char[] password
) {
    public boolean hasPassword() {
        return password != null && password.length > 0;
    }
}