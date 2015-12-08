package com.codenjoy.dojo.snake.client;

import java.util.LinkedList;

/**
 * Created by woland on 12/7/15.
 */
public class Astar{
    public static int WIDTH = 10;
    public static int HEIGHT = 10;

    /**
     * Пример хуевой реализации алгоритма поиска пути А*
     * @param args нихуя
     */
    public static void main(String[] args) {
        // Создадим все нужные списки
        Table<Cell> cellList = new Table<Cell>(WIDTH, HEIGHT);
        Table blockList = new Table(WIDTH, HEIGHT);
        LinkedList<Cell> openList = new LinkedList<Cell>();
        LinkedList<Cell> closedList = new LinkedList<Cell>();
        LinkedList<Cell> tmpList = new LinkedList<Cell>();

        // Создадим преграду

        blockList.add(new Cell(0, 6, true));

        blockList.add(new Cell(1, 1, true));
        blockList.add(new Cell(1, 3, true));
        blockList.add(new Cell(1, 4, true));
        blockList.add(new Cell(1, 7, true));

        blockList.add(new Cell(2, 2, true));
        blockList.add(new Cell(2, 6, true));

        blockList.add(new Cell(3, 3, true));

        blockList.add(new Cell(4, 4, true));
        blockList.add(new Cell(4, 5, true));
        blockList.add(new Cell(4, 7, true));

        blockList.add(new Cell(5, 2, true));
        blockList.add(new Cell(5, 7, true));

        blockList.add(new Cell(6, 2, true));
        blockList.add(new Cell(6, 3, true));
        blockList.add(new Cell(6, 4, true));
        blockList.add(new Cell(6, 5, true));
        blockList.add(new Cell(6, 6, true));
        blockList.add(new Cell(6, 7, true));
        blockList.add(new Cell(6, 8, true));

        blockList.add(new Cell(7, 3, true));
        blockList.add(new Cell(7, 4, true));
        blockList.add(new Cell(7, 8, true));

        blockList.add(new Cell(8, 4, true));
        blockList.add(new Cell(8, 6, true));


        // Заполним карту как-то клетками, учитывая преграду
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                cellList.add(new Cell(i, j, blockList.get(i, j).blocked));
            }
        }

        // Стартовая и конечная
        cellList.get(8, 3).setAsStart();
        cellList.get(8, 2).setAsFinish();
        Cell start = cellList.get(8, 3);
        Cell finish = cellList.get(8, 2);

        cellList.printp();

        // Фух, начинаем
        boolean found = false;
        boolean noroute = false;

        //1) Добавляем стартовую клетку в открытый список.
        openList.push(start);

        //2) Повторяем следующее:
        while (!found && !noroute) {
            //a) Ищем в открытом списке клетку с наименьшей стоимостью F. Делаем ее текущей клеткой.
            Cell min = openList.getFirst();
            for (Cell cell : openList) {
                // тут я специально тестировал, при < или <= выбираются разные пути,
                // но суммарная стоимость G у них совершенно одинакова. Забавно, но так и должно быть.
                if (cell.F < min.F) min = cell;
            }

            //b) Помещаем ее в закрытый список. (И удаляем с открытого)
            closedList.push(min);
            openList.remove(min);
            //System.out.println(openList);

            //c) Для каждой из соседних 4//8-ми клеток ...
            tmpList.clear();
            //tmpList.add(cellList.get(min.x - 1, min.y - 1));
            tmpList.add(cellList.get(min.x,     min.y - 1));
            //tmpList.add(cellList.get(min.x + 1, min.y - 1));
            tmpList.add(cellList.get(min.x + 1, min.y));
            //tmpList.add(cellList.get(min.x + 1, min.y + 1));
            tmpList.add(cellList.get(min.x,     min.y + 1));
            //tmpList.add(cellList.get(min.x - 1, min.y + 1));
            tmpList.add(cellList.get(min.x - 1, min.y));

            for (Cell neightbour : tmpList) {
                //Если клетка непроходимая или она находится в закрытом списке, игнорируем ее. В противном случае делаем следующее.
                if (neightbour.blocked || closedList.contains(neightbour)) continue;

                //Если клетка еще не в открытом списке, то добавляем ее туда. Делаем текущую клетку родительской для это клетки. Расчитываем стоимости F, G и H клетки.
                if (!openList.contains(neightbour)) {
                    openList.add(neightbour);
                    neightbour.parent = min;
                    neightbour.H = neightbour.mandist(finish);
                    neightbour.G = start.price(min);
                    neightbour.F = neightbour.H + neightbour.G;
                    continue;
                }

                // Если клетка уже в открытом списке, то проверяем, не дешевле ли будет путь через эту клетку. Для сравнения используем стоимость G.
                if (neightbour.G + neightbour.price(min) < min.G) {
                    // Более низкая стоимость G указывает на то, что путь будет дешевле. Эсли это так, то меняем родителя клетки на текущую клетку и пересчитываем для нее стоимости G и F.
                    neightbour.parent = min; // вот тут я честно хз, надо ли min.parent или нет
                    neightbour.H = neightbour.mandist(finish);
                    neightbour.G = start.price(min);
                    neightbour.F = neightbour.H + neightbour.G;
                }

                // Если вы сортируете открытый список по стоимости F, то вам надо отсортировать свесь список в соответствии с изменениями.
            }

            //d) Останавливаемся если:
            //Добавили целевую клетку в открытый список, в этом случае путь найден.
            //Или открытый список пуст и мы не дошли до целевой клетки. В этом случае путь отсутствует.

            if (openList.contains(finish)) {
                found = true;
            }

            if (openList.isEmpty()) {
                noroute = true;
            }
        }

        // first step cell DEFAULT
        //int firstStepX = -1;
        //int firstStepY = -1;
        int firstStepX = finish.getX();
        int firstStepY = finish.getY();


        //3) Сохраняем путь. Двигаясь назад от целевой точки, проходя от каждой точки к ее родителю до тех пор, пока не дойдем до стартовой точки. Это и будет наш путь.
        if (!noroute) {
            Cell rd = finish.parent;
            while (!rd.equals(start)) {
                rd.road = true;

                firstStepX = rd.getX();
                firstStepY = rd.getY();
                System.out.println("[" + firstStepX + "," + firstStepY + "]");

                rd = rd.parent;
                if (rd == null) break;
            }
            cellList.printp();
        } else {
            System.out.println("NO ROUTE");
        }

        System.out.println("start.getX(): " + start.getX() + "; start.getY(): " + start.getY() + "; firstStepX: " + firstStepX + "; firstStepY: " + firstStepY);


    }
}
