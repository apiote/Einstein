#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/epoll.h>
#include <errno.h>
#include <algorithm>
#include <sstream>
#include <iostream>
#include <cstdlib>
#include <vector>
#include <set>
#include <utility>

#define MAXEVENTS 64

using namespace std;

int board[5][5];
int numberOfPlayers = 0;
int numberOfConnectedPlayers = 0;
bool gameCreated = false;
bool gameStarted = false;
int numberOfYellowPlayers = 0;
int numberOfBluePlayers = 0;
int yellowTeam[10];
int blueTeam[10];
string strArray[100];
string activeGroup = "none";
int numberRolled = 0;
vector<int> possibleStones;
int selectedStone = 0;
pair<int, int> selectedStonePosition;
vector<pair<int, int>> possibleMoves;
pair<int, int> votes;
bool voteStoneNeeded = false;
bool voteMoveNeeded = false;

int roll() {
    srand(time(0));
    return rand() % 6 + 1;
}

vector<int> getPossibleMoves() {
    bool myNumbers[6];
    for (int i = 0; i < 6; ++i) {
        myNumbers[i] = 0;
    }
    for (int i = 0; i < 5; ++i) {
        for (int j = 0; j < 5; ++j) {
            if (activeGroup == "yellow") {
                if (board[i][j] > 0 && board[i][j] < 7) {
                    myNumbers[board[i][j]] = true;
                }
            }
            else {
                if (board[i][j] > 10 && board[i][j] < 17) {
                    myNumbers[board[i][j] - 10] = true;
                }
            }
        }
    }
    vector<int> c;
    if (myNumbers[numberRolled]) {
        c.push_back(numberRolled);
    }
    else {
        int i = numberRolled + 1;
        while (i < 7) {
            if (myNumbers[i]) {
                c.push_back(i);
                break;
            }
            ++i;
        }
        i = numberRolled - 1;
        while (i > 0) {
            if (myNumbers[i]) {
                c.push_back(i);
                break;
            }
            --i;
        }
    }
    return c;
}

void writeN(int sender, string msg) {
    char a[100];
    int i;
    for (i = 0; i < msg.size(); ++i) {
        a[i] = msg[i];
    }
    if (write(sender, a, msg.size()) == -1) {
        perror("errorCode");
    }
}

void createBoard() {
    srand(time(0));
    for (int i = 0; i < 5; ++i) {
        for (int j = 0; j < 5; ++j) {
            board[i][j] = 0;
        }
    }
    {
        int tab[6] = {1, 2, 3, 4, 5, 6};
        random_shuffle(tab, tab + 5);
        board[0][0] = tab[0];
        board[0][1] = tab[1];
        board[0][2] = tab[2];
        board[1][0] = tab[3];
        board[1][1] = tab[4];
        board[2][0] = tab[5];
    }
    {
        int tab[6] = {11, 12, 13, 14, 15, 16};
        random_shuffle(tab, tab + 5);
        board[4][4] = tab[0];
        board[4][3] = tab[1];
        board[4][2] = tab[2];
        board[3][4] = tab[3];
        board[3][3] = tab[4];
        board[2][4] = tab[5];
    }
}

string intToString(int i) {
    stringstream ss;
    string s;
    ss << i;
    ss >> s;
    if (ss.fail()) {
        return "e";
        //TODO e - error
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
    while (ss >> tmp) {
        strArray[i] = tmp;
        i++;
    }
}

string boardToString() {
    string b = "board";
    for (int i = 0; i < 5; ++i) {
        for (int j = 0; j < 5; ++j) {
            b += " ";
            b += intToString(board[i][j]);
        }
    }
    return b;
}

void sendRolled() {
    string msg = "success rolled ";
    msg += intToString(numberRolled);
    msg += '\n';
    if (activeGroup == "yellow") {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(yellowTeam[i], msg);
        }
    }
    else {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(blueTeam[i], msg);
        }
    }
}

