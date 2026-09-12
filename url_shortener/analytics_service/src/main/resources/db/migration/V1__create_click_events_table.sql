CREATE TABLE click_events (
                              id                BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              code              VARCHAR(20)  NOT NULL,
                              original_url      TEXT         NOT NULL,
                              ip_address        TEXT,
                              user_agent        TEXT,
                              referrer          TEXT,
                              resolver_node_id  VARCHAR(2)   NOT NULL,
                              was_in_cache      BOOLEAN      NOT NULL,
                              resolve_time_ms   INTEGER      NOT NULL,
                              clicked_at        TIMESTAMPTZ  NOT NULL,
                              received_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_click_events_code ON click_events (code);
CREATE INDEX idx_click_events_clicked_at ON click_events (clicked_at);