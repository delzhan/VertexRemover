package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.*;

public class VertexRemover {

    /**
     * Удаляет указанные вершины и все полигоны, которые их содержат.
     * ВАЖНО: Индексы вершин для удаления передаются в OBJ-формате (начиная с 1).
     *
     * @param model                 модель для редактирования
     * @param vertexIndicesToDelete список индексов вершин для удаления (начинается с 1)
     */
    public static void removeVertices(Model model, List<Integer> vertexIndicesToDelete) {
        if (model == null || vertexIndicesToDelete == null || vertexIndicesToDelete.isEmpty()) {
            return;
        }

        // 1. Находим и удаляем полигоны, содержащие удаляемые вершины
        List<Polygon> polygonsToRemove = new ArrayList<>();
        for (Polygon polygon : model.getPolygons()) {
            for (int vertexIndex : polygon.getVertexIndices()) {
                if (vertexIndicesToDelete.contains(vertexIndex)) {
                    polygonsToRemove.add(polygon);
                    break; // Этот полигон помечен, переходим к следующему
                }
            }
        }
        model.getPolygons().removeAll(polygonsToRemove);

        // 2. Удаляем сами вершины (индексы должны быть отсортированы по убыванию)
        List<Integer> sortedIndices = new ArrayList<>(vertexIndicesToDelete);
        sortedIndices.sort(Collections.reverseOrder());
        for (int objIndex : sortedIndices) {
            int internalIndex = objIndex - 1; // Преобразуем в внутренний индекс (с 0)
            if (internalIndex >= 0 && internalIndex < model.getVertices().size()) {
                model.getVertices().remove(internalIndex);
            }
        }

        // 3. Переиндексируем оставшиеся полигоны
        reindexPolygons(model, vertexIndicesToDelete);

        // 4. Пересчитываем нормали для обновленной модели
        recalculateNormals(model);
    }

    /**
     * Пересчитывает нормали модели, усредняя нормали граней для каждой вершины.
     *
     * @param model модель для пересчета нормалей
     */
    public static void recalculateNormals(Model model) {
        // Очищаем старые нормали
        model.getNormals().clear();

        // Временный список для хранения суммы нормалей для каждой вершины
        List<Vector3f> vertexNormalSums = new ArrayList<>();
        for (int i = 0; i < model.getVertices().size(); i++) {
            vertexNormalSums.add(new Vector3f(0, 0, 0));
        }

        // Вычисляем нормаль для каждого полигона и добавляем её к его вершинам
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue; // Пропускаем некорректные полигоны

            // Получаем вершины полигона
            Vector3f v0 = model.getVertices().get(vertexIndices.get(0) - 1);
            Vector3f v1 = model.getVertices().get(vertexIndices.get(1) - 1);
            Vector3f v2 = model.getVertices().get(vertexIndices.get(2) - 1);

            // Вычисляем векторы сторон и нормаль полигона
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            Vector3f faceNormal = edge1.cross(edge2).normalize();

            // Добавляем эту нормаль ко всем вершинам полигона
            for (int vertexObjIndex : vertexIndices) {
                int internalIndex = vertexObjIndex - 1;
                Vector3f currentSum = vertexNormalSums.get(internalIndex);
                vertexNormalSums.set(internalIndex, currentSum.add(faceNormal));
            }
        }

        // Нормализуем суммы, чтобы получить итоговые нормали вершин, и добавляем в модель
        for (Vector3f sum : vertexNormalSums) {
            model.getNormals().add(sum.normalize());
        }

        // Обновляем индексы нормалей в полигонах (теперь они 1:1 с вершинами)
        for (Polygon polygon : model.getPolygons()) {
            polygon.setNormalIndices(new ArrayList<>(polygon.getVertexIndices()));
        }
    }

    /**
     * Вспомогательный метод для переиндексации полигонов после удаления вершин.
     */
    /**
     * Вспомогательный метод для переиндексации полигонов после удаления вершин.
     */
    private static void reindexPolygons(Model model, List<Integer> deletedObjIndices) {
        for (Polygon polygon : model.getPolygons()) {
            // Переиндексируем вершины
            ArrayList<Integer> newVertexIndices = new ArrayList<>();  // Изменено на ArrayList
            for (int oldObjIndex : polygon.getVertexIndices()) {
                int shift = 0;
                for (int deletedIndex : deletedObjIndices) {
                    if (oldObjIndex > deletedIndex) {
                        shift++;
                    }
                }
                newVertexIndices.add(oldObjIndex - shift);
            }
            polygon.setVertexIndices(newVertexIndices);  // Теперь тип совпадает

            // Переиндексируем нормали (если они были)
            if (polygon.getNormalIndices() != null && !polygon.getNormalIndices().isEmpty()) {
                ArrayList<Integer> newNormalIndices = new ArrayList<>();  // Изменено на ArrayList
                for (int oldObjIndex : polygon.getNormalIndices()) {
                    int shift = 0;
                    for (int deletedIndex : deletedObjIndices) {
                        if (oldObjIndex > deletedIndex) {
                            shift++;
                        }
                    }
                    newNormalIndices.add(oldObjIndex - shift);
                }
                polygon.setNormalIndices(newNormalIndices);  // Теперь тип совпадает
            }
            // Текстурные координаты можно обработать по аналогии при необходимости
        }
    }
}