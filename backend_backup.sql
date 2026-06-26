--
-- PostgreSQL database cluster dump
--

\restrict LCqGJwCFniO3H1uLnxoHEyXKqD3w9VRTgMLXnn3tsBrSjA0RmmBwKnrhF5R9aKo

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:4IogV4SIsWuwGNSqEFvOMQ==$kuYRHyWPrmCmYcmbCLyrkdhLIIqD+eUhrLDokKcyXFA=:8EDOyK1GSMlmFzfBejCVgO419wHQ8rzKgUT3rkIHeg8=';

--
-- User Configurations
--








\unrestrict LCqGJwCFniO3H1uLnxoHEyXKqD3w9VRTgMLXnn3tsBrSjA0RmmBwKnrhF5R9aKo

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

\restrict Cazn3xzAf3IwP8MjckLthLheEfUL99CmdrAPuH0FCqVtZ9GZDokchvQZgf5vF5e

-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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

\unrestrict Cazn3xzAf3IwP8MjckLthLheEfUL99CmdrAPuH0FCqVtZ9GZDokchvQZgf5vF5e

--
-- Database "postgres" dump
--

\connect postgres

--
-- PostgreSQL database dump
--

\restrict SLwTOu3rsGPVEWrBajJd87HRWUGd6pXguf42wSNIcmEKIDc7f6gYJczPXEcqP5U

-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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

\unrestrict SLwTOu3rsGPVEWrBajJd87HRWUGd6pXguf42wSNIcmEKIDc7f6gYJczPXEcqP5U

--
-- Database "yowyob_auth" dump
--

--
-- PostgreSQL database dump
--

\restrict eGWJD0cWvMKm3aDJs1flR5PkPulVlfjNqJnj0BehRBh5tYrMix8psyfiRTLLv8d

-- Dumped from database version 16.11 (Debian 16.11-1.pgdg13+1)
-- Dumped by pg_dump version 16.11 (Debian 16.11-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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

\unrestrict eGWJD0cWvMKm3aDJs1flR5PkPulVlfjNqJnj0BehRBh5tYrMix8psyfiRTLLv8d
\connect yowyob_auth
\restrict eGWJD0cWvMKm3aDJs1flR5PkPulVlfjNqJnj0BehRBh5tYrMix8psyfiRTLLv8d

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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

\unrestrict eGWJD0cWvMKm3aDJs1flR5PkPulVlfjNqJnj0BehRBh5tYrMix8psyfiRTLLv8d

--
-- PostgreSQL database cluster dump complete
--

