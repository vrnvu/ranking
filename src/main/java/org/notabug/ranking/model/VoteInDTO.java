package org.notabug.ranking.model;

public record VoteInDTO(
        String user,
        // TODO validate ranges with custom dto type
        int skill,
        int toxic
        ) {
}
