#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/epoll.h>
#include <errno.h>
#include <algorithm>
#include <ctime>
#include <sstream>
#include <iostream>
#include <arpa/inet.h>

#define MAXEVENTS 64

using namespace std;

int board[5][5];
int numberOfPlayers;
int numberOfConnectedPlayers = 0;
bool gameCreated = false;
bool gameStarted = false;
int numberOfYellowPlayers = 0;
int numberOfBluePlayers = 0;
int yellowTeam[10];
int blueTeam[10];
int lastDescriptor = 0;

string strArray[100];
static int make_socket_non_blocking (int sfd) {
    int flags, s;

    flags = fcntl (sfd, F_GETFL, 0);
    if (flags == -1) {
        perror ("fcntl");
        return -1;
    }

    flags |= O_NONBLOCK;
    s = fcntl (sfd, F_SETFL, flags);
    if (s == -1) {
        perror ("fcntl");
        return -1;
    }

    return 0;
}

static int create_and_bind (char *port) {
    struct addrinfo hints;
    struct addrinfo *result, *rp;
    int s, sfd;

    memset (&hints, 0, sizeof (struct addrinfo));
    hints.ai_family = AF_UNSPEC;     /* Return IPv4 and IPv6 choices */
    hints.ai_socktype = SOCK_STREAM; /* We want a TCP socket */
    hints.ai_flags = AI_PASSIVE;     /* All interfaces */

    s = getaddrinfo (NULL, port, &hints, &result);
    if (s != 0) {
        fprintf (stderr, "getaddrinfo: %s\n", gai_strerror (s));
        return -1;
    }

    for (rp = result; rp != NULL; rp = rp->ai_next) {
        sfd = socket (rp->ai_family, rp->ai_socktype, rp->ai_protocol);
        if (sfd == -1)
            continue;

        s = bind (sfd, rp->ai_addr, rp->ai_addrlen);
        if (s == 0) {
            /* We managed to bind successfully! */
            break;
        }

        close (sfd);
    }

    if (rp == NULL) {
        fprintf (stderr, "Could not bind\n");
        return -1;
    }

    freeaddrinfo (result);

    return sfd;
}


void createPlansza() {
    for(int i = 0; i < 5; ++i) {
        for(int j = 0; j < 5; ++j) {
            board[i][j] = 0;
        }
    }
    {
        int tab[6] = {1,2,3,4,5,6};
        random_shuffle(tab, tab+5);
        board[0][0]= tab[0];
        board[0][1] = tab[1];
        board[0][2] = tab[2];
        board[1][0] = tab[3];
        board[1][1] = tab[4];
        board[2][0] = tab[5];
    }
    {
        int tab[6] = {11,12,13,14,15,16};
        random_shuffle(tab, tab+5);
        board[4][4]= tab[0];
        board[4][3] = tab[1];
        board[4][2] = tab[2];
        board[3][4] = tab[3];
        board[3][3] = tab[4];
        board[2][4] = tab[5];
    }
}

string intToString(int i){
    stringstream ss;
    string s;
    ss << i;
    ss >> s;
    if (ss.fail()) {
        return "e";
        //TODO
        //e - error
    } else {
        return s;
    }
}
int stringToInt(string s) {
    stringstream ss(s);
    int i;
    ss >> i;
    if (ss.fail()) {
        return -1;
    } else {
        return i;
    }
}

void messageToStringArray(char message[]) {
    stringstream ss(message);
    int i = 0;
    string tmp;
    while(ss >> tmp) {
        strArray[i] = tmp;
        i++;
    }
}

string boardToString(){
    string b = "board";
    for(int i = 0; i < 5; ++i){
        for (int j = 0; j < 5; ++j) {
            b += " ";
            b += intToString(board[i][j]);
        }
    }
    return b;
}