void sendActiveGroup() {
    string s;
    if (activeGroup == "yellow") {
        s = "success active yellow\n";
    }
    else {
        s = "success active blue\n";
    }
    for (int i = 0; i < numberOfPlayers; ++i) {
        writeN(yellowTeam[i], s);
        writeN(blueTeam[i], s);
    }
}

void sendStoneVote(bool needed) {
    string msg = "success vote stone ";
    if (needed) {
        msg += "needed\n";
    }
    else {
        msg += "not_needed\n";
    }
    if (activeGroup == "yellow") {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(yellowTeam[i], msg);
        }
    }
    else {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(blueTeam[i], msg);
        }
    }
}

void sendMoveVote(bool needed) {
    string msg = "success vote move ";
    if (needed) {
        msg += "needed\n";
    }
    else {
        msg += "not_needed\n";
    }
    if (activeGroup == "yellow") {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(yellowTeam[i], msg);
        }
    }
    else {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(blueTeam[i], msg);
        }
    }
}

void sendStoneSelected(bool selected) {
    string msg = "success stone ";
    if (selected) {
        msg += intToString(selectedStonePosition.first);
        msg += " ";
        msg += intToString(selectedStonePosition.second);
        msg += " selected\n";
    }
    else {
        msg += "not_selected";
    }
    if (activeGroup == "yellow") {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(yellowTeam[i], msg);
        }
    }
    else {
        for (int i = 0; i < numberOfPlayers; ++i) {
            writeN(blueTeam[i], msg);
        }
    }
}

void sendMoveDone(pair<int, int> destination) {
    string msg = "success stone ";
    msg += intToString(selectedStonePosition.first);
    msg += " ";
    msg += intToString(selectedStonePosition.second);
    msg += " moved to ";
    msg += intToString(destination.first);
    msg += " ";
    msg += intToString(destination.second);
    msg += '\n';
    for (int i = 0; i < numberOfPlayers; ++i) {
        writeN(yellowTeam[i], msg);
        writeN(blueTeam[i], msg);
    }
}

void setSelectedStonePosition() {
    for (int i = 0; i < 5; ++i) {
        for (int j = 0; j < 5; ++j) {
            if (activeGroup == "yellow") {
                if (board[i][j] == selectedStone) {
                    selectedStonePosition.first = i;
                    selectedStonePosition.second = j;
                }
            }
            else {
                if (board[i][j] - 10 == selectedStone) {
                    selectedStonePosition.first = i;
                    selectedStonePosition.second = j;
                }
            }
        }
    }
}

void setPossibleMoves() {
    possibleMoves.clear();
    if (activeGroup == "yellow") {
        if (selectedStonePosition.first < 4) {
            pair<int, int> p = make_pair(selectedStonePosition.first + 1, selectedStonePosition.second);
            possibleMoves.push_back(p);
        }
        if (selectedStonePosition.second < 4) {
            pair<int, int> p = make_pair(selectedStonePosition.first, selectedStonePosition.second + 1);
            possibleMoves.push_back(p);
        }
    }
    else {
        if (selectedStonePosition.first > 0) {
            pair<int, int> p = make_pair(selectedStonePosition.first - 1, selectedStonePosition.second);
            possibleMoves.push_back(p);
        }
        if (selectedStonePosition.second > 0) {
            pair<int, int> p = make_pair(selectedStonePosition.first, selectedStonePosition.second - 1);
            possibleMoves.push_back(p);
        }
    }
}

void doMove(pair<int, int> destination) {
    board[selectedStonePosition.first][selectedStonePosition.second] = 0;
    if (activeGroup == "yellow") {
        board[destination.first][destination.second] = selectedStone;
    }
    else {
        board[destination.first][destination.second] = selectedStone + 10;
    }
    cout << "moved " << selectedStonePosition.first << " " << selectedStonePosition.second
    << " to " << destination.first << " " << destination.second << endl;
    //TODO send move moved info
    //TODO end turn
}

void startStoneVote(){
    votes.first = 0;
    votes.second = 0;
}

