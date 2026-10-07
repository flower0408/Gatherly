-- Pocetni podaci za prikaz aplikacije.
-- Svi nalozi imaju istu lozinku: gatherly demo 2026
-- Lozinka je duza od dvanaest znakova, koliko pravila aplikacije i traze.
-- Administrator je 'pera', obicni korisnici su ostali.

-- Datumi su kreirani za 2. septembar 2026. Pri svakom pokretanju pomeraju se za ceo broj nedelja
-- tako da 'danas' ostane u istom polozaju prema dogadjajima: oni koji su bili predstojeci to su i sada,
-- a dan u nedelji ostaje isti ('Saturday morning run' je i dalje subota).
set @shift = 7 * floor(datediff(curdate(), '2026-09-02') / 7);

insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, description, role)
values (false, true, null, 'pera@mail.com', 'Pera', true, null, 'Peric', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'pera',
        "pera", "I am admin of this app", 'ADMIN');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, role)
values (false, true, null, 'mika@mail.com', 'Mika', false, null, 'Mikic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'mika',
        'mika', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (false, true, null, 'ana@mail.com', 'Ana', false, null, 'Anic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'ana', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (false, true, null, 'zika@mail.com', 'Zika', false, null, 'Zikic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'zika', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (false, true, null, 'djura@mail.com', 'Djura', false, null, 'Djuric', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'djurica', 'USER');

insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Frontend meetup', 'Two short talks about what changed in the browser this year, then pizza and questions.', 'Novi Sad, SPENS',
        date_add(timestamp('2026-09-12 18:00:00'), interval @shift day), date_add(timestamp('2026-09-12 20:00:00'), interval @shift day), 50, 'TECHNOLOGY', date_add(timestamp('2026-08-12 12:00:00'), interval @shift day), false, 1);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Testing workshop, cancelled', 'The room fell through, so this one will be announced again for a new date.', 'Beograd, Startit Centar',
        date_add(timestamp('2026-09-08 18:20:30'), interval @shift day), date_add(timestamp('2026-09-08 20:20:30'), interval @shift day), 30, 'TECHNOLOGY', date_add(timestamp('2026-08-08 18:20:30'), interval @shift day), true, 2);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Community gathering', 'An open meeting of the group: what we did this year and what we plan for the next one.', 'Novi Sad, FTN',
        date_add(timestamp('2026-09-14 15:23:35'), interval @shift day), date_add(timestamp('2026-09-14 17:23:35'), interval @shift day), 20, 'BUSINESS', date_add(timestamp('2026-05-14 15:23:35'), interval @shift day), false, 1);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Board games night', 'Two tables, light games first and something longer afterwards. Only two seats left.', 'Novi Sad, Studentski kulturni centar',
        date_add(timestamp('2026-09-15 14:56:55'), interval @shift day), date_add(timestamp('2026-09-15 16:56:55'), interval @shift day), 2, 'GAMES', date_add(timestamp('2026-05-15 14:56:55'), interval @shift day), false, 2);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Photography walk', 'We walk from the bridge to the fortress and stop wherever the light is good.', 'Novi Sad, Petrovaradin',
        date_add(timestamp('2026-09-13 12:12:12'), interval @shift day), date_add(timestamp('2026-09-13 14:12:12'), interval @shift day), 15, 'ART_AND_CULTURE', date_add(timestamp('2026-06-13 12:12:12'), interval @shift day), false, 3);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Open air concert', 'Three bands by the river, from the afternoon until the lights go out.', 'Novi Sad, Strand',
        date_add(timestamp('2026-09-20 05:22:12'), interval @shift day), date_add(timestamp('2026-09-20 07:22:12'), interval @shift day), 200, 'MUSIC', date_add(timestamp('2026-07-20 05:22:12'), interval @shift day), false, 3);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Running club', 'Our usual ten kilometres along the river, at a pace where nobody is left behind.', 'Novi Sad, Kej',
        date_add(timestamp('2026-08-10 01:12:39'), interval @shift day), date_add(timestamp('2026-08-10 03:12:39'), interval @shift day), 40, 'SPORT', date_add(timestamp('2026-07-27 01:12:39'), interval @shift day), false, 2);

insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Will the talks be recorded for those who cannot come?', date_add(timestamp('2026-08-11 19:40:00'), interval @shift day), 1, 2, null);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Yes, we will put the recordings up a few days later.', date_add(timestamp('2026-08-04 09:15:00'), interval @shift day), 1, 1, 1);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Is there somewhere to leave a bike near the entrance?', date_add(timestamp('2026-06-22 21:05:00'), interval @shift day), 1, 3, null);

insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (true, false, 'HARASSMENT', date_add(cast('2026-05-12' as date), interval @shift day), 3, null, null, 4);
insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (null, false, 'SPAM', date_add(cast('2026-08-20' as date), interval @shift day), 2, 1, null, null);
insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (true, false, 'HARASSMENT', date_add(cast('2026-05-12' as date), interval @shift day), 5, null, 2, null);

insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-05-12' as date), interval @shift day), 'HEART', 3, null, 1);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-05-13' as date), interval @shift day), 'LIKE', 3, 1, null);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-05-13' as date), interval @shift day), 'DISLIKE', 3, 1, null);

insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values (date_add(cast('2026-05-11' as date), interval @shift day), false, 'People who build things for the web, meeting once a month to show what they made.', false, 'Novi Sad Web Circle', null);
insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values (date_add(cast('2026-05-11' as date), interval @shift day), false, 'A group for anyone who likes taking pictures around the city, no equipment required.', false, 'City Photographers', null);
insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values (date_add(cast('2026-05-12' as date), interval @shift day), true, 'A group that was closed because nobody was moderating what was posted in it.', true, 'Old Notice Board', 'Nobody was moderating the content');

insert into community_organizers (community_id, organizer_id)
values (1, 1);
insert into community_organizers (community_id, organizer_id)
values (2, 3);

insert into community_members (community_id, member_id)
values (1, 1);
insert into community_members (community_id, member_id)
values (1, 2);
insert into community_members (community_id, member_id)
values (2, 3);

insert into community_events (community_id, event_id)
values (1, 3);
insert into community_events (community_id, event_id)
values (1, 4);
insert into community_events (community_id, event_id)
values (2, 5);

insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('PENDING', null, date_add(cast('2026-05-13' as date), interval @shift day), false, 3, 1);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-05-14' as date), interval @shift day), date_add(cast('2026-05-13' as date), interval @shift day), false, 2, 4);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('REJECTED', date_add(cast('2026-05-16' as date), interval @shift day), date_add(cast('2026-05-15' as date), interval @shift day), false, 3, 4);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('WAITLISTED', null, date_add(cast('2026-05-17' as date), interval @shift day), false, 1, 4);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-08-10' as date), interval @shift day), date_add(cast('2026-07-28' as date), interval @shift day), false, 3, 7);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('NO_SHOW', date_add(cast('2026-08-10' as date), interval @shift day), date_add(cast('2026-07-29' as date), interval @shift day), false, 1, 7);

-- Sistemsku blokadu uvek izrice administrator, pa se i on belezi
insert into banned (is_deleted, timestamp, banned_by_user_id, towards_user_id, for_community_id)
values (false, date_add(cast('2026-05-13' as date), interval @shift day), 1, 5, null);

-- Dodatni podaci, da aplikacija ima sadrzaj za prikaz: jos korisnika, zajednica,
-- dogadjaja po raznim kategorijama i datumima, prijava u svim stanjima i razgovora.

insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, description, role)
values (false, true, null, 'jovana@mail.com', 'Jovana', false, null, 'Jovanovic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'jovana',
        'Jovana J.', 'I run every morning and I am always looking for company.', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, description, role)
values (false, true, null, 'marko@mail.com', 'Marko', false, null, 'Markovic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'marko',
        'Marko M.', 'Board games, strategy games, any games.', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, description, role)
