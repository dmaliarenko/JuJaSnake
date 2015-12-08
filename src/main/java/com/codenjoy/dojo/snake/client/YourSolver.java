package com.codenjoy.dojo.snake.client;


import com.codenjoy.dojo.client.Direction;
import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.Point;
import com.codenjoy.dojo.services.RandomDice;
import com.codenjoy.dojo.snake.model.Elements;

import java.util.LinkedList;

/**
 * User: your name
 */
public class YourSolver implements Solver<Board> {

    private static final String USER_NAME = "dmytro.maliarenko@gmail.com";

    private Dice dice;
    private Board board;

    public YourSolver(Dice dice) {
        this.dice = dice;
    }

    @Override
    public String get(Board board) {
        this.board = board;

//        Point point = board.getApples().get(0);
//        point.getX()
//        point.getY()

        char[][] field = board.getField();

        // Создадим все нужные списки
        Table<Cell> cellList = new Table<Cell>(field.length, field.length);
        Table blockList = new Table(field.length, field.length);
        LinkedList<Cell> openList = new LinkedList<Cell>();
        LinkedList<Cell> closedList = new LinkedList<Cell>();
        LinkedList<Cell> tmpList = new LinkedList<Cell>();

        // Стартовая и конечная дефолтные [может и не надо]
        Cell start = cellList.get(0, 0);
        Cell finish = cellList.get(field.length - 1, field.length - 1);


        // Заполним карту как-то клетками, учитывая преграду
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field.length; j++) {
                //добавляем клетку, и если это не свободная клетка и не яблоко, то блокируем ее
                cellList.add(new Cell(i, j, !board.isAt(i, j, Elements.GOOD_APPLE, Elements.NONE)));

                //если клетка распознана как голова, то делаем эту клетку стартовой
                if (board.isAt(i, j, Elements.HEAD_DOWN, Elements.HEAD_UP, Elements.HEAD_LEFT, Elements.HEAD_RIGHT)) {
                    cellList.get(i, j).setAsStart();
                    start = cellList.get(i, j);
                } else if (board.isAt(i, j, Elements.GOOD_APPLE)) {
                    //если клетка распознана как яблоко, то делаем эту клетку финишной
                    cellList.get(i, j).setAsFinish();
                    finish = cellList.get(i, j);

                }
            }
        }

        //cellList.printp();

        // Фух, начинаем
        boolean found = false;
        boolean noroute = false;

        //1) Добавляем стартовую клетку в открытый список.^)
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

            //c) Для каждой из соседних 8-ми клеток ...
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

                System.out.println("StepX: " + firstStepX + "; StepY: " + firstStepY);

                rd = rd.parent;
                if (rd == null) break;
            }
            //cellList.printp();
        } else {
            System.out.println("NO ROUTE");
        }

        //System.out.println("start.getX(): " + start.getX() + "; start.getY(): " + start.getY() + "; firstStepX: " + firstStepX + "; firstStepY: " + firstStepY);
        int dx = start.getX() - firstStepX;
        int dy = start.getY() - firstStepY;

        if (dx < 0 && board.isAt(start.getX() + 1, start.getY(), Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.RIGHT.toString();
        }
        if (dx > 0 && board.isAt(start.getX() - 1, start.getY(), Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.LEFT.toString();
        }
        if (dy < 0 && board.isAt(start.getX(), start.getY() + 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.DOWN.toString();
        }
        if (dy > 0 && board.isAt(start.getX(), start.getY() - 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.UP.toString();
        }

        if (board.isAt(start.getX() + 1, start.getY(), Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.RIGHT.toString();
        }
        if (board.isAt(start.getX() - 1, start.getY(), Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.LEFT.toString();
        }
        if (board.isAt(start.getX(), start.getY() + 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.DOWN.toString();
        }
        if (board.isAt(start.getX(), start.getY() - 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.UP.toString();
        }

        return Direction.UP.toString();
    }






    public String getOLd(Board board) {
        this.board = board;

//        Point point = board.getApples().get(0);
//        point.getX()
//        point.getY()

        char[][] field = board.getField();

        // found snake
        int snakeHeadX = -1;
        int snakeHeadY = -1;
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field.length; y++) {
                char ch = field[x][y];
                if (ch == Elements.HEAD_DOWN.ch() ||
                    ch == Elements.HEAD_UP.ch() ||
                    ch == Elements.HEAD_LEFT.ch() ||
                    ch == Elements.HEAD_RIGHT.ch())
                {
                    snakeHeadX = x;
                    snakeHeadY = y;
                    break;

                }
            }
            if (snakeHeadX != -1) {
                break;
            }
        }

        // нашли змейку
        int appleX = -1;
        int appleY = -1;
        for (int x = 0; x < field.length; x++) {
            for (int y = 0; y < field.length; y++) {
                char ch = field[x][y];
                if (ch == Elements.GOOD_APPLE.ch()) {
                    appleX = x;
                    appleY = y;
                    break;

                }
            }
            if (appleX != -1) {
                break;
            }
        }

        int dx = snakeHeadX - appleX;
        int dy = snakeHeadY - appleY;

        if (dx < 0 && board.isAt(snakeHeadX + 1, snakeHeadY, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.RIGHT.toString();
        }
        if (dx > 0 && board.isAt(snakeHeadX - 1, snakeHeadY, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.LEFT.toString();
        }
        if (dy < 0 && board.isAt(snakeHeadX, snakeHeadY + 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.DOWN.toString();
        }
        if (dy > 0 && board.isAt(snakeHeadX, snakeHeadY - 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.UP.toString();
        }

        //если правильное направление невозможно, то пытаемся изменить положение
        if (board.isAt(snakeHeadX + 1, snakeHeadY, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.RIGHT.toString();
        }
        if (board.isAt(snakeHeadX - 1, snakeHeadY, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.LEFT.toString();
        }
        if (board.isAt(snakeHeadX, snakeHeadY + 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.DOWN.toString();
        }
        if (board.isAt(snakeHeadX, snakeHeadY - 1, Elements.GOOD_APPLE, Elements.NONE)) {
            return Direction.UP.toString();
        }

        return Direction.UP.toString();
    }

    public static void main(String[] args) {
        start(USER_NAME, WebSocketRunner.Host.REMOTE);
    }

    public static void start(String name, WebSocketRunner.Host server) {
        try {
            WebSocketRunner.run(server, name,
                    new YourSolver(new RandomDice()),
                    new Board());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
