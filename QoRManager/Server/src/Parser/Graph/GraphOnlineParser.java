package Parser.Graph;

import org.apache.logging.log4j.Logger;

import Monitoring.Enums.MeasurableValues;
import Network.DataModel.CommunicationMessages;
import Structures.Graph.Edge;
import Structures.Graph.Graph;
import Structures.Graph.Vertex;
import Structures.Graph.interfaces.IVertex;
import Structures.IGraph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static io.github.atomfinger.touuid.UUIDs.*;

/**
 * Parser for graphonline.ru
 */
public class GraphOnlineParser {
    private final Logger _Logger;

    public GraphOnlineParser(Logger logger) {
        _Logger = logger;
    }

    public Graph loadBaseGraph(String fileName, UUID graphID) {
        // Instantiate the Factory
        Graph graph = null;
        UUID graphIDToUse = graphID;

        try {
            var doc = getDocument(fileName);
            var elements = doc.getDocumentElement().getFirstChild().getChildNodes();
            for (int i = 0; i < elements.getLength(); i++) {
                var node = elements.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    var element = (Element) node;

                    if (element.getNodeName().equals("graph") && graphIDToUse == null) {
                        graphIDToUse = toUUID(Integer.parseInt(element.getAttribute("uidGraph")));
                    }
                    if (element.getNodeName().equals("node")) {
                        var vertex = handleVertex(element);
                        if (graph == null) {
                            graph = new Graph(graphIDToUse, vertex, true, fileName);
                        } else {
                            graph.addVertex(vertex);
                        }
                    } else if (element.getNodeName().equals("edge")) {
                        var id = UUID.randomUUID();//Integer.parseInt(element.getAttribute("id"));
                        var source = graph.getVertexById(toUUID(Integer.parseInt(element.getAttribute("source"))));
                        var target = graph.getVertexById(toUUID(Integer.parseInt(element.getAttribute("target"))));
                        var edge = new Edge(source, target, id);
                        var latency = element.getAttribute("transmissionTime");
                        if (latency.isEmpty())
                            latency = "0";
                        edge.updateWeight(CommunicationMessages.Types.LATENCY.toString(), Integer.parseInt(latency));

                        graph.addEdge(edge);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IllegalArgumentException | IOException e) {
            _Logger.fatal("Failed to load file: " + fileName + "; Exception: " + e);
        }
        return graph;
    }

    public void saveGraphToXML(IGraph graph, String fileName) {
        try {
            createFolderIfNeeded(fileName);
            var doc = createDocument();
            var rootElement = doc.createElement("graphml");
            doc.appendChild(rootElement);
            var graphElement = doc.createElement("graph");
            graphElement.setAttribute("id", "Graph");
            graphElement.setAttribute("uidGraph", graph.getGraphID().toString());
            // INFO: Because we do not use the UIDEdge, but it is present in the graphml files by default, we enter an arbitrary value.
            graphElement.setAttribute("uidEdge", UUID.randomUUID().toString());
            rootElement.appendChild(graphElement);

            addVertexNodes(doc, graphElement, graph.getAllVertices());
            addVertexEdges(doc, graphElement, graph.getAllEdges());

            var outputFile = new File(fileName);

            var in = new DOMSource(doc);
            var out = new StreamResult(outputFile);
            var transformer = getTransformer();

            transformer.transform(in, out);
        } catch (ParserConfigurationException | SAXException | TransformerException | IOException e) {
            _Logger.fatal("Exception: ", e);
        }
    }

    private void createFolderIfNeeded(String fileName) throws IOException {
        File targetFile = new File(fileName);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
    }

    private void addVertexNodes(Document doc, Element graph, List<IVertex> vertices) {
        for(var vertex : vertices) {
            var vertexElement = doc.createElement("node");
            //Graphml attributes
            vertexElement.setAttribute("id", vertex.getId().toString());
            vertexElement.setAttribute("mainText", vertex.getLabel());
            vertexElement.setAttribute("upText", "");
            vertexElement.setAttribute("size", "30");
            vertexElement.setAttribute("positionX", 10 + vertex.getApplicationIndex()*50 + vertex.getApproximationIndex()*5 + "");
            vertexElement.setAttribute("positionY", 50 + vertex.getStage() * 50 + "");

            //Own attributes
            vertexElement.setAttribute("application", vertex.getApplicationIndex()+"");
            vertexElement.setAttribute("approximation", vertex.getApproximationIndex()+"");
            vertexElement.setAttribute("stage", vertex.getStage()+"");
            vertexElement.setAttribute("executionTime", vertex.getWeight(MeasurableValues.TIME.name()).getValue().toString());
            vertexElement.setAttribute("QoR", vertex.getWeight(MeasurableValues.QoR.name()).getValue().toString());
            vertexElement.setAttribute("CPU", vertex.getWeight(MeasurableValues.CPU.name()).getValue().toString());
            vertexElement.setAttribute("RAM", vertex.getWeight(MeasurableValues.RAM.name()).getValue().toString());
            vertexElement.setAttribute("energy", vertex.getWeight(MeasurableValues.ENERGY.name()).getValue().toString());
            //Dummy Parameters - might not be needed anymore
            vertexElement.setAttribute("parameter1", vertex.getWeight(MeasurableValues.PARAMETER_1.name()).getValue().toString());
            vertexElement.setAttribute("parameter2", vertex.getWeight(MeasurableValues.PARAMETER_2.name()).getValue().toString());
            vertexElement.setAttribute("parameter3", vertex.getWeight(MeasurableValues.PARAMETER_3.name()).getValue().toString());
            vertexElement.setAttribute("parameter4", vertex.getWeight(MeasurableValues.PARAMETER_4.name()).getValue().toString());
            vertexElement.setAttribute("parameter5", vertex.getWeight(MeasurableValues.PARAMETER_5.name()).getValue().toString());

            //Add element to graph
            graph.appendChild(vertexElement);
        }
    }

    private void addVertexEdges(Document doc, Element graph, List<Edge> edges) {
        for(var edge : edges) {
            var edgeElement = doc.createElement("edge");
            //Graphml attributes
            edgeElement.setAttribute("source", edge.getSource().getId().toString());
            edgeElement.setAttribute("target", edge.getDestination().getId().toString());
            edgeElement.setAttribute("isDirect", "true");
            edgeElement.setAttribute("weight", "1");
            edgeElement.setAttribute("useWeight", "false");
            edgeElement.setAttribute("id", edge.id().toString());
            edgeElement.setAttribute("text", "");
            edgeElement.setAttribute("upText", "");
            edgeElement.setAttribute("arrayStyleStart", "");
            edgeElement.setAttribute("arrayStyleFinish", "");
            edgeElement.setAttribute("model_width", "4");
            edgeElement.setAttribute("model_type", "0");
            edgeElement.setAttribute("model_curveValue", "0.1");

            //Own attributes
            edgeElement.setAttribute("transmissionTime", edge.getWeight(MeasurableValues.LATENCY.name()).getValue().toString());
            //INFO: Possible additions that have been used in the past but are not really stored in the edge
            //edgeElement.setAttribute("transmissionTimeBound", ??);

            //Add element to graph
            graph.appendChild(edgeElement);
        }
    }

    public Map<Edge, List<CommunicationMessages.TestSetupParameter>> generateCostsForEdge(String fileName, IGraph g) {
        var parameter = new HashMap<Edge, List<CommunicationMessages.TestSetupParameter>>();
        try {
            var doc = getDocument(fileName);
            var elements = doc.getDocumentElement().getFirstChild().getChildNodes();
            for (int i = 0; i < elements.getLength(); i++) {
                var node = elements.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    var element = (Element) node;

                    if (element.getNodeName().equals("edge")) {
                        var id = UUID.randomUUID();//Integer.parseInt(element.getAttribute("id"));
                        var source = toUUID(Integer.parseInt(element.getAttribute("source")));
                        var target = toUUID(Integer.parseInt(element.getAttribute("target")));
                        var edge = g.getEdgesBetweenVertices(source, target);
                        var costs = handleCostForEdge(element);
                        parameter.put(edge.get(0), costs);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            _Logger.fatal("Exception: ", e);
        }
        return parameter;
    }

    public Map<IVertex, List<CommunicationMessages.TestSetupParameter>> generateCostsForVertex(String fileName) {
        var parameter = new HashMap<IVertex, List<CommunicationMessages.TestSetupParameter>>();
        try {
            var doc = getDocument(fileName);
            var elements = doc.getDocumentElement().getFirstChild().getChildNodes();
            for (int i = 0; i < elements.getLength(); i++) {
                var node = elements.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    var element = (Element) node;

                    if (element.getNodeName().equals("node")) {
                        var vertex = handleVertex(element);
                        var costs = handleCostForVertex(element, null);
                        parameter.put(vertex, costs);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            _Logger.fatal("Exception: ", e);
        }
        return parameter;
    }

    private DocumentBuilderFactory getDBFactory() throws ParserConfigurationException {
        var dbf = DocumentBuilderFactory.newInstance();

        // optional, but recommended
        // process XML securely, avoid attacks like XML External Entities (XXE)
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        return dbf;
    }

    private Transformer getTransformer() throws TransformerConfigurationException {
        var transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();

        return transformer;
    }

    private Document getDocument(String fileName) throws ParserConfigurationException, IOException, SAXException {
        var dbf = getDBFactory();

        // parse XML file
        var db = dbf.newDocumentBuilder();
        var doc = db.parse(new File(fileName));

        // optional, but recommended
        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        return doc;
    }

    private Document createDocument() throws ParserConfigurationException, IOException, SAXException {
        var dbf = getDBFactory();

        //create Document
        var db = dbf.newDocumentBuilder();
        return db.newDocument();
    }

    private List<CommunicationMessages.TestSetupParameter> handleCostForEdge(Element element) {
        var parameters = new ArrayList<CommunicationMessages.TestSetupParameter>();

        parameters.add(CommunicationMessages.TestSetupParameter.newBuilder()
                .setParameterType(CommunicationMessages.Types.LATENCY)
                .setNegativePercentage(Integer.parseInt(element.getAttribute("transmissionTimeBound")))
                .setPositivePercentage(Integer.parseInt(element.getAttribute("transmissionTimeBound")))
                .setExpectedValue(Integer.parseInt(element.getAttribute("transmissionTime")))
                .build());

        return parameters;
    }

    public List<CommunicationMessages.TestSetupParameter> handleCostForEdge(Edge edge) {
        var parameters = new ArrayList<CommunicationMessages.TestSetupParameter>();

        parameters.add(CommunicationMessages.TestSetupParameter.newBuilder()
                .setParameterType(CommunicationMessages.Types.LATENCY)
                .setNegativePercentage(10)
                .setPositivePercentage(10)
                .setExpectedValue((int)edge.getWeight(MeasurableValues.LATENCY.name()).getValue())
                .build());

        return parameters;
    }

    public List<CommunicationMessages.TestSetupParameter> handleCostForVertex(Element element, IVertex vertex) {
        var parameters = new ArrayList<CommunicationMessages.TestSetupParameter>();

        parameters.add(CommunicationMessages.TestSetupParameter.newBuilder()
                .setParameterType(CommunicationMessages.Types.CPU)
                .setNegativePercentage(10)
                .setPositivePercentage(10)
                .setExpectedValue(30)
                .build());

        parameters.add(CommunicationMessages.TestSetupParameter.newBuilder()
                .setParameterType(CommunicationMessages.Types.RAM)
                .setNegativePercentage(15)
                .setPositivePercentage(15)
                .setExpectedValue(120)
                .build());

        /*parameters.add(CommunicationMessages.TestSetupParameter.newBuilder()
                .setParameterType(CommunicationMessages.Types.TIME)
                .setNegativePercentage(Integer.parseInt(element.getAttribute("executionTimeBound")))
                .setPositivePercentage(Integer.parseInt(element.getAttribute("executionTimeBound")))
                .setExpectedValue(Integer.parseInt(element.getAttribute("executionTime")))
                .build());*/
        int time = 0;
        if (vertex != null)
            time = (int)vertex.getWeight(MeasurableValues.TIME.name()).getValue();
        else if (element != null)
            time = Integer.parseInt(element.getAttribute("executionTime"));
        parameters.add(CommunicationMessages.TestSetupParameter.newBuilder()
                .setParameterType(CommunicationMessages.Types.TIME)
                .setNegativePercentage(0)
                .setPositivePercentage(0)
                .setExpectedValue(time)
                .build());

        return parameters;
    }

    private IVertex handleVertex(Element element) {
        var id = toUUID(Integer.parseInt(element.getAttribute("id")));
        var label = element.getAttribute("mainText");
        var application = element.getAttribute("application");
        var approximation = element.getAttribute("approximation");
        var stage = element.getAttribute("stage");
        var qor = element.getAttribute(MeasurableValues.QoR.name());
        var executionTime = element.getAttribute("executionTime");
        var vertex = new Vertex(label, id, "");
        vertex.updateServiceName(label);
        vertex.updateWeight(CommunicationMessages.Types.CPU.toString(), 30);
        vertex.updateWeight(CommunicationMessages.Types.RAM.toString(), 120);

        if (!application.isEmpty())
            vertex.setApplicationIndex(Integer.parseInt(application));
        if (!approximation.isEmpty())
            vertex.setApproximationIndex(Integer.parseInt(approximation));
        if (!stage.isEmpty())
            vertex.setStage(Integer.parseInt(stage));
        if (!qor.isEmpty())
            vertex.setQoR(Integer.parseInt(qor));
        if (!executionTime.isEmpty())
            vertex.updateWeight(CommunicationMessages.Types.TIME.toString(), Integer.parseInt(executionTime));

        return vertex;
    }
}