void startTurn(string color) {
    activeGroup = color;
    cout << color << " turn" << endl;
    sendActiveGroup();

    numberRolled = roll();
    cout << "rolled " << numberRolled << endl;
    sendRolled();
    possibleStones = getPossibleMoves();
    voteStoneNeeded = false;
    if (possibleStones.size() > 1) {
        voteStoneNeeded = true;
    }
    if (voteStoneNeeded) {
        cout << "vote stone needed" << endl;
        sendStoneVote(true);
        startStoneVote();
    }
    else {
        cout << "vote stone not needed" << endl;
        sendStoneVote(false);
        selectedStone = possibleStones[0];
        setSelectedStonePosition();
        cout << "selected stone " << selectedStonePosition.first << " " << selectedStonePosition.second << endl;
        sendStoneSelected(true);
        voteMoveNeeded = false;
        setPossibleMoves();
        for (int i = 0; i < possibleMoves.size(); ++i) {
            cout << possibleMoves[i].first << " " << possibleMoves[i].second << endl;
        }
        if (possibleMoves.size() > 1) {
            voteMoveNeeded = true;
        }
        if (voteMoveNeeded) {
            cout << "vote move needed" << endl;
            sendMoveVote(true);
            //TODO vote
        }
        else {
            cout << "vote move not needed" << endl;
            sendMoveVote(false);
            doMove(possibleMoves[0]);
            sendMoveDone(possibleMoves[0]);
        }
    }
}

void startGame() {
    gameStarted = true;
    cout << "game starts" << endl;
    for (int i = 0; i < numberOfPlayers; ++i) {
        writeN(yellowTeam[i], "success game started\n");
        writeN(blueTeam[i], "success game started\n");
        string s = "success ";
        s += boardToString();
        s += '\n';
        writeN(yellowTeam[i], s);
        writeN(blueTeam[i], s);
    }
    startTurn("yellow");
}

bool alreadyJoined(int sender) {
    for (int i = 0; i < numberOfYellowPlayers; ++i) {
        if (sender == yellowTeam[i]) {
            return true;
        }
    }
    for (int i = 0; i < numberOfBluePlayers; ++i) {
        if (sender == blueTeam[i]) {
            return true;
        }
    }
    return false;
}

void joinIfPossible(int sender) {
    if (alreadyJoined(sender)) {
        cout << "player already joined" << endl;
        writeN(sender, "error join already_joined\n");
    }
    else {
        if (numberOfConnectedPlayers < 2 * numberOfPlayers) {
            if (numberOfYellowPlayers < numberOfPlayers) {
                yellowTeam[numberOfYellowPlayers] = sender;
                ++numberOfYellowPlayers;
                cout << "player joined yellow team" << endl;
                writeN(sender, "success join yellow\n");
            }
            else {
                blueTeam[numberOfBluePlayers] = sender;
                ++numberOfBluePlayers;
                cout << "player joined blue team" << endl;
                writeN(sender, "success join blue\n");
            }
            ++numberOfConnectedPlayers;
            if (numberOfConnectedPlayers == 2 * numberOfPlayers) {
                startGame();
            }
        }
        else {
            cout << "nie ma miejsc w zespolach" << endl;
            writeN(sender, "error join full\n");
        }
    }
}

void createGame(int sender) {
    numberOfPlayers = stringToInt(strArray[1]);
    //TODO check if strArray has [1]
    if (numberOfPlayers < 1) {
        cout << "invalid player count" << endl;
        writeN(sender, "error create invalid_count\n");
    }
    else {
        createBoard();
        gameCreated = true;

        cout << "game created for " << numberOfPlayers << " players" << endl;
        writeN(sender, "success create\n");

        joinIfPossible(sender);
    }
}

bool isHisTurn(int sender){
    if(activeGroup == "yellow"){
        for (int i = 0; i < numberOfPlayers; ++i) {
            if(sender == yellowTeam[i]){
                return true;
            }
        }
    }
    else{
        for (int i = 0; i < numberOfPlayers; ++i) {
            if(sender == blueTeam[i]){
                return true;
            }
        }
    }
    return false;
}

