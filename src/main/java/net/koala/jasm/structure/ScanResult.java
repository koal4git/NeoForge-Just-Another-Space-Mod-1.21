package net.koala.jasm.structure;

import java.awt.desktop.UserSessionEvent;

//sealed means only classes below are allowed to implement. can tell me if i miss a case
public sealed interface ScanResult {

    record Success(RocketStructure structure) implements ScanResult {

    }

    record Failure(Reason reason, String detail) implements ScanResult {

    }

    enum Reason {
        NO_BLOCKS_FOUND,
        TOO_MANY_BLOCKS,
        TOO_WIDE,
        TOO_TALL,
        NOT_ON_PLATFORM,
        BELOW_PLATFORM,
        MISSING_COMPONENT
    }



}
