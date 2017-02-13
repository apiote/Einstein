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
string activeTeam = "none";
int numberRolled = 0;
vector<pair<int, int>> possibleStones;
pair<int, int> selectedStone;
vector<pair<int, int>> possibleMoves;
pair<int, int> votesForStone;
int votesForMove[3];
bool voteStoneNeeded = false;
bool voteMoveNeeded = false;

int roll() {
    srand(time(0));
    return rand() % 6 + 1;
}

void setPossibleStones() {
    bool myNumbers[6];
    for (int i = 0; i < 6; ++i) {
        myNumbers[i] = 0;
    }
    for (int i = 0; i < 5; ++i) {
        for (int j = 0; j < 5; ++j) {
            if (activeTeam == "yellow") {
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
    possibleStones.clear();
    if (myNumbers[numberRolled]) {
        for (int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                if (activeTeam == "yellow") {
                    if (board[i][j] == numberRolled) {
                        pair<int, int> stone = make_pair(i, j);
                        possibleStones.push_back(stone);
                    }
                }
                else {
                    if (board[i][j] - 10 == numberRolled) {
                        pair<int, int> stone = make_pair(i, j);
                        possibleStones.push_back(stone);
                    }
                }
            }
        }
    }
    else {
        int n = numberRolled + 1;
        while (n < 7) {
            if (myNumbers[n]) {
                for (int i = 0; i < 5; ++i) {
                    for (int j = 0; j < 5; ++j) {
                        if (activeTeam == "yellow") {
                            if (board[i][j] == n) {
                                pair<int, int> stone = make_pair(i, j);
                                possibleStones.push_back(stone);
                            }
                        }
                        else {
                            if (board[i][j] - 10 == n) {
                                pair<int, int> stone = make_pair(i, j);
                                possibleStones.push_back(stone);
                            }
                        }
                    }
                }
                break;
            }
            ++n;
        }
        n = numberRolled - 1;
        while (n > 0) {
            if (myNumbers[n]) {
                for (int i = 0; i < 5; ++i) {
                    for (int j = 0; j < 5; ++j) {
                        if (activeTeam == "yellow") {
                            if (board[i][j] == n) {
                                pair<int, int> stone = make_pair(i, j);
                                possibleStones.push_back(stone);
                            }
                        }
                        else {
                            if (board[i][j] - 10 == n) {
                                pair<int, int> stone = make_pair(i, j);
                                possibleStones.push_back(stone);
                            }
                        }
                    }
                }
                break;
            }
            --n;
        }
    }
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
    if (activeTeam == "yellow") {
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

void sendActiveTeam() {
    string s;
    if (activeTeam == "yellow") {
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
    if (activeTeam == "yellow") {
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
    if (activeTeam == "yellow") {
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
        msg += intToString(selectedStone.first);
        msg += " ";
        msg += intToString(selectedStone.second);
        msg += " selected\n";
    }
    else {
        msg += "not_selected";
    }
    if (activeTeam == "yellow") {
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
    msg += intToString(selectedStone.first);
    msg += " ";
    msg += intToString(selectedStone.second);
    msg += " moved to ";
    msg += intToString(destination.first);
    msg += " ";
    msg += intToString(destination.second);
    msg += " by ";
    msg += activeTeam;
    msg += '\n';
    for (int i = 0; i < numberOfPlayers; ++i) {
        writeN(yellowTeam[i], msg);
        writeN(blueTeam[i], msg);
    }
}

void setPossibleMoves() {
    possibleMoves.clear();
    if (activeTeam == "yellow") {
        if (selectedStone.first < 4) {
            pair<int, int> p = make_pair(selectedStone.first + 1, selectedStone.second);
            possibleMoves.push_back(p);
        }
        if (selectedStone.second < 4) {
            pair<int, int> p = make_pair(selectedStone.first, selectedStone.second + 1);
            possibleMoves.push_back(p);
        }
        if(possibleMoves.size() == 2){
            pair<int, int> p = make_pair(selectedStone.first + 1, selectedStone.second + 1);
            possibleMoves.push_back(p);
        }
    }
    else {
        if (selectedStone.first > 0) {
            pair<int, int> p = make_pair(selectedStone.first - 1, selectedStone.second);
            possibleMoves.push_back(p);
        }
        if (selectedStone.second > 0) {
            pair<int, int> p = make_pair(selectedStone.first, selectedStone.second - 1);
            possibleMoves.push_back(p);
        }
        if(possibleMoves.size() == 2){
            pair<int, int> p = make_pair(selectedStone.first - 1, selectedStone.second - 1);
            possibleMoves.push_back(p);
        }
    }
}

void sendEndGame(string winner, string reason){
    cout << winner << " won " << "because of " << reason << endl;
    string msg = "success game ended ";
    msg += winner;
    msg += " ";
    msg += reason;
    msg += '\n';
    for (int i = 0; i < numberOfPlayers; ++i) {
        writeN(yellowTeam[i], msg);
        writeN(blueTeam[i], msg);
    }
}

void checkIfEndGame(){
    if(board[0][0] > 10){
        sendEndGame("blue", "corner");
    }
    if(board[4][4] > 0 && board[4][4] < 7){
        sendEndGame("blue", "corner");
    }
    bool yellowStonesOnTheBoard = false;
    bool blueStonesOnTheBoard = false;
    for (int i = 0; i < 5; ++i) {
        for (int j = 0; j < 5; ++j) {
            if(board[i][j] > 0){
                if(board[i][j] > 10){
                    blueStonesOnTheBoard = true;
                }
                else{
                    yellowStonesOnTheBoard = true;
                }
            }
        }
    }
    if(!blueStonesOnTheBoard){
        sendEndGame("yellow", "no_stones");
    }
    if(!yellowStonesOnTheBoard){
        sendEndGame("blue", "no_stones");
    }
    //TODO check no vote?
    //TODO end game if needed
}

void startTurn(string color);

void changeTurn(){
    if(activeTeam == "yellow"){
        startTurn("blue");
    }
    else{
        startTurn("yellow");
    }
}

void doMove(pair<int, int> destination) {
    int selectedStoneNumber = board[selectedStone.first][selectedStone.second];
    board[selectedStone.first][selectedStone.second] = 0;
    board[destination.first][destination.second] = selectedStoneNumber;
    cout << "moved " << selectedStone.first << " " << selectedStone.second
    << " to " << destination.first << " " << destination.second << " by " << activeTeam << endl;
    sendMoveDone(destination);
    checkIfEndGame();
    changeTurn();
}

void startVote(){
    votesForStone.first = 0;
    votesForStone.second = 0;
    for (int i = 0; i < 3; ++i) {
        votesForMove[i] = 0;
    }
}

void selectStone(pair<int, int> stone){
    selectedStone.first = stone.first;
    selectedStone.second = stone.second;
    cout << "selected stone " << selectedStone.first << " " << selectedStone.second << endl;
    sendStoneSelected(true);

    voteMoveNeeded = false;
    setPossibleMoves();
    cout << "possible moves: " << endl;
    for (int i = 0; i < possibleMoves.size(); ++i) {
        cout << possibleMoves[i].first << " " << possibleMoves[i].second << endl;
    }
    if (possibleMoves.size() > 1) {
        voteMoveNeeded = true;
    }
    if (voteMoveNeeded) {
        cout << "vote move needed" << endl;
        sendMoveVote(true);
        startVote();
    }
    else {
        cout << "vote move not needed" << endl;
        sendMoveVote(false);
        doMove(possibleMoves[0]);
        sendMoveDone(possibleMoves[0]);
    }
}

void startTurn(string color) {
    activeTeam = color;
    cout << color << " turn" << endl;
    sendActiveTeam();
    numberRolled = roll();
    cout << "rolled " << numberRolled << endl;
    sendRolled();
    setPossibleStones();
    voteStoneNeeded = false;
    voteMoveNeeded = false;
    if (possibleStones.size() > 1) {
        voteStoneNeeded = true;
    }
    if (voteStoneNeeded) {
        cout << "vote stone needed" << endl;
        sendStoneVote(true);
        cout << "possible stones: " << endl;
        for (int i = 0; i < possibleStones.size(); ++i) {
            cout << possibleStones[i].first << " " << possibleStones[i].second << endl;
        }
        startVote();
    }
    else {
        cout << "vote stone not needed" << endl;
        sendStoneVote(false);
        selectStone(possibleStones[0]);

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
            cout << "both teams are full" << endl;
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
    if(activeTeam == "yellow"){
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

void checkIfVoteForMoveCanEnd(){
    if (votesForMove[0] + votesForMove[1] + votesForMove[2] == numberOfPlayers) {
        cout << "voting finished" << endl;
        if (votesForMove[0] > votesForMove[1] && votesForMove[0] > votesForMove[2]) {
            doMove(possibleMoves[0]);
        }
        else if (votesForMove[1] > votesForMove[0] && votesForMove[1] > votesForMove[2]) {
            doMove(possibleMoves[1]);
        }
        else if(votesForMove[2] > votesForMove[0] && votesForMove[2] > votesForMove[1]){
            doMove(possibleMoves[2]);
        }
        else {
            cout << "tie" << endl;
            //TODO tie
        }
        //TODO if tie do something else
        //voteMoveNeeded = false;
    }
}

void voteForMove(int sender, pair<int, int> move){
    string msg = "success vote move ";
    msg += intToString(move.first);
    msg += " ";
    msg += intToString(move.second);
    msg += '\n';
    for (int i = 0; i < numberOfPlayers; ++i) {
        if(activeTeam == "yellow") {
            writeN(yellowTeam[i], msg);
        }
        else{
            writeN(blueTeam[i], msg);
        }
    }
    bool correctMove = false;
    for (int j = 0; j < 3; ++j) {
        if (possibleMoves[j].first == move.first && possibleMoves[j].second == move.second) {
            ++votesForMove[j];
            cout << "voted for move " << move.first << " " << move.second << endl;
            correctMove = true;
            break;
        }
    }
    if(!correctMove){
        cout << "failed to vote for move " << move.first << " " << move.second << endl;
        writeN(sender, "error vote move invalid\n");
    }
    checkIfVoteForMoveCanEnd();
    cout << "current votes: ";
    for (int k = 0; k < 3; ++k) {
        cout << votesForMove[k] << " ";
    }
    cout << endl;
}

void checkIfVoteForStoneCanEnd(){
    if (votesForStone.first + votesForStone.second == numberOfPlayers) {
        cout << "voting finished" << endl;
        if (votesForStone.first > votesForStone.second) {
            selectStone(possibleStones[0]);
        }
        else if (votesForStone.first < votesForStone.second) {
            selectStone(possibleStones[1]);
        }
        else {
            cout << "tie" << endl;
            //TODO tie
        }
        //TODO if tie do something else
        //voteStoneNeeded = false;
    }
}

void voteForStone(int sender, pair<int, int> stone){
    string msg = "success vote stone ";
    msg += intToString(stone.first);
    msg += " ";
    msg += intToString(stone.second);
    msg += '\n';
    for (int i = 0; i < numberOfPlayers; ++i) {
        if(activeTeam == "yellow") {
            writeN(yellowTeam[i], msg);
        }
        else{
            writeN(blueTeam[i], msg);
        }
    }
    if (possibleStones[0].first == stone.first && possibleStones[0].second == stone.second) {
        ++votesForStone.first;
        cout << "voted for stone " << stone.first << " " << stone.second << endl;
    }
    else if (possibleStones[1].first == stone.first && possibleStones[1].second == stone.second) {
        ++votesForStone.second;
        cout << "voted for stone " << stone.first << " " << stone.second << endl;
    }
    else {
        cout << "failed to vote for stone " << stone.first << " " << stone.second << endl;
        writeN(sender, "error vote stone not_selectable\n");
    }
    checkIfVoteForStoneCanEnd();
    cout << "current votes: " << votesForStone.first << " " << votesForStone.second << endl;
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
            writeN(sender, "error game not_started\n");
        }//else if not your turn
        else{
            if(isHisTurn(sender)) {
                if (strArray[1] == "stone") {
                    if (voteStoneNeeded) {
                        int x = stringToInt(strArray[2]);
                        int y = stringToInt(strArray[3]);
                        //TODO errors
                        pair<int, int> stone = make_pair(x, y);
                        //TODO check if already voted
                        voteForStone(sender, stone);
                    }
                    else{
                        cout << "vote for stone is not needed at this moment" << endl;
                        writeN(sender, "error vote stone not_needed\n");
                    }
                }
                else if (strArray[1] == "move") {
                    if (voteMoveNeeded) {
                        int x = stringToInt(strArray[2]);
                        int y = stringToInt(strArray[3]);
                        //TODO errors
                        pair<int, int> move = make_pair(x, y);
                        //TODO check if already voted
                        voteForMove(sender, move);
                    }
                    else{
                        cout << "vote for move is not needed at this moment" << endl;
                        writeN(sender, "error vote move not_needed\n");
                    }
                }
                else {
                    cout << "unknown request" << endl;
                    writeN(sender, "error request_unknown\n");
                }
            }
            else{
                cout << "it is not player turn" << endl;
                writeN(sender, "error vote move not_your_turn\n");
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
