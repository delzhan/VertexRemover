package com.cgvsu.model;

import com.cgvsu.math.Vector3f;
import java.util.*;

public class VertexRemover {
    /**
     * ”дал€ет указанные вершины и все полигоны, которые их содержат.
     */
    public static void removeVertices(Model model, List<Integer> vertexIndicesToDelete) {
        if (model == null || vertexIndicesToDelete == null || vertexIndicesToDelete.isEmpty()) {
            return;
        }

        // ”даление дубликатов
        List<Integer> uniqueIndices = new ArrayList<>(new LinkedHashSet<>(vertexIndicesToDelete));

        // Ќаходим и удал€ем полигоны, содержащие удал€емые вершины
        List<Polygon> polygonsToRemove = new ArrayList<>();
        for (Polygon polygon : model.getPolygons()) {
            for (int vertexIndex : polygon.getVertexIndices()) {
                if (uniqueIndices.contains(vertexIndex)) {
                    polygonsToRemove.add(polygon);
                    break; // Ётот полигон помечен, переходим к следующему
                }
            }
        }
        model.getPolygons().removeAll(polygonsToRemove);

        // ”дал€ем сами вершины
        List<Integer> sortedIndices = new ArrayList<>(uniqueIndices);
        sortedIndices.sort(Collections.reverseOrder());
        for (int objIndex : sortedIndices) {
            int internalIndex = objIndex - 1;
            if (internalIndex >= 0 && internalIndex < model.getVertices().size()) {
                model.getVertices().remove(internalIndex);
            }
        }

        reindexPolygons(model, uniqueIndices);
        recalculateNormals(model);
    }

    /**
     * ѕересчитывает нормали модели, усредн€€ нормали граней дл€ каждой вершины.
     */
    public static void recalculateNormals(Model model) {
        // ќчищаем старые нормали
        model.getNormals().clear();

        // ¬ременный список дл€ хранени€ суммы нормалей дл€ каждой вершины
        List<Vector3f> vertexNormalSums = new ArrayList<>();
        for (int i = 0; i < model.getVertices().size(); i++) {
            vertexNormalSums.add(new Vector3f(0, 0, 0));
        }

        // ¬ычисл€ем нормаль дл€ каждого полигона и добавл€ем еЄ к его вершинам
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue; // ѕропускаем некорректные полигоны

            // ѕолучаем вершины полигона
            Vector3f v0 = model.getVertices().get(vertexIndices.get(0) - 1);
            Vector3f v1 = model.getVertices().get(vertexIndices.get(1) - 1);
            Vector3f v2 = model.getVertices().get(vertexIndices.get(2) - 1);

            // ¬ычисл€ем векторы сторон и нормаль полигона
            Vector3f edge1 = v1.subtract(v0);
            Vector3f edge2 = v2.subtract(v0);
            Vector3f faceNormal = edge1.cross(edge2).normalize();

            // ƒобавл€ем эту нормаль ко всем вершинам полигона
            for (int vertexObjIndex : vertexIndices) {
                int internalIndex = vertexObjIndex - 1;
                Vector3f currentSum = vertexNormalSums.get(internalIndex);
                vertexNormalSums.set(internalIndex, currentSum.add(faceNormal));
            }
        }

        // Ќормализуем суммы, чтобы получить итоговые нормали вершин, и добавл€ем в модель
        for (Vector3f sum : vertexNormalSums) {
            model.getNormals().add(sum.normalize());
        }

        // ќбновл€ем индексы нормалей в полигонах (теперь они 1:1 с вершинами)
        for (Polygon polygon : model.getPolygons()) {
            polygon.setNormalIndices(new ArrayList<>(polygon.getVertexIndices()));
        }
    }

    /**
     * ¬спомогательный метод дл€ переиндексации полигонов после удалени€ вершин.
     */
    private static void reindexPolygons(Model model, List<Integer> deletedObjIndices) {
        for (Polygon polygon : model.getPolygons()) {
            // ѕереиндексируем вершины
            ArrayList<Integer> newVertexIndices = new ArrayList<>();
            for (int oldObjIndex : polygon.getVertexIndices()) {
                int shift = 0;
                for (int deletedIndex : deletedObjIndices) {
                    if (oldObjIndex > deletedIndex) {
                        shift++;
                    }
                }
                newVertexIndices.add(oldObjIndex - shift);
            }
            polygon.setVertexIndices(newVertexIndices);

            // ѕереиндексируем нормали (если они были)
            if (polygon.getNormalIndices() != null && !polygon.getNormalIndices().isEmpty()) {
                ArrayList<Integer> newNormalIndices = new ArrayList<>();
                for (int oldObjIndex : polygon.getNormalIndices()) {
                    int shift = 0;
                    for (int deletedIndex : deletedObjIndices) {
                        if (oldObjIndex > deletedIndex) {
                            shift++;
                        }
                    }
                    newNormalIndices.add(oldObjIndex - shift);
                }
                polygon.setNormalIndices(newNormalIndices);
            }
        }
    }
}
