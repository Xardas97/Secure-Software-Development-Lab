insert into users(id, username, password)
values (1, 'bruce', 'wayne'),
       (2, 'peter', 'security_rules'),
       (3, 'tom', 'guessmeifyoucan'),
        (4, 'toeLover', 'blood_everywhere');

insert into persons(id, firstName, lastName, email)
values (1, 'bruce', 'wayne', 'notBatman@gmail.com'),
       (2, 'Peter', 'Petigrew', 'oneFingernailFewerToClean@gmail.com'),
       (3, 'Tom', 'Riddle', 'theyGotMyNose@gmail.com'),
        (4, 'Quentin', 'Tarantino', 'qt5@gmail.com');

insert into movies(id, title, description)
values (1, 'Four rooms', 'Following New Years celebration in a hotel in four different perspectives'),
       (2, 'Porodicno blago specijal', 'Stojkovici kupuju prase'),
       (3, 'Conan the Barbarian', 'Original fantasy hero');

insert into genres(id, name)
values (1, 'action'),
       (2, 'adventure'),
       (3, 'soap opera'),
       (4, 'comedy'),
       (5, 'horror');

insert into movies_to_genres(movieId, genreId)
values (1, 4),
       (1, 2),
       (2, 3),
       (2, 4),
       (3, 1),
       (3, 2);

insert into ratings(movieId, userId, rating)
values (1, 3, 5),
        (3, 2, 1),
        (3, 1, 3),
        (1, 1, 5),
        (1, 2, 4);

insert into comments(movieId, userId, comment)
values (1, 1, 'There are four rooms. P.S. I am not Batman');

insert into roles(id, name)
values (1, 'ADMIN'),
       (2, 'MANAGER'),
       (3, 'REVIEWER');

insert into user_to_roles(userId, roleId)
values (1, 3), /* bruce     = REVIEWER */
       (2, 3), /* peter     = REVIEWER */
       (3, 1), /* tom       = ADMIN    */
       (4, 2); /* toeLover  = MANAGER  */

insert into permissions(id, name)
values (1, 'ADD_COMMENT'),
       (2, 'VIEW_MOVIES_LIST'),
       (3, 'CREATE_MOVIE'),
       (4, 'VIEW_PERSONS_LIST'),
       (5, 'VIEW_PERSON'),
       (6, 'UPDATE_PERSON'),
       (7, 'VIEW_MY_PROFILE'),
       (8, 'RATE_MOVIE');

insert into role_to_permissions(roleId, permissionId)
values (1, 1), /* ADMIN    = ADD_COMMENT       */
       (1, 2), /* ADMIN    = VIEW_MOVIES_LIST  */
       (1, 3), /* ADMIN    = CREATE_MOVIE      */
       (1, 4), /* ADMIN    = VIEW_PERSONS_LIST */
       (1, 5), /* ADMIN    = VIEW_PERSON       */
       (1, 6), /* ADMIN    = UPDATE_PERSON     */
       (1, 7), /* ADMIN    = VIEW_MY_PROFILE   */
       (1, 8), /* ADMIN    = RATE_MOVIE        */
       (2, 1), /* MANAGER  = ADD_COMMENT       */
       (2, 2), /* MANAGER  = VIEW_MOVIES_LIST  */
       (2, 3), /* MANAGER  = CREATE_MOVIE      */
       (2, 4), /* MANAGER  = VIEW_PERSONS_LIST */
       (2, 6), /* MANAGER  = UPDATE_PERSON     */
       (2, 7), /* MANAGER  = VIEW_MY_PROFILE   */
       (3, 1), /* REVIEWER = ADD_COMMENT       */
       (3, 2), /* REVIEWER = VIEW_MOVIES_LIST  */
       (3, 6), /* REVIEWER = UPDATE_PERSON     */
       (3, 7), /* REVIEWER = VIEW_MY_PROFILE   */
       (3, 8); /* REVIEWER = RATE_MOVIE        */