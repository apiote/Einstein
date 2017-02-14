#!/usr/bin/env python

import curses
import socket
import threading
import time
import sys

stdscr = curses.initscr()

errorMessages = {'already_exists': 'Game already exists on server',
                 'invalid_count': 'Wrong number of players'}

statusText = ''
command = ''
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
curses.init_pair(3, 21, -1)
curses.init_pair(5, 208, 231)
curses.init_pair(7, 21, 231)
curses.init_pair(9, 208, 87)
curses.init_pair(11, 21, 87)


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


def waitForBoard():
    socketReadLine(client)
    response = socketReadLine(client)
    i = 0
    for point in response.split(' ')[2:]:
        board[int(i / 5)][int(i % 5)] = int(point)
        i += 1
    drawBoard()


def drawBoard():
    print('#drawing board', file=sys.stderr)
    boardBox.move(0, 0)
    boardBox.addstr('╭───┬───┬───┬───┬───╮\n')
    i = 0
    for row in board:
        for box in row:
            boardBox.addstr('│ ')
            boardBox.addstr(str(box % 10), curses.color_pair(
                2 * int(box / 10) + 1) if box != 0 else curses.color_pair(2))
            boardBox.addstr(' ')
        boardBox.addstr('│\n')
        print
        if(i < 4):
            boardBox.addstr('├───┼───┼───┼───┼───┤\n')
        i += 1
    boardBox.addstr('╰───┴───┴───┴───┴───╯\n')
    boardBox.refresh()


def do(command):
    global statusText
    verb = command.split(' ')[0]
    if verb == 'exit':
        curses.endwin()
        return True
    elif verb == 'connect':
        try:
            address = command.split(' ')[1]
            port = command.split(' ')[2]
        except IndexError:
            statusText = 'Syntax error in {}'.format(command)
            return
        try:
            client.connect((address, int(port)))
        except Exception as e:
            statusText = str(e)
        else:
            statusText = 'Connected to game. Type `create {n}` to create a game for n players per team, or `join` to join an existing game'
    elif verb == 'create':
        try:
            number = command.split(' ')[1]
        except IndexError:
            statusText = 'Syntax error in {}'.format(command)
            return
        socketPrintLine(client, command)
        response = socketReadLine(client)
        if response.split(' ')[0] == 'error':
            statusText = errorMessages[response.split(' ')[2]]
        else:
            statusText = 'Successfully created game for {}'.format(number)
            response = socketReadLine(client)
            statusText = 'Succesfully joined {} team. Waiting for all players'.format(
                response.split(' ')[2])

            waitForBoard()

    elif verb == 'join':
        socketPrintLine(client, command)
        response = socketReadLine(client)
        if response.split(' ')[0] == 'error':
            statusText = errorMessages[response.split(' ')[2]]
        else:
            statusText = 'Successfully joined {} team'.format(
                response.split(' ')[2])

            waitForBoard()


    else:
        statusText = 'No such command {}'.format(command)


def inputFunction():
    global command
    global gameEnded
    while not gameEnded:
        c = stdscr.getch()
        if c == 10:
            textBox.clear()
            gameEnded = do(command)
            command = ''
            textBox.move(0, 0)
            textBox.refresh()
        elif c == 127 or c == 8:
            textBox.clear()
            command = command[:-1]
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
        statusBox.move(1, 0)
        statusBox.addstr(statusText)
        textBox.move(0, len(command))
        statusBox.refresh()
        textBox.refresh()
        time.sleep(.5)

statusText = 'Not connected. Type `connect {address} {port}` to connect'

inputThread = threading.Thread(target=inputFunction)
inputThread.start()
statusThread = threading.Thread(target=statusFunction)
statusThread.start()
