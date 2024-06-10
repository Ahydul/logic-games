from bs4 import BeautifulSoup
import urllib.request
import json

URL_BASE = "https://www.janko.at/Raetsel/Hakyuu/"
FILE_NAME = "datosDificultad.txt"


def scrap_difficulty():
    f = urllib.request.urlopen(URL_BASE)
    soup = BeautifulSoup(f, "lxml")

    index = soup.find("div", id="index-3")
    json_all_data = []
    json_all_data.append("Dificultad: tableros")

    for index_per_difficulty in index.find_all("tr")[1:]:
        i = index_per_difficulty.find_all("td")
        difficulty = i[0].string
        links = i[1].find_all("a")

        boards = []
        for link in links:
            name = link.string.strip()
            boards.append(name)

        json_data = difficulty + ":" + ", ".join(sorted(boards))

        json_all_data.append(json_data[:-2])
    with open(FILE_NAME, "w") as output:
        output.write("\n".join(json_all_data))


def scrap():
    f = urllib.request.urlopen(URL_BASE)
    soup = BeautifulSoup(f, "lxml")

    index = soup.find("div", id="index-2")

    json_all_data = []

    for index_per_size in index.find_all("tr"):
        i = index_per_size.find_all("td")
        size = i[0].string.split()
        if "SIZE: " + str(size) == "SIZE: ['18x10']":
            break
        links = i[1].find_all("a")

        json_data = {"size": size, "games": []}

        for link in links:
            name = link.string.strip()
            f2 = urllib.request.urlopen(URL_BASE + link["href"])
            soup2 = BeautifulSoup(f2, "lxml")
            data = soup2.find("script", id="data").string.strip()
            game = "Tablero " + name + ":" +  data + " " + (URL_BASE + link["href"])
            json_data["games"].append(game)

        json_all_data.append(json_data)

    with open(FILE_NAME, "w") as output:
        json.dump(json_all_data, output)