values (false, true, null, 'tijana@mail.com', 'Tijana', false, null, 'Tijanic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'tijana',
        'Tijana T.', 'Food, languages and long walks.', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (false, true, null, 'nikola@mail.com', 'Nikola', false, null, 'Nikolic', '$2a$10$zCsaljiIPMxn3HYbBt.mVuipuvrJZbJbduWfxwBJTpVm8C5NQ0qe.', 'nikola', 'USER');

insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values (date_add(cast('2026-03-04' as date), interval @shift day), false, 'We run together along the Danube, every Saturday morning. Beginners are welcome, nobody is left behind.', false, 'Novi Sad Runners', null);
insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values (date_add(cast('2026-04-18' as date), interval @shift day), false, 'Board games every other Friday. We bring the games, you bring yourself.', false, 'Board Game Guild', null);
insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values (date_add(cast('2026-06-02' as date), interval @shift day), false, 'Photo walks around the city and the fortress, for anyone with a camera or a phone.', false, 'Photo Walks Novi Sad', null);

insert into community_organizers (community_id, organizer_id) values (4, 6);
insert into community_organizers (community_id, organizer_id) values (5, 7);
insert into community_organizers (community_id, organizer_id) values (6, 3);
insert into community_organizers (community_id, organizer_id) values (6, 8);

insert into community_members (community_id, member_id) values (4, 6);
insert into community_members (community_id, member_id) values (4, 3);
insert into community_members (community_id, member_id) values (4, 9);
insert into community_members (community_id, member_id) values (5, 7);
insert into community_members (community_id, member_id) values (5, 2);
insert into community_members (community_id, member_id) values (5, 9);
insert into community_members (community_id, member_id) values (6, 3);
insert into community_members (community_id, member_id) values (6, 8);
insert into community_members (community_id, member_id) values (6, 6);

insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Saturday morning run', 'An easy five kilometres along the Danube. We meet by the bridge and finish with coffee.', 'Novi Sad, Kej',
        date_add(timestamp('2026-09-26 08:00:00'), interval @shift day), date_add(timestamp('2026-09-26 09:30:00'), interval @shift day), 25, 'SPORT', date_add(timestamp('2026-08-20 10:00:00'), interval @shift day), false, 6);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Catan tournament', 'Four rounds, small prizes and a lot of arguing about sheep.', 'Novi Sad, Studentski kulturni centar',
        date_add(timestamp('2026-10-03 18:00:00'), interval @shift day), date_add(timestamp('2026-10-03 22:00:00'), interval @shift day), 4, 'GAMES', date_add(timestamp('2026-08-21 11:00:00'), interval @shift day), false, 7);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Street food festival', 'Twenty stands, live music and a lot of walking between them.', 'Novi Sad, Trg slobode',
        date_add(timestamp('2026-09-19 12:00:00'), interval @shift day), date_add(timestamp('2026-09-19 22:00:00'), interval @shift day), 300, 'FOOD_AND_DRINK', date_add(timestamp('2026-08-01 09:00:00'), interval @shift day), false, 8);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Museum night tour', 'A guided walk through three museums, with a story behind each room.', 'Novi Sad, Dunavska',
        date_add(timestamp('2026-10-10 19:00:00'), interval @shift day), date_add(timestamp('2026-10-10 22:00:00'), interval @shift day), 30, 'ART_AND_CULTURE', date_add(timestamp('2026-08-15 14:00:00'), interval @shift day), false, 3);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Angular workshop', 'From the first component to a working screen, for people who already know some JavaScript.', 'Novi Sad, FTN',
        date_add(timestamp('2026-09-30 17:00:00'), interval @shift day), date_add(timestamp('2026-09-30 20:00:00'), interval @shift day), 18, 'TECHNOLOGY', date_add(timestamp('2026-08-18 08:30:00'), interval @shift day), false, 1);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Danube kayak trip', 'Half a day on the water, kayaks provided. Some experience is useful but not required.', 'Novi Sad, Strand',
        date_add(timestamp('2026-10-05 09:00:00'), interval @shift day), date_add(timestamp('2026-10-05 14:00:00'), interval @shift day), 12, 'OUTDOORS', date_add(timestamp('2026-08-22 16:00:00'), interval @shift day), false, 9);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Startup pitch night', 'Six teams, five minutes each, and questions from the room afterwards.', 'Novi Sad, Startit Centar',
        date_add(timestamp('2026-10-15 18:30:00'), interval @shift day), date_add(timestamp('2026-10-15 21:00:00'), interval @shift day), 60, 'BUSINESS', date_add(timestamp('2026-08-19 12:00:00'), interval @shift day), false, 7);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('English conversation club', 'An hour of talking, no grammar drills. Any level is fine.', 'Novi Sad, Gradska biblioteka',
        date_add(timestamp('2026-09-24 18:00:00'), interval @shift day), date_add(timestamp('2026-09-24 19:30:00'), interval @shift day), 16, 'EDUCATION', date_add(timestamp('2026-08-05 10:00:00'), interval @shift day), false, 8);
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Sunrise yoga', 'We start before the city wakes up. Bring a mat and something warm for afterwards.', 'Novi Sad, Petrovaradin',
        date_add(timestamp('2026-08-15 06:00:00'), interval @shift day), date_add(timestamp('2026-08-15 07:30:00'), interval @shift day), 20, 'SPORT', date_add(timestamp('2026-07-10 09:00:00'), interval @shift day), false, 6);

