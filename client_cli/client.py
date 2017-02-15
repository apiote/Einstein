#!/usr/bin/env python3

import curses
import socket
import threading
import time
import sys

stdscr = curses.initscr()

errorMessages = {'already_exists': 'Game already exists on server', 'invalid_count': 'Wrong number of players',
                 'full': 'All players are already in game', 'not_started': 'Game has not started yet', 'already_joined': 'You have already joined the game'}

errorText = ''
hintText = ''
command = ''
myTeam = ''
server = ''
roll = -1

allowedVerbs = {}
votes = {}
gameEnded = False

curses.noecho()
curses.start_color()
curses.use_default_colors()

statusBox = curses.newwin(3, curses.COLS, 0, 0)
boardBox = curses.newwin(15, 30, 5, 0)
textBox = curses.newwin(1, curses.COLS, curses.LINES - 1, 0)

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

board = [[0, 0, 0, 0, 0],
         [0, 0, 0, 0, 0],
         [0, 0, 0, 0, 0],
         [0, 0, 0, 0, 0],
         [0, 0, 0, 0, 0]]

curses.init_pair(2, -1, -1)
curses.init_pair(1, 208, -1)
curses.init_pair(11, 21, -1)
curses.init_pair(101, 208, 231)
curses.init_pair(111, 21, 231)
curses.init_pair(1001, 208, 87)
curses.init_pair(1011, 21, 87)


def socketPrintLine(sock, message):
    sock.sendall(bytes(message + '\n', 'utf-8'))


def socketReadLine(sock):
    response = ''
    while True:
        c = sock.recv(1).decode('utf-8')
        print(c, file=sys.stderr, end='')
        if c != '\n':
            response += c
        else:
            print('\n', file=sys.stderr)
            return response
# todo what if (when `exit`) i close a socket while recv’ing and (throw and) catch an exception


def waitForBoard():
    socketReadLine(client)
    response=socketReadLine(client)
    i=0
    for point in response.split(' ')[2:]:
        board[int(i / 5)][int(i % 5)]=int(point)
        i += 1
    drawBoard()


def drawBoard():
    print('#drawing board', file=sys.stderr)
    boardBox.move(0, 0)
    boardBox.addstr('   A   B   C   D   E\n')
    boardBox.addstr(' ╭───┬───┬───┬───┬───╮\n')
    i=0
    for row in board:
        boardBox.addstr(str(i+1))
        for box in row:
            boardBox.addstr('│ ')
            boardBox.addstr(str(box % 10), curses.color_pair(
                10 * int(box / 10) + 1) if box != 0 else curses.color_pair(2)) 
            boardBox.addstr(' ')
        boardBox.addstr('│\n')
        print
        if(i < 4):
            boardBox.addstr(' ├───┼───┼───┼───┼───┤\n')
        i += 1
    boardBox.addstr(' ╰───┴───┴───┴───┴───╯\n')
    boardBox.refresh()


def startGame():
   threading.Thread(target=runGame).start()


def runGame():
    global allowedVerbs
    global votes
    global hintText
    global errorText
    global roll
    won=None
    while won == None:
        response=socketReadLine(client)
        allowedVerbs={'exit'}
        if response.split(' ')[1] == 'active' and response.split(' ')[2] == myTeam:
            errorText='Your move'
            response=socketReadLine(client)
            roll=int(response.split(' ')[2])
            response=socketReadLine(client)
            if response.split(' ')[3] == 'needed':
                highlightSelectables(roll)
                allowedVerbs = {'exit', 'select'}
                hintText='Type `select {n}` to vote for Your stone with number n'
            votes={}
            votingFinished=False
            while not votingFinished:
                response=socketReadLine(client)
                response=response.split(' ')
                if response[-1] == 'selected':
                    votingFinished=True
                    selected=response[-3] + ' ' + response[-2]
                elif response[0] == 'success' and response[1] == 'vote':
                    votes[response[3] + ' ' + response[4]] += 1
                elif response[-1] == 'not_selected':
                    votes={}
                elif response[0] == 'error':
                    errorText='You didn’t send a vote for selection'

            response=socketReadLine(client)
            if response.split(' ')[3] == 'needed':
                highlightMoveTargets(selected)
                allowedVerbs = {'exit', 'move'}
                hintText='Type `move {target}` (e.g. move A2) to move vote for the move'
            votes={}
            votingFinished=False
            while not votingFinished:
                response=socketReadLine(client)
                response=response.split(' ')
                if response[3] == 'moved':
                    votingFinished=True
                    target=response[5] + ' ' + response[6]
                elif response[0] == 'success' and response[1] == 'vote':
                    votes[response[3] + ' ' + response[4]] += 1
                elif response[-1] == 'not_moved':
                    votes={}
                elif response[0] == 'error':
                    errorText='You didn’t send a vote for move'
            moveStone(selected, target)
        elif response.split(' ')[1] == 'active':
            errorText='Waiting for opponent’s move'
            # todo receive move result or gameEnded
            # todo move stone
        elif response.split(' ')[1] == 'game':
            errorText='{} team won by {}.'.format(response.split(' ')[3].capitalize(), {'corner': 'corner reaching',
                        'no_stones': 'opponent capturing', 'no_vote': 'opponent disconnection'}[response.split(' ')[4]])
            allowedVerbs = {'exit', 'create', 'join'}
            hintText='Type `exit` to quit, `create {n}` to create a game for n players per team  or `join` to join an existing game'


