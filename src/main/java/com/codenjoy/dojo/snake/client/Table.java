package com.codenjoy.dojo.snake.client;

/**
 * Created by woland on 12/7/15.
 */
public class Table<T extends Cell> {
    /**
     * Создаем карту игры с размерами width и height
     */
    public Table(int width, int height) {
        this.width = width;
        this.height = height;
        this.table = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                table[i][j] = new Cell(0, 0, false);
            }
        }
    }

    /**
     * Добавить клетку на карту
     */
    public void add(Cell cell) {
        table[cell.x][cell.y] = cell;
    }

    /**
     * Получить клетку по координатам x, y
     * @return клетка, либо фейковая клетка, которая всегда блокирована (чтобы избежать выхода за границы)
     */
    @SuppressWarnings("unchecked")
    public T get(int x, int y) {
        if (x < width && x >= 0 && y < height && y >= 0) {
            return (T)table[x][y];
        }
        // а разве так можно делать в Java? оО но работает оО
        return (T)(new Cell(0, 0, true));
    }

    /**
     * Печать всех клеток поля. Красиво.
     */
    public void printp() {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                System.out.print(this.get(i, j));
            }
            System.out.println();
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    public int width;
    public int height;
    private Cell[][] table;

}