insert into community_events (community_id, event_id) values (4, 8);
insert into community_events (community_id, event_id) values (4, 16);
insert into community_events (community_id, event_id) values (5, 9);
insert into community_events (community_id, event_id) values (1, 12);

-- Prijave: potvrdjene, one koje cekaju odluku, popunjen dogadjaj sa listom cekanja
-- i prosli dogadjaji sa upisanim dolascima
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-21' as date), interval @shift day), date_add(cast('2026-08-20' as date), interval @shift day), false, 3, 8);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-21' as date), interval @shift day), date_add(cast('2026-08-20' as date), interval @shift day), false, 9, 8);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('PENDING', null, date_add(cast('2026-08-23' as date), interval @shift day), false, 2, 8);

insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-22' as date), interval @shift day), date_add(cast('2026-08-21' as date), interval @shift day), false, 2, 9);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-22' as date), interval @shift day), date_add(cast('2026-08-21' as date), interval @shift day), false, 9, 9);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-22' as date), interval @shift day), date_add(cast('2026-08-22' as date), interval @shift day), false, 6, 9);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-23' as date), interval @shift day), date_add(cast('2026-08-22' as date), interval @shift day), false, 8, 9);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('WAITLISTED', null, date_add(cast('2026-08-24' as date), interval @shift day), false, 3, 9);

insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('PENDING', null, date_add(cast('2026-08-24' as date), interval @shift day), false, 6, 11);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('PENDING', null, date_add(cast('2026-08-25' as date), interval @shift day), false, 7, 11);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-26' as date), interval @shift day), date_add(cast('2026-08-25' as date), interval @shift day), false, 8, 11);

insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-20' as date), interval @shift day), date_add(cast('2026-08-19' as date), interval @shift day), false, 3, 12);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', date_add(cast('2026-08-20' as date), interval @shift day), date_add(cast('2026-08-19' as date), interval @shift day), false, 6, 12);

insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-08-15' as date), interval @shift day), date_add(cast('2026-07-11' as date), interval @shift day), false, 3, 16);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-08-15' as date), interval @shift day), date_add(cast('2026-07-12' as date), interval @shift day), false, 9, 16);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('NO_SHOW', date_add(cast('2026-08-15' as date), interval @shift day), date_add(cast('2026-07-12' as date), interval @shift day), false, 7, 16);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-08-10' as date), interval @shift day), date_add(cast('2026-07-28' as date), interval @shift day), false, 8, 7);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-08-10' as date), interval @shift day), date_add(cast('2026-07-28' as date), interval @shift day), false, 9, 7);

insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Is the pace beginner friendly, or should I train first?', date_add(timestamp('2026-08-21 18:20:00'), interval @shift day), 8, 3, null);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Completely beginner friendly, we split into two groups at the start.', date_add(timestamp('2026-08-21 19:05:00'), interval @shift day), 8, 6, 4);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Is there parking near the meeting point?', date_add(timestamp('2026-08-22 09:15:00'), interval @shift day), 8, 9, null);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Do we need to bring our own copy of the game?', date_add(timestamp('2026-08-22 20:40:00'), interval @shift day), 9, 2, null);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'No need, the guild brings four sets.', date_add(timestamp('2026-08-23 08:10:00'), interval @shift day), 9, 7, 7);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Which museums are included in the tour?', date_add(timestamp('2026-08-23 12:00:00'), interval @shift day), 12, 8, null);

insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-08-21' as date), interval @shift day), 'LIKE', 9, 4, null);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-08-21' as date), interval @shift day), 'LIKE', 2, 4, null);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-08-22' as date), interval @shift day), 'HEART', 3, 5, null);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-08-22' as date), interval @shift day), 'HEART', 3, null, 8);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-08-22' as date), interval @shift day), 'LIKE', 9, null, 8);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, date_add(cast('2026-08-23' as date), interval @shift day), 'LIKE', 6, null, 11);

-- Prijava sadrzaja koja jos ceka odluku administratora
insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (null, false, 'SCAM_OR_MISLEADING', date_add(cast('2026-08-24' as date), interval @shift day), 6, null, 11, null);

-- Blokada u zajednici: nikola je izbacen iz gilde za drustvene igre
insert into banned (is_deleted, timestamp, banned_by_user_id, towards_user_id, for_community_id)
values (false, date_add(cast('2026-08-23' as date), interval @shift day), 7, 9, 5);

-- Jos jedan prosli dogadjaj, da vise ljudi ima dovoljno istorije za skor pouzdanosti
insert into `event` (title, description, location, starts_at, ends_at, capacity, category, creation_date, is_deleted, created_by_user_id)
values ('Sunset picnic', 'Blankets, snacks and the view from the fortress while the sun goes down.', 'Novi Sad, Petrovaradin',
        date_add(timestamp('2026-07-18 18:30:00'), interval @shift day), date_add(timestamp('2026-07-18 21:00:00'), interval @shift day), 30, 'OUTDOORS', date_add(timestamp('2026-06-25 10:00:00'), interval @shift day), false, 8);

insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('NO_SHOW', date_add(cast('2026-07-18' as date), interval @shift day), date_add(cast('2026-06-30' as date), interval @shift day), false, 3, 17);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-07-18' as date), interval @shift day), date_add(cast('2026-06-30' as date), interval @shift day), false, 6, 17);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-07-18' as date), interval @shift day), date_add(cast('2026-07-01' as date), interval @shift day), false, 9, 17);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('NO_SHOW', date_add(cast('2026-07-18' as date), interval @shift day), date_add(cast('2026-07-02' as date), interval @shift day), false, 7, 17);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', date_add(cast('2026-07-18' as date), interval @shift day), date_add(cast('2026-07-02' as date), interval @shift day), false, 2, 17);

-- Naslovne slike dogadjaja. Stoje medju resursima aplikacije, pa rade i posle novog preuzimanja projekta.
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/technology.svg', 1, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/business.svg', 3, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/games.svg', 4, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/art-and-culture.svg', 5, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/music.svg', 6, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/sport.svg', 7, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/sport.svg', 8, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/games.svg', 9, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/food-and-drink.svg', 10, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/art-and-culture.svg', 11, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/technology.svg', 12, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/outdoors.svg', 13, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/business.svg', 14, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/education.svg', 15, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/sport.svg', 16, null);
insert into image (is_deleted, path, belongs_to_event_id, belongs_to_user_id)
values (false, '/assets/images/covers/outdoors.svg', 17, null);
