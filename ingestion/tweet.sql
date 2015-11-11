CREATE TABLE tweet (
    id bigint NOT NULL,
    name character varying(21) DEFAULT NULL::character varying,
    screen_name character varying(16) DEFAULT NULL::character varying,
    orig_name character varying(21) DEFAULT NULL::character varying,
    orig_screen_name character varying(16) DEFAULT NULL::character varying,
    tweet character varying(200) DEFAULT NULL::character varying,
    avatar_url character varying(2100) DEFAULT NULL::character varying,
    orig_avatar_url character varying(2100) DEFAULT NULL::character varying,
    ts timestamp without time zone NOT NULL,
    url1 character varying(2100) DEFAULT NULL::character varying,
    url2 character varying(2100) DEFAULT NULL::character varying,
    media_url character varying(2100) DEFAULT NULL::character varying,
    read boolean DEFAULT false
);
