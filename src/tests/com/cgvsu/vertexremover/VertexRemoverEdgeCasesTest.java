package tests.com.cgvsu.vertexremover;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.model.VertexRemover;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VertexRemoverEdgeCasesTest {

    @Test
    void testNullModel() {
        // Должен корректно обработать null-модель без исключений
        assertDoesNotThrow(() -> {
            VertexRemover.removeVertices(null, Arrays.asList(1, 2, 3));
        });
    }

    @Test
    void testNullVerticesList() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(0, 0, 0));

        // Должен корректно обработать null-список без исключений
        assertDoesNotThrow(() -> {
            VertexRemover.removeVertices(model, null);
        });

        // Модель не должна измениться
        assertEquals(1, model.getVertices().size());
    }

    @Test
    void testModelWithNoPolygons() {
        Model model = new Model();
        model.getVertices().add(new Vector3f(0, 0, 0));
        model.getVertices().add(new Vector3f(1, 0, 0));

        // Удаляем вершину из модели без полигонов
        VertexRemover.removeVertices(model, Arrays.asList(1));

        assertEquals(1, model.getVertices().size());
        assertTrue(model.getPolygons().isEmpty());
    }

    @Test
    void testVertexBelongsToMultiplePolygons() {
        Model model = new Model();

        // 4 вершины
        model.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0), // 1
                new Vector3f(1, 0, 0), // 2
                new Vector3f(0, 1, 0), // 3
                new Vector3f(1, 1, 0)  // 4
        ));

        // Два треугольника, оба используют вершину 1
        Polygon poly1 = new Polygon();
        poly1.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        Polygon poly2 = new Polygon();
        poly2.setVertexIndices(new ArrayList<>(Arrays.asList(1, 3, 4)));

        model.getPolygons().add(poly1);
        model.getPolygons().add(poly2);

        // Удаляем вершину 1, которая входит в оба полигона
        VertexRemover.removeVertices(model, Arrays.asList(1));

        // Оба полигона должны быть удалены
        assertEquals(0, model.getPolygons().size());
        assertEquals(3, model.getVertices().size());
    }

    @Test
    void testRepeatedVertexIndicesInList() {
        Model model = new Model();
        model.getVertices().addAll(Arrays.asList(
                new Vector3f(0, 0, 0),
                new Vector3f(1, 0, 0),
                new Vector3f(0, 1, 0)
        ));

        Polygon poly = new Polygon();
        poly.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));
        model.getPolygons().add(poly);

        // Пытаемся удалить вершину 2 дважды
        VertexRemover.removeVertices(model, Arrays.asList(2, 2));

        // Вершина 2 должна быть удалена только один раз
        assertEquals(2, model.getVertices().size());
        assertEquals(0, model.getPolygons().size());
    }
}