void checkIfVoteCanEnd(){
    if (votes.first + votes.second == numberOfPlayers) {
        cout << "voting finished" << endl;
        if (votes.first > votes.second) {
            doMove(possibleMoves[0]);
        }
        else if (votes.first < votes.second) {
            doMove(possibleMoves[1]);
        }
        else {
            //TODO tie
        }
    }
}

void voteForMove(int sender, pair<int, int> move){
    string msg = "vote move ";
    msg += intToString(selectedStonePosition.first);
    msg += " ";
    msg += intToString(selectedStonePosition.second);
    msg += " to ";
    msg += intToString(move.first);
    msg += " ";
    msg += intToString(move.second);
    if (possibleMoves[0].first == move.first && possibleMoves[0].second == move.second) {
        ++votes.first;
        cout << "voted for move " << move.first << " " << move.second << endl;
        string message = "success ";
        message += msg;
        writeN(sender, message);
    }
    else if (possibleMoves[1].first == move.first && possibleMoves[1].second == move.second) {
        ++votes.second;
        cout << "voted for move " << move.first << " " << move.second << endl;
        string message = "success ";
        message += msg;
        writeN(sender, message);
    }
    else {
        cout << "failed to vote for move " << move.first << " " << move.second << endl;
        string message = "error ";
        message += msg;
        writeN(sender, message);
    }
    checkIfVoteCanEnd();
}

void handleMessage(char message[], int sender) {
    messageToStringArray(message);

    if (strArray[0] == "create") {
        if (!gameCreated) {
            createGame(sender);
        }
        else {
            cout << "game already exists" << endl;
            writeN(sender, "error create already_exists\n");
            joinIfPossible(sender);
        }
    }
    else if (strArray[0] == "join") {
        if (gameCreated) {
            joinIfPossible(sender);
        }
        else {
            cout << "game hasn't started yet" << endl;
            writeN(sender, "error join not_started\n");
        }
    }
    else if(strArray[0] == "vote"){
        if(!gameStarted){
            cout << "game hasn't started yet" << endl;
            writeN(sender, "error join not_started\n");
        }//else if not your turn
        else{
            if(isHisTurn(sender)) {
                if (strArray[1] == "stone") {
                    //TODO vote for stone
                }
                else if (strArray[1] == "move") {
                    if (voteMoveNeeded) {
                        int x = stringToInt(strArray[2]);
                        int y = stringToInt(strArray[3]);
                        //TODO errors
                        pair<int, int> move = make_pair(x, y);
                        voteForMove(sender, move);
                    }
                    else{
                        //TODO vote move not needed
                    }
                }
                else {
                    //TODO correct info
                    cout << "unknown request" << endl;
                    writeN(sender, "error request_unknown\n");
                }
            }
            else{
                //TODO not his turn
            }
        }
    }
    else {
        cout << "unknown request" << endl;
        writeN(sender, "error request_unknown\n");
    }
}

static int make_socket_non_blocking(int sfd) {
    int flags, s;

    flags = fcntl(sfd, F_GETFL, 0);
    if (flags == -1) {
        perror("fcntl");
        return -1;
    }

    flags |= O_NONBLOCK;
    s = fcntl(sfd, F_SETFL, flags);
    if (s == -1) {
        perror("fcntl");
        return -1;
    }

    return 0;
}

