insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, description, role)
values (false, true, null, 'pera@mail.com', 'Pera', true, null, 'Peric', '$2a$12$6LRoZ4kDywW7WnK9bg16A.XXVHgKXxpi6YZ5JYptFnwW3y97DZGju', 'pera',
        "pera", "I am admin of this app", 'ADMIN');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, display_name, role)
values (false, true, null, 'mika@mail.com', 'Mika', false, null, 'Mikic', '$2a$12$15ymkpdnVT1DGRfGjjqIY.SnwcaMTyiIUb71f3r3Be8i3zHuNRM.i', 'mika',
        'mika', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (false, true, null, 'ana@mail.com', 'Ana', false, null, 'Anic', '$2a$12$uVuGNCVu62e8v7YtlF9yZurtYkvWgOj9N5UEdb51eB1EM959We.v.', 'ana', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (true, true, null, 'zika@mail.com', 'Zika', false, null, 'Zikic', '$2a$12$TeQF.oCNjgTsl9rFWA9Tb.zA3716nzJZ5wwxONeu1tTzHoqBkk7FK', 'zika', 'USER');
insert into `user`(is_deleted, is_verified, verification_token, email, first_name, is_admin, last_login, last_name, password, username, role)
values (true, true, null, 'djura@mail.com', 'Djura', false, null, 'Djuric', '$2a$12$TeQF.oCNjgTsl9rFWA9Tb.zA3716nzJZ5wwxONeu1tTzHoqBkk7FK', 'djurica', 'USER');

insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Frontend meetup', 'This is first event by me. I am glad if you can see it.', 'Novi Sad, SPENS',
        '2026-09-12 18:00:00', 50, '2026-08-12 12:00:00', false, 1);
insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Cancelled workshop', 'This is new event for today, happy to be here.', 'Beograd, Startit Centar',
        '2026-09-08 18:20:30', 30, '2026-08-08 18:20:30', true, 2);
insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Community gathering', 'This is an event in a community. If you see it, you are inside a community', 'Novi Sad, FTN',
        '2026-09-14 15:23:35', 20, '2026-05-14 15:23:35', false, 1);
insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Board games night', 'This is another event for a community. Say hi to everyone in this community', 'Novi Sad, Kvartic',
        '2026-09-15 14:56:55', 2, '2026-05-15 14:56:55', false, 2);
insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Photography walk', 'This is test event for second community. Say hi to everyone in this community', 'Novi Sad, Petrovaradin',
        '2026-09-13 12:12:12', 15, '2026-06-13 12:12:12', false, 3);
insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Open air concert', 'This is Anas public event. Everyone can see it. Sending good vibes to everyone.', 'Novi Sad, Strand',
        '2026-09-20 05:22:12', 200, '2026-07-20 05:22:12', false, 3);
insert into `event` (title, description, location, starts_at, capacity, creation_date, is_deleted, created_by_user_id)
values ('Running club', 'This is Mikas public event. Everyone can see it. Sending good vibes to everyone.', 'Novi Sad, Kej',
        '2026-08-10 01:12:39', 40, '2026-07-27 01:12:39', false, 2);

insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Good thoughts', '2026-08-11', 1, 2, null);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Fine answer to message', '2026-08-04', 1, 1, 1);
insert into comment (is_deleted, text, timestamp, belongs_to_event_id, belongs_to_user_id, replies_to_comment_id)
values (false, 'Another comment', '2026-06-22', 1, 3, null);

insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (true, false, 'HARASSMENT', '2026-05-12', 3, null, null, 4);
insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (true, false, 'HARASSMENT', '2026-05-12', 2, 1, null, null);
insert into report (accepted, is_deleted, reason, timestamp, by_user_id, on_comment_id, on_event_id, on_user_id)
values (true, false, 'HARASSMENT', '2026-05-12', 5, null, 2, null);

insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, '2026-05-12', 'HEART', 3, null, 1);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, '2026-05-13', 'LIKE', 3, 1, null);
insert into reaction (is_deleted, timestamp, type, made_by_user_id, on_comment_id, on_event_id)
values (false, '2026-05-13', 'DISLIKE', 3, 1, null);

insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values ('2026-05-11', false, 'Test community for testing purposes', false, 'Test Community 1', null);
insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values ('2026-05-11', false, 'Another test community for testing', false, 'Test Community 2', null);
insert into community (creation_date, is_deleted, description, is_suspended, name, suspended_reason)
values ('2026-05-12', true, 'Deleted test community for testing', true, 'Test Community 3', 'Un-moderated');

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
values ('PENDING', null, '2026-05-13', false, 3, 1);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ACCEPTED', '2026-05-14', '2026-05-13', false, 2, 4);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('REJECTED', '2026-05-16', '2026-05-15', false, 3, 4);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('WAITLISTED', null, '2026-05-17', false, 1, 4);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('ATTENDED', '2026-08-10', '2026-07-28', false, 3, 7);
insert into event_registration (status, at, created_at, is_deleted, created_by_user_id, for_event_id)
values ('NO_SHOW', '2026-08-10', '2026-07-29', false, 1, 7);

insert into banned (is_deleted, timestamp, by_organizer_id, towards_user_id, for_community_id)
values (false, '2026-05-13', null, 1, null);
