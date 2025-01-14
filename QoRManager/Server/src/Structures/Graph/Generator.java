package Structures.Graph;

import Monitoring.Enums.MeasurableValues;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static io.github.atomfinger.touuid.UUIDs.toUUID;

public class Generator {
    private static final String XLS_EXTENSION = "xls";
    private static final String XLSX_EXTENSION = "xlsx";

    private final String _Filepath;
    private final Workbook _Workbook;

    public Generator(String filepath) throws IOException {
        _Filepath = filepath;
        _Workbook = openFile();
    }

    private Workbook openFile() throws IOException {
        var excelFile = new File(_Filepath);
        var excelInput = new FileInputStream(excelFile);
        var fileExtension = FilenameUtils.getExtension(excelFile.getName());

        Workbook workbook;
        if(XLS_EXTENSION.equalsIgnoreCase(fileExtension))
            workbook = new HSSFWorkbook(excelInput);
        else if (XLSX_EXTENSION.equalsIgnoreCase(fileExtension))
            workbook = new XSSFWorkbook(excelInput);
        else
            throw new FileNotFoundException("Please select " + XLS_EXTENSION + "-File or " + XLSX_EXTENSION + "-File!");

        return workbook;
    }

    public Graph generateGraph(){
        var graph =  generate("Graph", true);
        determineNodeInformation(graph, "Nodes");
        determineApproximateCosts(graph, "Costs");

        // TODO: ELIAS FRAGEN WAS DAS BEDEUTET?
        //PROBABLY NOT IDEAL - WAS DEVELOPED FOR MOBIDIC
        setGraphStages(graph, graph.getStart());

        return graph;
    }

    public Graph generate(String sheetName, boolean directed) {
        var vertices = new ArrayList<Vertex>();
        var vertexArray = new Vertex[0];
        var vertexCount = 0;

        var sheet = _Workbook.getSheet(sheetName);
        var verticesIterator = sheet.iterator();
        var edgesIterator = sheet.iterator();
        Row currentRow;

        while(verticesIterator.hasNext()) {
            currentRow = verticesIterator.next();

            if (currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().equalsIgnoreCase(""))
                continue;
            if (currentRow.getCell(0).getStringCellValue().equalsIgnoreCase("END"))
                break;

            vertices.add(new Vertex(currentRow.getCell(0).getStringCellValue(), toUUID(vertexCount), ""));
            vertexCount++;
        }

        vertexArray = vertices.toArray(vertexArray);
        var graph = new Graph(UUID.randomUUID(), vertexArray[0], directed, _Filepath);

        for(int i = 1; i < vertexArray.length; i++){
            graph.addVertex(vertexArray[i]);
        }

        while(edgesIterator.hasNext()) {
            currentRow = edgesIterator.next();
            if(currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().equalsIgnoreCase(""))
                continue;
            if(currentRow.getCell(0).getStringCellValue().equalsIgnoreCase("END"))
                break;

            var identifier = currentRow.getCell(0).getStringCellValue();
            var currentVertex = graph.getVertexByIdentifier(identifier);

            for(int i = 0; i < vertexArray.length; i++) {
                var row = currentRow.getCell(i+1);
                if(row != null) {
                    if(row.getStringCellValue().equalsIgnoreCase("x")){
                        graph.addEdge(currentVertex.getId(), toUUID(i), UUID.randomUUID());
                    }
                }
            }
        }

        return graph;
    }

    private void determineNodeInformation(Graph graph, String sheetName){
        var sheet = _Workbook.getSheet(sheetName);
        var rowIterator = sheet.iterator();
        Row currentRow;

        while(rowIterator.hasNext()) {
            currentRow = rowIterator.next();

            if(currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().equalsIgnoreCase(""))
                continue;
            if(currentRow.getCell(0).getStringCellValue().equalsIgnoreCase("END"))
                break;

            var currentVertex = graph.getVertexByIdentifier(currentRow.getCell(0).getStringCellValue());
            currentVertex.updateServiceName(currentRow.getCell(1).getStringCellValue());
            currentVertex.setApplicationIndex((int) currentRow.getCell(3).getNumericCellValue());
            currentVertex.setApproximationIndex((int) currentRow.getCell(4).getNumericCellValue());
        }
    }

    private void determineApproximateCosts(Graph graph, String sheetName){
        var sheet = _Workbook.getSheet(sheetName);
        var rowIterator = sheet.iterator();
        Row currentRow;

        while(rowIterator.hasNext()) {
            currentRow = rowIterator.next();

            if (currentRow.getCell(0) == null || currentRow.getCell(0).getStringCellValue().equalsIgnoreCase(""))
                continue;
            if (currentRow.getCell(0).getStringCellValue().equalsIgnoreCase("END"))
                break;
            var serviceName = currentRow.getCell(0).getStringCellValue();

            for (var currentVertex : graph.getAllVertices()) {
                var edges = graph.getOutgoingEdges(currentVertex);
                for(var currentEdge : edges) {
                    currentEdge.updateWeight(MeasurableValues.LATENCY.name(), 0);
                }

                if(!currentVertex.getServiceName().equals(serviceName))
                    continue;

                currentVertex.updateWeight(MeasurableValues.TIME.name(), (int) currentRow.getCell(1).getNumericCellValue());
                currentVertex.updateWeight(MeasurableValues.CPU.name(), (int) currentRow.getCell(2).getNumericCellValue());
                currentVertex.updateWeight(MeasurableValues.RAM.name(), (int) currentRow.getCell(3).getNumericCellValue());
                currentVertex.setQoR((int) currentRow.getCell(4).getNumericCellValue());
            }
        }
    }

    /** Method to initialize all stages in a graph
     *
     * @param startVertex the Vertex to start the process from
     */
    public void setGraphStages(IGraph graph, IVertex startVertex){
        startVertex.setStage(0);
        setStages(graph, startVertex);
    }

    /** Helper method to initialize all stages in a graph
     *
     * @param currentVertex the vertex which is currently handled
     */
    private void setStages(IGraph graph, IVertex currentVertex){
        var edges = graph.getOutgoingEdges(currentVertex);

        for(var edge : edges){
            if(edge.getDestination().getStage() < currentVertex.getStage() + 1) {
                edge.getDestination().setStage(currentVertex.getStage() + 1);
            }
        }

        for(var edge : edges){
            setStages(graph, edge.getDestination());
        }
    }
}
