CREATE SEQUENCE short_url_id_seq;

ALTER TABLE short_url
    ALTER COLUMN id SET DEFAULT nextval('short_url_id_seq');

ALTER SEQUENCE short_url_id_seq OWNED BY short_url.id;

SELECT setval('short_url_id_seq', COALESCE((SELECT MAX(id) FROM short_url), 0) + 1, false);