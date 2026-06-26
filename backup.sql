--
-- PostgreSQL database cluster dump
--

\restrict xbtLQHbazgBi5JLvqEGAKVPeNiOFhTwAnZNketBru2nh2btjJkBSd3hmwWUHHCv

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:+jO82pvutN48YaJj5yK8mg==$IC7M2GpBJ1UKVGeYHo/CuaSzYbN+7N6BvyTm5/506X8=:oD6YrCUtFb0si3lLV/SIyRTMiw8VYnjRLOEsdC9zERc=';

--
-- User Configurations
--








\unrestrict xbtLQHbazgBi5JLvqEGAKVPeNiOFhTwAnZNketBru2nh2btjJkBSd3hmwWUHHCv

--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

\restrict HPK4nTvI8zxCThjjvrcAS7gWMLtYSZfKa5PBxoy1aXa2tMdH1hnwx0e8rd4vSyg

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

\unrestrict HPK4nTvI8zxCThjjvrcAS7gWMLtYSZfKa5PBxoy1aXa2tMdH1hnwx0e8rd4vSyg

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

\restrict rV2c2rCgyvhfNKsguiOHhxgd2dDrqJYpZbG1d0VsDFrbwQZpjx1fkmpoKGsS6bF

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- PostgreSQL database dump complete
--

\unrestrict rV2c2rCgyvhfNKsguiOHhxgd2dDrqJYpZbG1d0VsDFrbwQZpjx1fkmpoKGsS6bF

--
-- Database "yowyob_auth" dump
--

--
-- PostgreSQL database dump
--

\restrict pckUp7qwlOZjSVxwX1h9Zr65rUCpld1IC5zOURVe0qKSQ9V2BDw0vxeyWqpXYxE

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: yowyob_auth; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE yowyob_auth WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE yowyob_auth OWNER TO postgres;

\unrestrict pckUp7qwlOZjSVxwX1h9Zr65rUCpld1IC5zOURVe0qKSQ9V2BDw0vxeyWqpXYxE
\connect yowyob_auth
\restrict pckUp7qwlOZjSVxwX1h9Zr65rUCpld1IC5zOURVe0qKSQ9V2BDw0vxeyWqpXYxE

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    avatar_url character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    email_verified boolean NOT NULL,
    name character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    phone character varying(255),
    role character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    avatarurl character varying(255),
    updatedat timestamp(6) without time zone,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying, 'MERCHANT'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'BANNED'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, avatar_url, created_at, email, email_verified, name, password, phone, role, status, updated_at, avatarurl, updatedat) FROM stdin;
e4f370d5-19e2-488e-b77c-2c26c3ac255e	\N	2026-02-10 10:57:05.233731	crawler@yowyob.system	t	Yowyob Crawler System	$2a$10$0GGL.xoPr8hQabvshdSmSexC3vEtfX2pYo/Kmg9.j0pKheRm5j08K	\N	USER	ACTIVE	2026-02-10 10:57:05.233812	\N	\N
3baf3fbb-eed5-4533-982c-94a201f7174b	\N	2026-02-10 19:32:29.9852	nzungangf@gmail.com	t	Freddy Nzungang	$2a$10$J.8eSzT48lgznLkn2BIAvuDzQW4jyyeMFG8ji6mIwlfHW33.QZK6S	\N	USER	ACTIVE	2026-02-10 19:32:29.985285	\N	\N
4027cd0c-1a15-4cdf-b18b-a5b4fc6a4278	\N	2026-02-10 21:54:07.366412	test@example.com	f	Test User	$2a$10$kJQMpAobky.cQpBnkkGyY.HqMc2O3qrsVadr8SwQrlps0XIm5Gb.a	\N	USER	ACTIVE	2026-02-10 21:54:07.366489	\N	\N
\.


--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict pckUp7qwlOZjSVxwX1h9Zr65rUCpld1IC5zOURVe0qKSQ9V2BDw0vxeyWqpXYxE

--
-- Database "yowyob_listings" dump
--

--
-- PostgreSQL database dump
--

\restrict qyXZXJJXUdfB8F2Ld5INnYdJZ24Dzt0H3XGHLTPcBY7OLGaPqgNEXZ9oZVokzYt

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: yowyob_listings; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE yowyob_listings WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE yowyob_listings OWNER TO postgres;