def highlightSelectables(rolledValue):
    teamModifier = 0 if myTeam == 'yellow' else 10
    rolledValue += teamModifier
    stones = [0, 0, 0, 0, 0, 0]
    i = 0
    for row in board:
        j = 0
        for field in row:
            if int(field / 10) * 10 == teamModifier:
                stones[field % 10] = (i, j)
            j+=1
        i+=1

    lower = ''
    for stone in stones[:rolledValue]:
        if stone != 0:
            lower = stone
    upper = ''
    for stone in stones[rolledValue+1:]:
        if stone != 0:
            upper = stone
            break
    
    i,j = lower
    board[i][j] += 100
    i,j = upper
    board[i][j] += 100
    drawBoard()


def highlightMoveTargets(selected):
    pass


def moveStone(source, target):
    source = source.split(' ')
    target = target.split(' ')
    board[target[0]][target[1]] = board[source[0]][source[1]]
    board[source[0]][source[1]] = 0
    drawBoard()


def parseVotes():
    votesString = ''
    for pos, vote in votes.items():
        votesString += translateToChessNotation(pos) + ': ' + vote + ', '
    return votesString[:-2]


def translateToChessNotation(position):
    row = int(position.split(' ')[0])
    column = int(position.split(' ')[1])
    row += 1
    column += ord('A')
    column = chr(column)
    return '{}{}'.format(row, column)


def do(command):
    global errorText
    global allowedVerbs
    global server
    global myTeam
    verb=command.split(' ')[0]
    if verb not in allowedVerbs:
        errorText = verb + ' not allowed now'
        return
    if verb == 'exit':
        curses.endwin()
        #todo interrupt runGame thread
        return True
    elif verb == 'connect':
        try:
            address=command.split(' ')[1]
            port=command.split(' ')[2]
        except IndexError:
            errorText='Syntax error in {}'.format(command)
            return
        try:
            client.connect((address, int(port)))
        except Exception as e:
            errorText=str(e)
        else:
            errorText='Connected to game. Type `create {n}` to create a game for n players per team, or `join` to join an existing game'
            server=address + ':' + port
            allowedVerbs = {'exit', 'create', 'join'}
    elif verb == 'create':
        try:
            number=command.split(' ')[1]
        except IndexError:
            errorText='Syntax error in {}'.format(command)
            return
        socketPrintLine(client, command)
        response=socketReadLine(client)
        if response.split(' ')[0] == 'error':
            errorText=errorMessages[response.split(' ')[2]]
        else:
            errorText='Successfully created game for {}'.format(number)
            response=socketReadLine(client)
            errorText='Successfully joined {} team. Waiting for all players'.format(
                response.split(' ')[2])
            myTeam=response.split(' ')[2]

            waitForBoard()
            startGame()

    elif verb == 'join':
        socketPrintLine(client, command)
        response=socketReadLine(client)
        if response.split(' ')[0] == 'error':
            errorText=errorMessages[response.split(' ')[2]]
        else:
            errorText='Successfully joined {} team'.format(
                response.split(' ')[2])
            myTeam=response.split(' ')[2]

            waitForBoard()
            startGame()

    elif verb == 'select':
        socketPrintLine(client, 'vote stone ' + \
                        findStonePosition(command.split(' ')[1]))

    elif verb == 'move':
        socketPrintLine(client, 'vote move' + \
                        translateChessNotation(command.split(' ')[1]))

    else:
        errorText='No such command {}'.format(command)


def findStonePosition(stoneNumber):
    teamModifier = 0 if myTeam == 'yellow' else 10
    stoneNumber = stoneNumber + teamModifier
    i, j = 0, 0
    for row in board:
        j = 0
        for field in row:
            if field % 100 == stoneNumber:
                return '{} {}'.format(i, j)
            j += 1
        i += 1
    return None


def translateChessNotation(chessField):
    chessField = chessField.upper()
    if(chessField[0] >= 'A' and chessField <= 'E'):
        column = chessField[0]
        row = chessField[1]
    else:
        column = chessField[1]
        row = chessField[0]
    row = int(row)-1
    column = ord(column) - ord('A')
    return '{} {}'.format(row, column)


def inputFunction():
    global command
    global gameEnded
    while not gameEnded:
        c=stdscr.getch()
        if c == 10:
            textBox.clear()
            gameEnded=do(command)
            command=''
            textBox.move(0, 0)
            textBox.refresh()
        elif c == 127 or c == 8:
            textBox.clear()
            command=command[:-1]
            textBox.move(0, 0)
            textBox.addstr(command)
            textBox.refresh()
        else:
            command += chr(c)
            textBox.addstr(chr(c))
            textBox.refresh()


def statusFunction():
    global gameEnded
    while not gameEnded:
        statusBox.clear()
        statusBox.move(0, 0)
        statusBox.addstr(hintText + '\n')
        statusBox.addstr(errorText + '\n')
        statusBox.addstr('Connected to ' + server if server !=
                         '' else 'Not connected')
        statusBox.addstr('; Team ' + myTeam if myTeam != '' else '')
        statusBox.addstr('; Rolled {}'.format(roll) if roll != -1 else '')
        statusBox.addstr('; Votes: ' + parseVotes() if votes != {} else '')
        textBox.move(0, len(command))
        statusBox.refresh()
        textBox.refresh()
        time.sleep(.5)

allowedVerbs = {'connect', 'exit'}
statusText='Not connected.'
hintText='Type `connect {address} {port}` to connect'

inputThread=threading.Thread(target=inputFunction)
inputThread.start()
statusThread=threading.Thread(target=statusFunction)
statusThread.start()
