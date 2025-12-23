package tests.com.cgvsu.vertexremover;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.VertexRemover;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VertexRemoverTest {

    private Model model;

    @BeforeEach
    void setUp() {
        // Создаем простую модель - треугольник для каждого теста
        model = new Model();

        // Вершины: треугольник
        model.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0),   // индекс 1 в OBJ
                new Vector3f(1, 0, 0),   // индекс 2
                new Vector3f(0, 1, 0),   // индекс 3
                new Vector3f(2, 2, 2)    // индекс 4 - изолированная вершина
        ));

        // Полигон (треугольник из первых трех вершин)
        Polygon triangle = new Polygon();
        triangle.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        model.getPolygons().add(triangle);

        // Нормали (будут пересчитаны автоматически)
    }

    @Test
    void testRemoveSingleVertexRemovesPolygon() {
        // Удаляем вершину 1, которая входит в треугольник
        List<Integer> verticesToDelete = Arrays.asList(1);

        // Запоминаем начальное состояние
        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        // Выполняем удаление
        VertexRemover.removeVertices(model, verticesToDelete);

        // Проверяем результаты
        assertEquals(initialVertexCount - 1, model.getVertices().size(),
                "Должна удалиться одна вершина");
        assertEquals(0, model.getPolygons().size(),
                "Все полигоны, содержащие удаленную вершину, должны быть удалены");
        assertEquals(3, model.getNormals().size(),
                "Нормали должны быть пересчитаны для оставшихся вершин");
    }

    @Test
    void testRemoveIsolatedVertexKeepsPolygons() {
        // Удаляем вершину 4, которая НЕ входит ни в один полигон
        List<Integer> verticesToDelete = Arrays.asList(4);

        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        VertexRemover.removeVertices(model, verticesToDelete);

        // Проверяем
        assertEquals(initialVertexCount - 1, model.getVertices().size(),
                "Изолированная вершина должна быть удалена");
        assertEquals(initialPolygonCount, model.getPolygons().size(),
                "Полигоны не должны быть затронуты");
        // Треугольник из вершин 1,2,3 должен остаться
        Polygon remainingPolygon = model.getPolygons().get(0);
        assertEquals(Arrays.asList(1, 2, 3), remainingPolygon.getVertexIndices(),
                "Индексы полигона не должны измениться при удалении изолированной вершины");
    }

    @Test
    void testRemoveMultipleVertices() {
        // Удаляем вершины 1 и 4
        List<Integer> verticesToDelete = Arrays.asList(1, 4);

        VertexRemover.removeVertices(model, verticesToDelete);

        // Ожидаем: вершина 1 удалена (и полигон тоже), вершина 4 удалена
        assertEquals(2, model.getVertices().size(), "Осталось 2 вершины");
        assertEquals(0, model.getPolygons().size(), "Нет полигонов");
    }

    @Test
    void testReindexingAfterVertexRemoval() {
        // Создаем более сложную модель с двумя полигонами
        Model complexModel = new Model();

        // 4 вершины квадрата
        complexModel.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0), // 1
                new Vector3f(1, 0, 0), // 2
                new Vector3f(1, 1, 0), // 3
                new Vector3f(0, 1, 0)  // 4
        ));

        // Два треугольника, составляющие квадрат
        Polygon tri1 = new Polygon();
        tri1.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        Polygon tri2 = new Polygon();
        tri2.setVertexIndices(new ArrayList<>(Arrays.asList(1, 3, 4)));

        complexModel.getPolygons().add(tri1);
        complexModel.getPolygons().add(tri2);

        // Удаляем вершину 2 (она только в первом треугольнике)
        VertexRemover.removeVertices(complexModel, Arrays.asList(2));

        // Проверяем переиндексацию
        assertEquals(3, complexModel.getVertices().size(), "Осталось 3 вершины");

        // Второй треугольник (1,3,4) должен стать (1,2,3) после переиндексации
        Polygon remainingPolygon = complexModel.getPolygons().get(0);
        assertEquals(Arrays.asList(1, 2, 3), remainingPolygon.getVertexIndices(),
                "Индексы должны быть пересчитаны после удаления вершины");
    }

    @Test
    void testEmptyVerticesListDoesNothing() {
        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        // Пытаемся удалить пустой список вершин
        VertexRemover.removeVertices(model, new ArrayList<>());

        // Ничего не должно измениться
        assertEquals(initialVertexCount, model.getVertices().size());
        assertEquals(initialPolygonCount, model.getPolygons().size());
    }

    @Test
    void testRemoveAllVertices() {
        // Удаляем все вершины модели
        List<Integer> allVertices = Arrays.asList(1, 2, 3, 4);

        VertexRemover.removeVertices(model, allVertices);

        // Все вершины и полигоны должны быть удалены
        assertEquals(0, model.getVertices().size(), "Не должно остаться вершин");
        assertEquals(0, model.getPolygons().size(), "Не должно остаться полигонов");
        assertEquals(0, model.getNormals().size(), "Не должно остаться нормалей");
    }

    @Test
    void testInvalidVertexIndexIsIgnored() {
        int initialVertexCount = model.getVertices().size();
        int initialPolygonCount = model.getPolygons().size();

        // Пытаемся удалить несуществующую вершину
        List<Integer> invalidVertices = Arrays.asList(999, 1); // 999 - не существует

        VertexRemover.removeVertices(model, invalidVertices);

        // Вершина 1 должна быть удалена, 999 проигнорирована
        assertEquals(initialVertexCount - 1, model.getVertices().size());
        assertEquals(0, model.getPolygons().size()); // Полигон с вершиной 1 удален
    }

    @Test
    void testTextureAndNormalIndicesReindexing() {
        // Создаем модель с текстурными координатами и нормалями
        Model texturedModel = new Model();

        texturedModel.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0), // 1
                new Vector3f(1, 0, 0), // 2
                new Vector3f(0, 1, 0)  // 3
        ));

        texturedModel.getNormals().addAll(Arrays.asList(
                new Vector3f(0, 0, 1), // нормаль 1
                new Vector3f(0, 0, 1), // нормаль 2
                new Vector3f(0, 0, 1)  // нормаль 3
        ));

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        poly.setNormalIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        texturedModel.getPolygons().add(poly);

        // Удаляем вершину 2
        VertexRemover.removeVertices(texturedModel, Arrays.asList(2));

        // Проверяем переиндексацию нормалей
        Polygon remainingPolygon = texturedModel.getPolygons().get(0);
        assertEquals(Arrays.asList(1, 2), remainingPolygon.getVertexIndices());
        // Нормали тоже должны быть переиндексированы
        assertEquals(Arrays.asList(1, 2), remainingPolygon.getNormalIndices());
    }
}