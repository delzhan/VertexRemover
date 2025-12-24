package com.cgvsu;

import com.cgvsu.model.Model;
import com.cgvsu.model.VertexRemover;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.util.FileCompareObj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.ROOT);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Программа удаления вершин из 3D модели ===");

        Path dataDir = Path.of("").toAbsolutePath().resolve("data");
        System.out.println("\nДоступные модели в папке 'data':");
        try (var files = Files.list(dataDir)) {
            files.filter(p -> p.toString().endsWith(".obj"))
                    .map(p -> p.getFileName().toString().replace(".obj", ""))
                    .forEach(System.out::println);
        }

        System.out.print("\nВведите имя модели (без .obj) [по умолчанию WrapSkull]: ");
        String modelName = scanner.nextLine().trim();
        if (modelName.isEmpty()) {
            modelName = "WrapSkull";
        }

        Path inputFile = dataDir.resolve(modelName + ".obj");
        if (!Files.exists(inputFile)) {
            System.err.println("Файл не найден: " + inputFile);
            return;
        }

        System.out.println("\nЗагрузка модели из: " + inputFile);
        String fileContent = Files.readString(inputFile);
        Model model = ObjReader.read(fileContent);

        System.out.println("\nХарактеристики модели ДО удаления:");
        System.out.println("  • Вершин: " + model.getVertices().size());
        System.out.println("  • Текстурных координат: " + model.getTextureVertices().size());
        System.out.println("  • Нормалей: " + model.getNormals().size());
        System.out.println("  • Полигонов: " + model.getPolygons().size());

        System.out.println("\n" + "=".repeat(50));
        System.out.println("УДАЛЕНИЕ ВЕРШИН");
        System.out.println("=".repeat(50));
        System.out.println("Доступные номера вершин: 1 - " + model.getVertices().size());
        System.out.print("Введите номера вершин для удаления (через пробел или запятую): ");

        String input = scanner.nextLine().trim();
        List<Integer> verticesToDelete = parseVertexIndices(input, model.getVertices().size());

        if (verticesToDelete.isEmpty()) {
            System.out.println("Не указаны вершины для удаления. Завершение работы.");
            return;
        }

        System.out.println("Будут удалены вершины: " + verticesToDelete);

        System.out.println("\nВыполняется удаление вершин и связанных полигонов...");
        VertexRemover.removeVertices(model, verticesToDelete);
        System.out.println("Готово!");

        System.out.println("\nХарактеристики модели ПОСЛЕ удаления:");
        System.out.println("  • Вершин: " + model.getVertices().size());
        System.out.println("  • Нормалей: " + model.getNormals().size());
        System.out.println("  • Полигонов: " + model.getPolygons().size());

        System.out.println("\n" + "=".repeat(50));
        System.out.println("СОХРАНЕНИЕ РЕЗУЛЬТАТА");
        System.out.println("=".repeat(50));

        Path outputFile = inputFile.resolveSibling(modelName + "_modified.obj");
        System.out.println("Сохранение измененной модели в: " + outputFile);
        ObjWriter.write(model, outputFile.toString());

        System.out.println("\n" + "=".repeat(50));
        System.out.println("СРАВНЕНИЕ ФАЙЛОВ");
        System.out.println("=".repeat(50));

        FileCompareObj comparator = new FileCompareObj(inputFile, outputFile);
        System.out.println("\n=== Сводка различий ===");
        comparator.printDifferenceSummary();

        System.out.println("\n=== Проверка идентичности ===");
        boolean identical = comparator.areFilesIdentical();
        System.out.println("Файлы полностью идентичны: " + (identical ? "ДА" : "НЕТ"));

        if (!identical) {
            System.out.println("\n? Изменения успешно применены! Модель сохранена в отдельный файл.");
            System.out.println("  Исходный файл: " + inputFile.getFileName());
            System.out.println("  Измененный файл: " + outputFile.getFileName());
        }

        scanner.close();
    }

    /**
     * Парсит строку с номерами вершин, проверяет корректность
     */
    private static List<Integer> parseVertexIndices(String input, int maxVertexIndex) {
        List<Integer> indices = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) {
            return indices;
        }

        String[] parts = input.split("[,\\s]+");
        for (String part : parts) {
            try {
                int index = Integer.parseInt(part.trim());
                if (index >= 1 && index <= maxVertexIndex) {
                    if (!indices.contains(index)) {
                        indices.add(index);
                    }
                } else {
                    System.err.println("Предупреждение: номер вершины " + index +
                            " вне диапазона (1-" + maxVertexIndex + "), пропускаем.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Предупреждение: '" + part + "' не является числом, пропускаем.");
            }
        }

        indices.sort(Collections.reverseOrder());
        return indices;
    }
}
