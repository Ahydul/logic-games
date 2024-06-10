from bs4 import BeautifulSoup
import json
import io
import re


def difficultyProcess():
    with open("test.html", "r") as file:
        html_content = file.read()

    res = []
    res.append("Board, Size, Difficulty, Score")

    soup = BeautifulSoup(html_content, "html.parser")
    li_elements = soup.find_all('li', class_=['level test expandable', 'level test expandable lastExpandable'])
    for li in li_elements:
        span = li.find("span")
        for child in span.find_all():
            child.extract()
        remaining_text = span.get_text(strip=True)
        index = remaining_text.find("] ") + 2
        index2 = remaining_text.find(",")
        size = remaining_text[index:index2]
        print("Size " + size)
        
        with open(size + "-startboard.txt", "r") as file:
            data = file.read()

        board_numbers = re.findall(r'Tablero (\d+)', data)

        with open("datosDificultad.txt", "r") as file:
            data = file.read().split("\n")[1:]

        difficultyPerBoard = {}
        for d in data:
            spl = d.split(":")
            for d2 in spl[1].split(", "):
                difficultyPerBoard[d2] = spl[0]

        table = li.select_one('table[style="border-spacing: 20px 0;justify-content: start;display: flex;"]')
        trs = table.find_all('tr', limit=200)[1:] #Skip first
        print(len(trs))

        for boardIndex, tr in enumerate(trs):
            score = tr.find_all('td')[4].string
            board = board_numbers[boardIndex]
            difficulty = difficultyPerBoard.get(board)
            if difficulty is not None:
                res.append(board + ", " + size + ", " + difficulty + ", " + score)
            else:
                res.append(board + ", " + size + ", " + "" + ", " + score)

    with open("boardScores.csv", "w") as file:
        file.write("\n".join(res))


class Coordinate:
    def __init__(self, row, column):
        self.row = int(row)
        self.column = int(column)

    def __str__(self):
        return f"({self.row},{self.column})"

def process3():
    FILE = "datos3.json"
    with open(FILE, "r") as file:
        data = json.load(file)[2]["games"][59]
    print(data)

def process2():
    FILE = "17-areas.txt"
    with open(FILE, "r") as file:
        data = file.read()

    output = ""

    for line in data.split("\n"):
        if len(line) == 0:
            break
        if "Tablero" in line:
            output += line + "\n"
        else:
            spl1 = line.split(":")
            output += spl1[0] + ":["
            spl2 = (
                spl1[1]
                .replace("(", "")
                .replace(")", "")
                .replace("[", "")
                .replace("]", "")
                .split(", ")
            )
            for v in spl2:
                spl3 = v.split(",")
                output += "(" + spl3[1] + "," + spl3[0] + ")" + ", "
            output = output[:-2] + "]\n"

    with open(FILE, "w") as file:
        file.write(output)


FILE_NAME = "datos4.json"
OUTPUT_AREAS = "areas.txt"
OUTPUT_START_BOARDS = "startboard.txt"
OUTPUT_SOLUTIONS = "solution.txt"

def process_solution(data, size):
    solutions_array = []
    board_difficulty = []
    for s in data:
        solution_str = "[solution]"
        index_solution = s.find(solution_str)
        index_moves = s.find("[moves]")

        solution = s[index_solution + len(solution_str) : index_moves].strip()
        solutions_array.append(solution)

        difficulty = s[len("Tablero ") : s.find(":")]
        board_difficulty.append(difficulty)

    output_string = io.StringIO()
    for index, solution in enumerate(solutions_array):
        output_string.write("Tablero " + board_difficulty[index] + "\n")
        output_string.write(solution+"\n")

    output_result = output_string.getvalue()
    output_string.close()
    print(output_result)

    with open(str(size) + "-" + OUTPUT_SOLUTIONS, "w") as file:
        file.write(output_result)


def process_start_board(data, size):
    start_board_array = []
    board_difficulty = []
    for s in data:
        problem_str = "[problem]"
        index_problem = s.find(problem_str)
        index_areas = s.find("[areas]")

        startboard_data = s[index_problem + len(problem_str) : index_areas].strip()
        start_board_array.append(startboard_data)

        difficulty = s[len("Tablero ") : s.find(":")]
        board_difficulty.append(difficulty)

    output_string = io.StringIO()
    for index, start_board in enumerate(start_board_array):
        output_string.write("Tablero " + board_difficulty[index] + "\n")
        output_string.write(start_board+"\n")

    output_result = output_string.getvalue()
    output_string.close()
    print(output_result)

    with open(str(size) + "-" + OUTPUT_START_BOARDS, "w") as file:
        file.write(output_result)



def process_areas(data, size):
    areas_array = []
    board_difficulty = []
    for s in data:
        areas_str = "[areas]"
        index_areas = s.find(areas_str)
        solution_str = "[solution]"
        index_solution = s.find(solution_str)

        areas_data = s[index_areas + len(areas_str) : index_solution].strip()

        rows = areas_data.split("\n")
        areas = {}
        for row, sr in enumerate(rows):
            cols = sr.split()
            for column, area_id in enumerate(cols):
                coordinate = Coordinate(row=row, column=column)

                if area_id in areas:
                    areas[area_id].append(coordinate)
                else:
                    areas[area_id] = [coordinate]

        sorted_areas = dict(sorted(areas.items(), key=lambda x: len(x[1])))
        areas_array.append(sorted_areas)

        difficulty = s[len("Tablero ") : s.find(":")]
        board_difficulty.append(difficulty)

    distributions = {}
    output_string = io.StringIO()
    for index, sorted_areas in enumerate(areas_array):
        output_string.write("Tablero " + board_difficulty[index] + "\n")

        if size == 6:
            a = size
        elif size == 12:
            a = size - 4
        elif size == 15:
            a = size - 3
        elif size == 17:
            a = size - 9
        else:
            a = size + 1
        dist = [0] * a

        for key, value in sorted_areas.items():
            coordinates_str = ", ".join(str(coord) for coord in value)
            output_string.write(f"{key}:[{coordinates_str}]\n")
            print(len(value) - 1)
            dist[len(value) - 1] += 1

        dist = ",".join(str(v) for v in dist)

        if dist in distributions:
            distributions[dist] += 1
        else:
            distributions[dist] = 1

    output_result = output_string.getvalue()
    output_string.close()
    print(output_result)
    with open(str(size) + "-" + OUTPUT_AREAS, "w") as file:
        file.write(output_result)

    for distribution, number in distributions.items():
        print(f"{distribution}: {number}")


def process_all():
    with open(FILE_NAME, "r") as file:
        data = json.load(file)

    i = 0
    while(i <= 7 ):
    #while(i <= 0):
        data2 = data[i]["games"]
        size = int(data[i]["size"][0].split("x")[0])

        process_areas(data2,size)
        process_start_board(data2, size)
        process_solution(data2, size)

        i += 1