\unrestrict qyXZXJJXUdfB8F2Ld5INnYdJZ24Dzt0H3XGHLTPcBY7OLGaPqgNEXZ9oZVokzYt
\connect yowyob_listings
\restrict qyXZXJJXUdfB8F2Ld5INnYdJZ24Dzt0H3XGHLTPcBY7OLGaPqgNEXZ9oZVokzYt

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: listings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.listings (
    id uuid NOT NULL,
    address character varying(255),
    category character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    description text,
    latitude double precision,
    longitude double precision,
    price double precision NOT NULL,
    seller_id uuid NOT NULL,
    status character varying(255),
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    createdat timestamp(6) without time zone,
    sellerid uuid NOT NULL,
    updatedat timestamp(6) without time zone,
    CONSTRAINT listings_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SOLD'::character varying, 'INACTIVE'::character varying, 'PENDING'::character varying])::text[])))
);


ALTER TABLE public.listings OWNER TO postgres;

--
-- Data for Name: listings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.listings (id, address, category, created_at, description, latitude, longitude, price, seller_id, status, title, updated_at, createdat, sellerid, updatedat) FROM stdin;
\.


--
-- Name: listings listings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.listings
    ADD CONSTRAINT listings_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict qyXZXJJXUdfB8F2Ld5INnYdJZ24Dzt0H3XGHLTPcBY7OLGaPqgNEXZ9oZVokzYt

--
-- Database "yowyob_users" dump
--

--
-- PostgreSQL database dump
--

\restrict 5LhEFUyPWQMWHVtB8f3Fy8CDfyocnioFc6QIo1bDFhDI9uRRGmPkJBWSGcZt3gL

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.1 (Debian 18.1-1.pgdg13+2)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: yowyob_users; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE yowyob_users WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE yowyob_users OWNER TO postgres;

\unrestrict 5LhEFUyPWQMWHVtB8f3Fy8CDfyocnioFc6QIo1bDFhDI9uRRGmPkJBWSGcZt3gL
\connect yowyob_users
\restrict 5LhEFUyPWQMWHVtB8f3Fy8CDfyocnioFc6QIo1bDFhDI9uRRGmPkJBWSGcZt3gL

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: search_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.search_history (
    id uuid NOT NULL,
    query character varying(255) NOT NULL,
    searched_at timestamp(6) without time zone,
    user_id uuid NOT NULL,
    searchedat timestamp(6) without time zone,
    userid uuid NOT NULL
);


ALTER TABLE public.search_history OWNER TO postgres;

--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_profiles (
    id uuid NOT NULL,
    address character varying(255),
    avatar_url character varying(255),
    bio character varying(255),
    city character varying(255),
    country character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    phone_number character varying(255),
    social_links_json character varying(255),
    updated_at timestamp(6) without time zone,
    user_id uuid NOT NULL,
    avatarurl character varying(255),
    firstname character varying(255),
    lastname character varying(255),
    phonenumber character varying(255),
    sociallinksjson character varying(255),
    updatedat timestamp(6) without time zone
);


ALTER TABLE public.user_profiles OWNER TO postgres;

--
-- Data for Name: search_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.search_history (id, query, searched_at, user_id, searchedat, userid) FROM stdin;
\.


--
-- Data for Name: user_profiles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_profiles (id, address, avatar_url, bio, city, country, created_at, email, first_name, last_name, phone_number, social_links_json, updated_at, user_id, avatarurl, firstname, lastname, phonenumber, sociallinksjson, updatedat) FROM stdin;
a1e7b0fd-f6ee-49b7-a985-29b5298de253	\N	\N	\N	\N	\N	2026-02-10 19:32:45.077744	\N	\N	\N	\N	\N	2026-02-10 19:32:45.077847	3baf3fbb-eed5-4533-982c-94a201f7174b	\N	\N	\N	\N	\N	\N
85721429-5788-4e01-a905-86ce44658d3a	\N	\N	\N	\N	\N	2026-02-10 22:11:38.288421	\N	\N	\N	\N	\N	2026-02-10 22:11:38.288472	4027cd0c-1a15-4cdf-b18b-a5b4fc6a4278	\N	\N	\N	\N	\N	\N
\.


--
-- Name: search_history search_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.search_history
    ADD CONSTRAINT search_history_pkey PRIMARY KEY (id);


--
-- Name: user_profiles uk_e5h89rk3ijvdmaiig4srogdc6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT uk_e5h89rk3ijvdmaiig4srogdc6 UNIQUE (user_id);


--
-- Name: user_profiles user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict 5LhEFUyPWQMWHVtB8f3Fy8CDfyocnioFc6QIo1bDFhDI9uRRGmPkJBWSGcZt3gL

--
-- PostgreSQL database cluster dump complete
--

