from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle
from reportlab.lib.colors import HexColor

OUT = "docs/design.pdf"
s = getSampleStyleSheet()
s.add(ParagraphStyle(name="CenterTitle", parent=s["Title"], alignment=TA_CENTER, textColor=HexColor("#17365D")))
s.add(ParagraphStyle(name="BlueHead", parent=s["Heading1"], textColor=HexColor("#17365D"), spaceBefore=10, spaceAfter=6))
s.add(ParagraphStyle(name="Small", parent=s["BodyText"], fontSize=9.4, leading=13))
P = lambda text, style="Small": Paragraph(text, s[style])

def footer(c, doc):
    c.saveState(); c.setStrokeColor(HexColor("#B7C9E2")); c.line(.65*inch,.52*inch,7.85*inch,.52*inch)
    c.setFont("Helvetica",8); c.setFillColor(HexColor("#667085")); c.drawString(.65*inch,.34*inch,"CMPE 202 - Event Stream Analyzer"); c.drawRightString(7.85*inch,.34*inch,f"Page {doc.page}"); c.restoreState()

story = [P("Event Stream Analyzer", "CenterTitle"), P("Part I - Design", "BlueHead"),
P("<b>Problem statement.</b> The application reads one event record per line from a plain-text IoT or smart-building log. It parses key/value fields, ignores malformed records, classifies valid records as sensor readings, device events, or access records, and writes the required aggregates as three readable JSON files. Parsing, classification, aggregation, and output are separate so future record types and formats can be added without rewriting existing logic."),
P("Pattern selection", "BlueHead"),
P("<b>Chain of Responsibility.</b> <i>ClassifierChain</i> holds independent classifiers for sensors, devices, and access records. Each classifier either recognizes the line or passes it on. This fits because classification is an ordered set of independent validation rules. A single large conditional would be shorter initially, but harder to test and extend."),
P("<b>Strategy.</b> <i>OutputWriter</i> is the output boundary, with <i>JsonOutputWriter</i> as the current strategy. A future <i>CsvOutputWriter</i> can be selected without changing parsers or aggregators. The trade-off is a small abstraction and one additional class per format."),
P("<b>Factory.</b> <i>ClassifierChainFactory</i> and <i>AggregatorFactory</i> centralize object creation. This keeps construction decisions out of the processing loop. The trade-off is modest indirection, but tests and future configuration do not need to know the concrete class graph."),
P("Consequences", "BlueHead"),
P("These patterns create more small classes than a procedural implementation, but each class has one reason to change. This improves unit testing, makes malformed-input behavior local, and supports extension. The primary cost is indirection: a contributor must follow the chain and factories to see the complete flow. The dependency-free JSON serializer reduces setup while keeping the output boundary replaceable."), PageBreak(),
P("Extensibility and Design Review", "CenterTitle"), P("Extensibility requirement", "BlueHead")]

rows = [[P("Hypothetical change"),P("Classes to add or modify"),P("Existing core logic")],
[P("Network Logs"),P("Add <i>NetworkClassifier</i>, <i>NetworkAggregator</i>, Network type, and factory registrations. Add the output mapping."),P("Parser and existing aggregators remain unchanged.")],
[P("JSON Lines input"),P("Add <i>RecordParser</i>; keep <i>KeyValueParser</i> and add <i>JsonLineParser</i>. Select the parser at the composition boundary."),P("Both return <i>ParsedLine</i>; classification and aggregation remain unchanged.")],
[P("CSV output"),P("Add <i>CsvOutputWriter</i> implementing <i>OutputWriter</i>; select it at the output boundary."),P("JSON and aggregation logic remain unchanged.")]]
t=Table(rows,colWidths=[1.35*inch,3.6*inch,1.95*inch],repeatRows=1)
t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),HexColor('#17365D')),('TEXTCOLOR',(0,0),(-1,0),colors.white),('GRID',(0,0),(-1,-1),.35,HexColor('#B7C9E2')),('VALIGN',(0,0),(-1,-1),'TOP'),('BACKGROUND',(0,1),(-1,-1),HexColor('#F7FAFF')),('LEFTPADDING',(0,0),(-1,-1),6),('RIGHTPADDING',(0,0),(-1,-1),6),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7)]))
story += [t, Spacer(1,14), P("Implementation mapping", "BlueHead"), P("<i>KeyValueParser</i> parses quoted values; <i>ClassifierChain</i> selects a record type; one aggregator per type computes the required statistics; <i>JsonOutputWriter</i> always writes all three files, including empty JSON objects. JUnit 5 tests cover parsing, classification, each aggregator, malformed input, tie-breaking, percentiles, and empty cases."), PageBreak(), P("UML Class Diagram", "CenterTitle")]

def draw_page(c, doc):
    if doc.page != 3: return
    c.saveState(); c.setStrokeColor(HexColor('#6B84A8')); c.setFillColor(HexColor('#EAF2FB'))
    def box(x,y,w,h,title,lines,fill='#EAF2FB'):
        c.setFillColor(HexColor(fill)); c.roundRect(x,y,w,h,6,fill=1,stroke=1); c.setFillColor(HexColor('#17365D')); c.setFont('Helvetica-Bold',8); c.drawCentredString(x+w/2,y+h-13,title); c.setFillColor(colors.black); c.setFont('Helvetica',7)
        for i,line in enumerate(lines): c.drawCentredString(x+w/2,y+h-27-i*10,line)
    def a(x1,y1,x2,y2): c.line(x1,y1,x2,y2); c.line(x2,y2,x2-5,y2+3); c.line(x2,y2,x2-5,y2-3)
    box(245,620,120,48,'App',['main / analyze']); box(70,485,125,62,'KeyValueParser',['parse(line)','returns ParsedLine']); box(245,485,125,62,'ClassifierChain',['<<Chain>>','classify(ParsedLine)']); box(420,485,125,62,'OutputWriter',['<<Strategy>>','write(...)'])
    box(210,325,195,70,'Aggregator',['<<interface>>','accept(ParsedLine)','result()']); box(55,170,140,70,'SensorAggregator',['statistics']); box(235,170,140,70,'DeviceAggregator',['severity / device']); box(415,170,140,70,'AccessAggregator',['percentiles / flags']); box(45,620,135,50,'Factories',['ClassifierChainFactory','AggregatorFactory'],'#FCE8D8')
    a(245,638,180,645); a(275,620,132,547); a(305,620,307,547); a(335,620,482,547); a(307,485,307,395); a(270,325,125,240); a(305,325,305,240); a(340,325,485,240)
    c.setFont('Helvetica',8); c.setFillColor(HexColor('#344054')); c.drawString(65,120,'Interfaces and pattern roles are labeled inside the boxes.'); c.restoreState()

doc=SimpleDocTemplate(OUT,pagesize=letter,rightMargin=.7*inch,leftMargin=.7*inch,topMargin=.65*inch,bottomMargin=.7*inch)
doc.build(story,onFirstPage=footer,onLaterPages=lambda c,d:(footer(c,d),draw_page(c,d)))
