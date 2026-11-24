from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter, landscape
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import Image, PageBreak, Paragraph, SimpleDocTemplate, Spacer

OUT = "docs/design.pdf"
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="TitleBlue", parent=styles["Title"], alignment=TA_CENTER,
                          textColor=colors.HexColor("#17365D"), spaceAfter=16))
styles.add(ParagraphStyle(name="HeadBlue", parent=styles["Heading1"],
                          textColor=colors.HexColor("#17365D"), spaceBefore=10, spaceAfter=6))
styles.add(ParagraphStyle(name="BodySmall", parent=styles["BodyText"], fontSize=10,
                          leading=14, spaceAfter=7))

P = lambda text, style="BodySmall": Paragraph(text, styles[style])

def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#B7C9E2"))
    canvas.line(0.65 * inch, 0.52 * inch, 7.85 * inch, 0.52 * inch)
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.HexColor("#667085"))
    canvas.drawString(0.65 * inch, 0.34 * inch, "CMPE 202 - Event Stream Analyzer")
    canvas.drawRightString(7.85 * inch, 0.34 * inch, f"Page {doc.page}")
    canvas.restoreState()

story = [
    P("Event Stream Analyzer", "TitleBlue"),
    P("Part I - Design", "HeadBlue"),
    P("<b>Problem statement.</b> The application reads one event record per line from a plain-text IoT or smart-building log. It parses key/value fields, skips malformed records, classifies valid records as sensor readings, device events, or access records, and writes three readable JSON files. Parsing, classification, aggregation, and output are separated so future record types and formats can be added without rewriting existing logic."),
    P("Pattern selection", "HeadBlue"),
    P("<b>Chain of Responsibility.</b> <i>ClassifierChain</i> holds <i>SensorClassifier</i>, <i>DeviceClassifier</i>, and <i>AccessClassifier</i> behind the <i>RecordClassifier</i> interface. They are tried in order and the first matching record type wins. This makes independent validation rules easy to test and extend."),
    P("<b>Strategy.</b> <i>OutputWriter</i> is the output boundary and <i>JsonOutputWriter</i> is the current implementation. A future <i>CsvOutputWriter</i> can implement the same interface without changing classification or aggregation."),
    P("<b>Simple Factory.</b> <i>ClassifierChainFactory</i> builds the ordered classifier list and <i>AggregatorFactory</i> creates the aggregator for a <i>RecordType</i>. This keeps construction decisions out of the processing loop."),
    P("Consequences", "HeadBlue"),
    P("The design adds small classes and some indirection compared with a procedural solution. In return, each class has one reason to change, malformed-input behavior is local, and each classifier and aggregator can be tested independently. The dependency-free JSON writer honors the core-logic constraint; it can be replaced by a JSON library later if requirements become more complex."),
    PageBreak(),
    P("Extensibility", "TitleBlue"),
    P("<b>Network Logs:</b> add a <i>NETWORK</i> record type, classifier, aggregator, factory registration, and output mapping. Existing parser and aggregators remain unchanged."),
    P("<b>JSON Lines input:</b> add a parser interface and a <i>JsonLineParser</i>, then select the parser at the composition boundary. Both parsers return <i>ParsedLine</i>, so classification and aggregation remain unchanged."),
    P("<b>CSV output:</b> add <i>CsvOutputWriter</i> implementing <i>OutputWriter</i> and select it in <i>App</i>. JSON serialization and aggregation remain unchanged."),
    P("Implemented input and output rules", "HeadBlue"),
    P("Every valid line begins with a <i>ts</i> field; the timestamp is ignored during aggregation. Blank lines, missing equals signs, empty keys or values, unclosed quotes, duplicate keys, and invalid classification fields are skipped. Sensor standard deviation is population standard deviation, sensor median averages the two middle values for an even set, access percentiles use nearest rank, and decimal results are rounded to one decimal place with whole numbers written without a decimal point."),
    PageBreak(),
    P("UML Class Diagram", "TitleBlue"),
    P("The full PlantUML source is provided in <i>docs/class-diagram.puml</i>. A high-resolution PNG version is included in <i>docs/UMLDiagram.png</i> for clearer viewing."),
    Spacer(1, 8),
    Image("docs/UMLDiagram.png", width=7.1 * inch, height=7.1 * inch * 858 / 3270),
]

doc = SimpleDocTemplate(OUT, pagesize=letter, rightMargin=.7 * inch, leftMargin=.7 * inch,
                        topMargin=.65 * inch, bottomMargin=.7 * inch)
doc.build(story, onFirstPage=footer, onLaterPages=footer)