void handleMessage(char message[]) {
    messageToStringArray(message);

    if(strArray[0] == "create") {
        if(!gameCreated) {
            numberOfPlayers = stringToInt(strArray[1]);
            if(numberOfPlayers == -1) {
                {
                    cout << "niepoprawna liczba graczy" << endl;
                    char a[100] = "error create invalid_count";
                    if (write(lastDescriptor, a, 100) == -1) {
                        perror("errorCode");
                    }
                }
            } else {
                createPlansza();
                gameCreated = true;
                ++numberOfConnectedPlayers;
                yellowTeam[numberOfYellowPlayers] = lastDescriptor;
                ++numberOfYellowPlayers;
                {
                    cout << "gra utworzona dla " << numberOfPlayers << " graczy" << endl;
                    char a[100] = "success create";
                    if (write(lastDescriptor, a, 100) == -1) {
                        perror("errorCode");
                    }
                }
                {
                    cout << "gra jest utworzona, gracz dolaczyl do zespolu zoltego" << endl;
                    char a[100] = "success join yellow";
                    if (write(lastDescriptor, a, 100) == -1) {
                        perror("errorCode");
                    }
                }
            }
        } else {
            {
                cout << "gra jest juz utworzona, nie mozna utworzyc kolejnej" << endl;
                char a[100] = "error create exists";
                if (write(lastDescriptor, a, 100) == -1) {
                    perror("errorCode");
                }
            }
            //TODO
            //zdzojnuj jesli sie da
        }
    } else if(strArray[0] == "join") {
        if(gameCreated) {
            if(numberOfConnectedPlayers < 2 * numberOfPlayers) {
                if(numberOfYellowPlayers < numberOfPlayers) {
                    yellowTeam[numberOfYellowPlayers] = lastDescriptor;
                    ++numberOfYellowPlayers;
                    {
                        cout << "gra jest utworzona, gracz dolaczyl do zespolu zoltego" << endl;
                        char a[100] = "success join yellow";
                        if (write(lastDescriptor, a, 100) == -1) {
                            perror("errorCode");
                        }
                    }
                }
                else{
                    blueTeam[numberOfBluePlayers] = lastDescriptor;
                    ++numberOfBluePlayers;
                    {
                        cout << "gra jest utworzona, gracz dolaczyl do zespolu niebieskiego" << endl;
                        char a[100] = "success join blue";
                        if (write(lastDescriptor, a, 100) == -1) {
                            perror("errorCode");
                        }
                    }
                }
                ++numberOfConnectedPlayers;
                if(numberOfConnectedPlayers == 2 * numberOfPlayers){
                    gameStarted = true;
                    cout << "rozpoczynamy gre" << endl;
                    for(int i = 0; i < numberOfPlayers; ++i){
                        {
                            char a[100] = "success game started";
                            if (write(yellowTeam[i], a, 100) == -1) {
                                perror("errorCode");
                            }
                        }
                        {
                            char a[100];
                            strcpy(a, boardToString().c_str());
                            if (write(yellowTeam[i], a, 100) == -1) {
                                perror("errorCode");
                            }
                        }
                        {
                            char a[100] = "success game started";
                            if (write(blueTeam[i], a, 100) == -1) {
                                perror("errorCode");
                            }
                        }
                        {
                            char a[100];
                            strcpy(a, boardToString().c_str());
                            if (write(blueTeam[i], a, 100) == -1) {
                                perror("errorCode");
                            }
                        }


                    }
                }
            } else {
                {
                    cout << "gra trwa, nie ma miejsc w zespolach" << endl;
                    char a[100] = "error join full";
                    if (write(lastDescriptor, a, 100) == -1) {
                        perror("errorCode");
                    }
                }
            }

        } else {
            {
                cout << "gra sie jeszcze nie rozpoczela" << endl;
                char a[100] = "error join not_started";
                if (write(lastDescriptor, a, 100) == -1) {
                    perror("errorCode");
                }
            }
        }
    } else {
        throw "error";
    }
}