static int create_and_bind(char *port) {
    struct addrinfo hints;
    struct addrinfo *result, *rp;
    int s, sfd;

    memset(&hints, 0, sizeof(struct addrinfo));
    hints.ai_family = AF_UNSPEC;     /* Return IPv4 and IPv6 choices */
    hints.ai_socktype = SOCK_STREAM; /* We want a TCP socket */
    hints.ai_flags = AI_PASSIVE;     /* All interfaces */

    s = getaddrinfo(NULL, port, &hints, &result);
    if (s != 0) {
        fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(s));
        return -1;
    }

    for (rp = result; rp != NULL; rp = rp->ai_next) {
        sfd = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol);
        if (sfd == -1)
            continue;

        s = bind(sfd, rp->ai_addr, rp->ai_addrlen);
        if (s == 0) {
            /* We managed to bind successfully! */
            break;
        }

        close(sfd);
    }

    if (rp == NULL) {
        fprintf(stderr, "Could not bind\n");
        return -1;
    }

    freeaddrinfo(result);

    return sfd;
}

int main(int argc, char *argv[]) {
    int sfd, s;
    int efd;
    struct epoll_event event;
    struct epoll_event *events;

    if (argc != 2) {
        fprintf(stderr, "Usage: %s [port]\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    sfd = create_and_bind(argv[1]);
    if (sfd == -1)
        abort();

    s = make_socket_non_blocking(sfd);
    if (s == -1)
        abort();

    s = listen(sfd, SOMAXCONN);
    if (s == -1) {
        perror("listen");
        abort();
    }

    efd = epoll_create1(0);
    if (efd == -1) {
        perror("epoll_create");
        abort();
    }

    event.data.fd = sfd;
    event.events = EPOLLIN | EPOLLET;
    s = epoll_ctl(efd, EPOLL_CTL_ADD, sfd, &event);
    if (s == -1) {
        perror("epoll_ctl");
        abort();
    }

    /* Buffer where events are returned */
    events = (epoll_event *) calloc(MAXEVENTS, sizeof event);

    /* The event loop */
    while (1) {
        int n, i;

        n = epoll_wait(efd, events, MAXEVENTS, -1);
        for (i = 0; i < n; i++) {
            if ((events[i].events & EPOLLERR) ||
                (events[i].events & EPOLLHUP) ||
                (!(events[i].events & EPOLLIN))) {
                /* An error has occured on this fd, or the socket is not
                   ready for reading (why were we notified then?) */
                fprintf(stderr, "epoll error\n");
                close(events[i].data.fd);
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
                    infd = accept(sfd, &in_addr, &in_len);

                    if (infd == -1) {
                        if ((errno == EAGAIN) ||
                            (errno == EWOULDBLOCK)) {
                            /* We have processed all incoming
                               connections. */
                            break;
                        } else {
                            perror("accept");
                            break;
                        }
                    }

                    s = getnameinfo(&in_addr, in_len,
                                    hbuf, sizeof hbuf,
                                    sbuf, sizeof sbuf,
                                    NI_NUMERICHOST | NI_NUMERICSERV);
                    if (s == 0) {
                        printf("Accepted connection on descriptor %d "
                                       "(host=%s, port=%s)\n", infd, hbuf, sbuf);
                    }

                    /* Make the incoming socket non-blocking and add it to the
                       list of fds to monitor. */
                    s = make_socket_non_blocking(infd);
                    if (s == -1)
                        abort();

                    event.data.fd = infd;
                    event.events = EPOLLIN | EPOLLET;
                    s = epoll_ctl(efd, EPOLL_CTL_ADD, infd, &event);
                    if (s == -1) {
                        perror("epoll_ctl");
                        abort();
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

                    count = read(events[i].data.fd, buf, sizeof buf);
                    if (count == -1) {
                        /* If errno == EAGAIN, that means we have read all
                           data. So go back to the main loop. */
                        if (errno != EAGAIN) {
                            perror("read");
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
                    s = write(1, buf, count);
                    handleMessage(buf, events[i].data.fd);
                    if (s == -1) {
                        perror("write");
                        abort();
                    }
                }

                if (done) {
                    printf("Closed connection on descriptor %d\n",
                           events[i].data.fd);

                    /* Closing the descriptor will make epoll remove it
                       from the set of descriptors which are monitored. */
                    close(events[i].data.fd);
                }
            }
        }
    }

    free(events);

    close(sfd);

    return EXIT_SUCCESS;
}