int main (int argc, char *argv[]) {
    int sfd, s;
    int efd;
    struct epoll_event event;
    struct epoll_event *events;

    if (argc != 2) {
        fprintf (stderr, "Usage: %s [port]\n", argv[0]);
        exit (EXIT_FAILURE);
    }

    sfd = create_and_bind (argv[1]);
    if (sfd == -1)
        abort ();

    s = make_socket_non_blocking (sfd);
    if (s == -1)
        abort ();

    s = listen (sfd, SOMAXCONN);
    if (s == -1) {
        perror ("listen");
        abort ();
    }

    efd = epoll_create1 (0);
    if (efd == -1) {
        perror ("epoll_create");
        abort ();
    }

    event.data.fd = sfd;
    event.events = EPOLLIN | EPOLLET;
    s = epoll_ctl (efd, EPOLL_CTL_ADD, sfd, &event);
    if (s == -1) {
        perror ("epoll_ctl");
        abort ();
    }

    /* Buffer where events are returned */
    events = (epoll_event*) calloc(MAXEVENTS, sizeof event);

    /* The event loop */
    while (1) {
        int n, i;

        n = epoll_wait (efd, events, MAXEVENTS, -1);
        for (i = 0; i < n; i++) {
            if ((events[i].events & EPOLLERR) ||
                    (events[i].events & EPOLLHUP) ||
                    (!(events[i].events & EPOLLIN))) {
                /* An error has occured on this fd, or the socket is not
                   ready for reading (why were we notified then?) */
                fprintf (stderr, "epoll error\n");
                close (events[i].data.fd);
                continue;
            }

            else if (sfd == events[i].data.fd) {
                /* We have a notification on the listening socket, which
                   means one or more incoming connections. */
                while (1) {
                    struct sockaddr in_addr;
                    socklen_t in_len;
                    int infd;
                    char hbuf[NI_MAXHOST], sbuf[NI_MAXSERV];

                    in_len = sizeof in_addr;
                    infd = accept (sfd, &in_addr, &in_len);
                    if(infd != -1) {
                        lastDescriptor = infd;
                    }
                    if (infd == -1) {
                        if ((errno == EAGAIN) ||
                                (errno == EWOULDBLOCK)) {
                            /* We have processed all incoming
                               connections. */
                            break;
                        } else {
                            perror ("accept");
                            break;
                        }
                    }

                    s = getnameinfo (&in_addr, in_len,
                                     hbuf, sizeof hbuf,
                                     sbuf, sizeof sbuf,
                                     NI_NUMERICHOST | NI_NUMERICSERV);
                    if (s == 0) {
                        printf("Accepted connection on descriptor %d "
                               "(host=%s, port=%s)\n", infd, hbuf, sbuf);
                    }

                    /* Make the incoming socket non-blocking and add it to the
                       list of fds to monitor. */
                    s = make_socket_non_blocking (infd);
                    if (s == -1)
                        abort ();

                    event.data.fd = infd;
                    event.events = EPOLLIN | EPOLLET;
                    s = epoll_ctl (efd, EPOLL_CTL_ADD, infd, &event);
                    if (s == -1) {
                        perror ("epoll_ctl");
                        abort ();
                    }
                }
                continue;
            } else {
                /* We have data on the fd waiting to be read. Read and
                   display it. We must read whatever data is available
                   completely, as we are running in edge-triggered mode
                   and won't get a notification again for the same
                   data. */
                int done = 0;

                while (1) {
                    ssize_t count;
                    char buf[512];

                    count = read (events[i].data.fd, buf, sizeof buf);
                    if (count == -1) {
                        /* If errno == EAGAIN, that means we have read all
                           data. So go back to the main loop. */
                        if (errno != EAGAIN) {
                            perror ("read");
                            done = 1;
                        }
                        break;
                    } else if (count == 0) {
                        /* End of file. The remote has closed the
                           connection. */
                        done = 1;
                        break;
                    }

                    /* Write the buffer to standard output */
                    handleMessage(buf);
                    s = write (1, buf, count);
                    if (s == -1) {
                        perror ("write");
                        abort ();
                    }
                }

                if (done) {
                    printf ("Closed connection on descriptor %d\n",
                            events[i].data.fd);

                    /* Closing the descriptor will make epoll remove it
                       from the set of descriptors which are monitored. */
                    close (events[i].data.fd);
                }
            }
        }
    }

    free (events);

    close (sfd);

    return EXIT_SUCCESS;
}
